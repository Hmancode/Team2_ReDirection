// Henry Jackson
// AI wrote most of this, not tested

/**
 * Post.js
 *
 * A class representing a forum post and a lightweight Comment helper class.
 * Includes account, title, body, files, upvotes/likes, downvotes, comment chain,
 * and metadata (timestamps, tags, views, pinned/locked flags, id).
 *
 * This file is intentionally dependency-free and works in Node and browser
 * environments. It provides JSON (de)serialization helpers so instances can be
 * stored or transmitted.
 */

'use strict';

/**
 * Lightweight unique id generator.
 * Uses crypto.randomUUID when available, otherwise falls back to timestamp+random.
 * @returns {string}
 */
function generateId() {
	if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
		try {
			return crypto.randomUUID();
		} catch (e) {
			// fall through
		}
	}
	if (typeof require === 'function') {
		try {
			const nodeCrypto = require('crypto');
			if (typeof nodeCrypto.randomUUID === 'function') return nodeCrypto.randomUUID();
		} catch (e) {
			// not available, fall back
		}
	}
	return `id_${Date.now()}_${Math.floor(Math.random() * 1e9)}`;
}

/**
 * Comment class representing a single comment in the comment chain.
 * Comments can have nested replies (forming a tree).
 */
class Comment {
	/**
	 * @param {Object} opts
	 * @param {string} opts.account - author account id or name
	 * @param {string} opts.text - comment text body
	 * @param {string} [opts.id] - optional id
	 * @param {Array<Comment|Object>} [opts.replies]
	 * @param {string|number|Date} [opts.createdAt]
	 */
	constructor({ account, text, id = null, replies = [], createdAt = null } = {}) {
		this.id = id || generateId();
		this.account = account || null;
		this.text = text || '';
		this.replies = (replies || []).map(r => (r instanceof Comment ? r : new Comment(r)));
		this.createdAt = createdAt ? new Date(createdAt) : new Date();
		this.updatedAt = new Date(this.createdAt);
	}

	addReply(reply) {
		const c = reply instanceof Comment ? reply : new Comment(reply);
		this.replies.push(c);
		this.updatedAt = new Date();
		return c;
	}

	toJSON() {
		return {
			id: this.id,
			account: this.account,
			text: this.text,
			replies: this.replies.map(r => r.toJSON()),
			createdAt: this.createdAt.toISOString(),
			updatedAt: this.updatedAt.toISOString(),
		};
	}

	static fromJSON(obj) {
		if (!obj) return null;
		return new Comment({
			id: obj.id,
			account: obj.account,
			text: obj.text,
			replies: (obj.replies || []).map(r => Comment.fromJSON(r)),
			createdAt: obj.createdAt,
		});
	}
}

/**
 * Post class modeling a forum post.
 * Contract:
 * - inputs: constructor accepts an options object (account, title, body, ...).
 * - outputs: instance methods mutate in-memory post; toJSON returns serializable form.
 * - error modes: validation methods return booleans and throw only on clearly invalid ops.
 */
class Post {
	/**
	 * Create a Post
	 * @param {Object} opts
	 * @param {string} opts.account - author account id or name
	 * @param {string} opts.title
	 * @param {string} opts.body
	 * @param {Array<Object>} [opts.files] - array of file metadata: {name, size, mimeType, url}
	 * @param {Set|string[]} [opts.upvoters] - accounts who upvoted (stored as a Set)
	 * @param {Set|string[]} [opts.downvoters]
	 * @param {Array<Comment|Object>} [opts.comments]
	 * @param {Object} [opts.metadata]
	 */
	constructor({
		account = null,
		title = '',
		body = '',
		files = [],
		upvoters = [],
		downvoters = [],
		comments = [],
		metadata = {},
		id = null,
	} = {}) {
		this.id = id || generateId();
		this.account = account;
		this.title = title;
		this.body = body;
		this.files = Array.isArray(files) ? files.slice() : [];
		this.upvoters = new Set(Array.from(upvoters || []));
		this.downvoters = new Set(Array.from(downvoters || []));
		this.comments = (comments || []).map(c => (c instanceof Comment ? c : new Comment(c)));

		// metadata default values
		this.metadata = Object.assign(
			{
				createdAt: new Date(),
				updatedAt: new Date(),
				tags: [],
				views: 0,
				pinned: false,
				locked: false,
				extra: {},
			},
			metadata || {}
		);
	}

	// --- basic editors ---
	editTitle(newTitle) {
		this.title = newTitle;
		this.touch();
	}

	editBody(newBody) {
		this.body = newBody;
		this.touch();
	}

	touch() {
		this.metadata.updatedAt = new Date();
	}

	// --- files ---
	attachFile(fileMeta) {
		// fileMeta: { name, size, mimeType, url }
		if (!fileMeta || !fileMeta.name) throw new Error('fileMeta with name required');
		this.files.push(Object.assign({}, fileMeta));
		this.touch();
		return this.files[this.files.length - 1];
	}

	detachFileByName(name) {
		const before = this.files.length;
		this.files = this.files.filter(f => f.name !== name);
		if (this.files.length !== before) this.touch();
	}

	// --- upvotes/downvotes ---
	upvote(accountId) {
		if (!accountId) throw new Error('accountId required to upvote');
		if (this.downvoters.has(accountId)) this.downvoters.delete(accountId);
		this.upvoters.add(accountId);
		this.touch();
	}

	removeUpvote(accountId) {
		const removed = this.upvoters.delete(accountId);
		if (removed) this.touch();
		return removed;
	}

	downvote(accountId) {
		if (!accountId) throw new Error('accountId required to downvote');
		if (this.upvoters.has(accountId)) this.upvoters.delete(accountId);
		this.downvoters.add(accountId);
		this.touch();
	}

	removeDownvote(accountId) {
		const removed = this.downvoters.delete(accountId);
		if (removed) this.touch();
		return removed;
	}

	get upvotes() {
		return this.upvoters.size;
	}

	get downvotes() {
		return this.downvoters.size;
	}

	get score() {
		return this.upvotes - this.downvotes;
	}

	// --- comments ---
	addComment(comment) {
		const c = comment instanceof Comment ? comment : new Comment(comment);
		this.comments.push(c);
		this.touch();
		return c;
	}

	/**
	 * Find a comment by id in the tree
	 * @param {string} id
	 * @returns {Comment|null}
	 */
	findCommentById(id) {
		const stack = this.comments.slice();
		while (stack.length) {
			const c = stack.shift();
			if (c.id === id) return c;
			for (const r of c.replies) stack.push(r);
		}
		return null;
	}

	/**
	 * Add a reply to a comment by id
	 * @param {string} parentCommentId
	 * @param {Comment|Object} reply
	 */
	addReplyToComment(parentCommentId, reply) {
		const parent = this.findCommentById(parentCommentId);
		if (!parent) throw new Error('parent comment not found');
		const c = parent.addReply(reply);
		this.touch();
		return c;
	}

	// --- metadata utilities ---
	incrementViews(n = 1) {
		this.metadata.views = (this.metadata.views || 0) + Math.max(0, n);
	}

	togglePinned() {
		this.metadata.pinned = !this.metadata.pinned;
		this.touch();
		return this.metadata.pinned;
	}

	toggleLocked() {
		this.metadata.locked = !this.metadata.locked;
		this.touch();
		return this.metadata.locked;
	}

	// --- serialization ---
	toJSON() {
		return {
			id: this.id,
			account: this.account,
			title: this.title,
			body: this.body,
			files: this.files.slice(),
			upvoters: Array.from(this.upvoters),
			downvoters: Array.from(this.downvoters),
			comments: this.comments.map(c => c.toJSON()),
			metadata: Object.assign({}, this.metadata, {
				createdAt: this.metadata.createdAt instanceof Date ? this.metadata.createdAt.toISOString() : this.metadata.createdAt,
				updatedAt: this.metadata.updatedAt instanceof Date ? this.metadata.updatedAt.toISOString() : this.metadata.updatedAt,
			}),
		};
	}

	/**
	 * Create a Post from a plain object (reverse of toJSON)
	 * @param {Object} obj
	 * @returns {Post}
	 */
	static fromJSON(obj) {
		if (!obj) return null;
		return new Post({
			id: obj.id,
			account: obj.account,
			title: obj.title,
			body: obj.body,
			files: obj.files || [],
			upvoters: obj.upvoters || [],
			downvoters: obj.downvoters || [],
			comments: (obj.comments || []).map(c => Comment.fromJSON(c)),
			metadata: Object.assign({}, obj.metadata || {}, {
				createdAt: obj.metadata && obj.metadata.createdAt ? new Date(obj.metadata.createdAt) : new Date(),
				updatedAt: obj.metadata && obj.metadata.updatedAt ? new Date(obj.metadata.updatedAt) : new Date(),
			}),
		});
	}

	// --- validation helpers ---
	isValid() {
		// simple checks: author and title exist
		return !!this.account && typeof this.title === 'string' && this.title.trim().length > 0;
	}
}

// Export for Node.js/CommonJS and also expose in browsers.
if (typeof module !== 'undefined' && typeof module.exports !== 'undefined') {
	module.exports = { Post, Comment };
} else {
	// global export for browsers
	window.Post = Post;
	window.Comment = Comment;
}

