// Henry Jackson
// AI wrote most of this, not tested
// need to go through and tune integration
// debugging needed

/**
 * Screen.js
 *
 * Base Screen class for the app. A Screen is a self-contained GUI view that
 * mounts into a root DOM element, holds local state, renders elements, and
 * can translate JSON data (posts, profiles) into interactive DOM components.
 *
 * This file defines:
 * - Screen: base class handling state, mounting, event delegation, and helpers
 * - Example subclasses: FeedScreen, ProfileScreen, LoginScreen (small demos)
 *
 * The implementation is DOM-oriented (browser). If run in a non-DOM environment
 * it will throw when attempting to mount or render; you can still use it for
 * server-side rendering by overriding render to produce strings.
 */

'use strict';

/**
 * Simple helper to ensure we have a DOM element from a selector or element.
 * @param {string|HTMLElement} root
 * @returns {HTMLElement}
 */
function resolveRoot(root) {
	if (!root) throw new Error('root is required (selector or HTMLElement)');
	if (typeof root === 'string') {
		const el = document.querySelector(root);
		if (!el) throw new Error(`no element matches selector: ${root}`);
		return el;
	}
	if (root instanceof HTMLElement) return root;
	throw new Error('root must be a selector string or HTMLElement');
}

/**
 * Base Screen class
 */
class Screen {
	/**
	 * @param {Object} opts
	 * @param {string|HTMLElement} opts.root - selector or element to mount into
	 * @param {Object} [opts.props] - external props (read-only by convention)
	 * @param {Object} [opts.state] - initial local state
	 */
	constructor({ root, props = {}, state = {} } = {}) {
		this.rootSelector = root || null;
		this.root = null; // resolved HTMLElement after mount
		this.props = Object.freeze(Object.assign({}, props));
		this.state = Object.assign({}, state);
		this._elements = new Map(); // named registered DOM elements
		this._delegatedHandlers = [];
		this.mounted = false;
	}

	/**
	 * Mount the screen into the DOM and call render.
	 */
	mount() {
		if (this.mounted) return;
		if (typeof document === 'undefined') throw new Error('DOM not available');
		this.root = resolveRoot(this.rootSelector || 'body');
		this.mounted = true;
		this.onMount();
		this.render();
	}

	/**
	 * Hook for subclasses; called once when mounting.
	 */
	onMount() {}

	/**
	 * Unmount the screen and remove delegated handlers.
	 */
	unmount() {
		if (!this.mounted) return;
		// remove delegated handlers
		this._delegatedHandlers.forEach(h => {
			this.root.removeEventListener(h.event, h.wrapped);
		});
		this._delegatedHandlers = [];
		this.mounted = false;
		this.onUnmount();
	}

	onUnmount() {}

	/**
	 * Set state and re-render.
	 * @param {Object|Function} patch - partial state object or function(prevState) -> partial
	 * @param {boolean} [replace=false] - whether to replace state instead of merging
	 */
	setState(patch, replace = false) {
		const prev = this.state;
		const next = typeof patch === 'function' ? patch(prev) : patch;
		if (replace) {
			this.state = Object.assign({}, next || {});
		} else {
			this.state = Object.assign({}, prev, next || {});
		}
		this.onStateChange(prev, this.state);
		this.render();
	}

	onStateChange(prev, next) {}

	/**
	 * Register a named element for easy access.
	 * @param {string} name
	 * @param {HTMLElement} el
	 */
	registerElement(name, el) {
		this._elements.set(name, el);
	}

	getElement(name) {
		return this._elements.get(name) || null;
	}

	/**
	 * Helper to create an element from a spec: {tag, attrs, children, events}
	 * children can be strings, elements, or arrays.
	 */
	createElement(spec) {
		const tag = spec.tag || 'div';
		const el = document.createElement(tag);
		const attrs = spec.attrs || {};
		for (const k of Object.keys(attrs)) {
			if (k === 'className') el.className = attrs[k];
			else if (k === 'textContent') el.textContent = attrs[k];
			else el.setAttribute(k, attrs[k]);
		}
		const children = spec.children || [];
		for (const c of children) {
			if (c == null) continue;
			if (Array.isArray(c)) c.forEach(x => appendChild(el, x));
			else appendChild(el, c);
		}
		const events = spec.events || {};
		for (const ev of Object.keys(events)) {
			el.addEventListener(ev, events[ev]);
		}
		return el;

		function appendChild(parent, child) {
			if (typeof child === 'string' || typeof child === 'number') parent.appendChild(document.createTextNode(String(child)));
			else if (child instanceof HTMLElement) parent.appendChild(child);
			else if (child && child.nodeType) parent.appendChild(child);
			else if (child && typeof child === 'object' && child.tag) parent.appendChild(parent.ownerDocument ? parent.ownerDocument.createElement(child.tag) : document.createElement(child.tag));
			else if (child == null) return;
			else parent.appendChild(document.createTextNode(String(child)));
		}
	}

	/**
	 * Delegated event helper: listens at root and dispatches to selector matches.
	 * Returns an object which can be used to remove the handler.
	 */
	on(event, selector, handler) {
		if (!this.root) throw new Error('Screen must be mounted before adding delegated handlers');
		const wrapped = e => {
			let node = e.target;
			while (node && node !== this.root) {
				if (node.matches && node.matches(selector)) {
					return handler.call(node, e, node);
				}
				node = node.parentElement;
			}
		};
		this.root.addEventListener(event, wrapped);
		const record = { event, selector, handler, wrapped };
		this._delegatedHandlers.push(record);
		return record;
	}

	/**
	 * Translate JSON-like data to UI components. Supports types: 'post', 'profile'.
	 * Subclasses can override or extend this.
	 * @param {string} type
	 * @param {Object} data
	 * @returns {HTMLElement}
	 */
	translateJSON(type, data) {
		switch (type) {
			case 'post':
				return this.createPostElement(data);
			case 'profile':
				return this.createProfileElement(data);
			default:
				return this.createElement({ tag: 'pre', attrs: { textContent: JSON.stringify(data, null, 2) } });
		}
	}

	/**
	 * Create a DOM element representing a post object.
	 * Expected post shape: { id, account, title, body, files, upvoters, downvoters, comments, metadata }
	 */
	createPostElement(post) {
		const p = post || {};
		const container = document.createElement('article');
		container.className = 'post';
		container.dataset.postId = p.id || '';

		const header = document.createElement('header');
		header.className = 'post-header';
		header.innerHTML = `<h3 class="post-title">${escapeHtml(p.title || '')}</h3><div class="post-account">${escapeHtml(p.account || '')}</div>`;

		const body = document.createElement('div');
		body.className = 'post-body';
		body.innerText = p.body || '';

		const meta = document.createElement('div');
		meta.className = 'post-meta';
		const upvotes = (p.upvoters && p.upvoters.length) || (p.upvotes || 0);
		const downvotes = (p.downvoters && p.downvoters.length) || (p.downvotes || 0);

		meta.innerHTML = `
			<button class="post-upvote">▲ <span class="count">${upvotes}</span></button>
			<button class="post-downvote">▼ <span class="count">${downvotes}</span></button>
			<span class="post-score">Score: ${upvotes - downvotes}</span>
		`;

		// wire up basic click handlers that bubble as custom events
		meta.querySelector('.post-upvote').addEventListener('click', e => {
			const evt = new CustomEvent('post:upvote', { detail: { post: p, postId: p.id }, bubbles: true });
			container.dispatchEvent(evt);
		});
		meta.querySelector('.post-downvote').addEventListener('click', e => {
			const evt = new CustomEvent('post:downvote', { detail: { post: p, postId: p.id }, bubbles: true });
			container.dispatchEvent(evt);
		});

		// attachments (files)
		const filesEl = document.createElement('div');
		filesEl.className = 'post-files';
		(p.files || []).forEach(f => {
			const a = document.createElement('a');
			a.href = f.url || '#';
			a.textContent = f.name || (f.url || 'attachment');
			a.className = 'post-file';
			filesEl.appendChild(a);
		});

		// comments preview (shallow)
		const commentsEl = document.createElement('div');
		commentsEl.className = 'post-comments';
		const comments = p.comments || [];
		if (comments.length) {
			const ul = document.createElement('ul');
			ul.className = 'comment-list';
			comments.slice(0, 3).forEach(c => {
				const li = document.createElement('li');
				li.textContent = (c.account ? c.account + ': ' : '') + (c.text || c.body || '');
				ul.appendChild(li);
			});
			if (comments.length > 3) {
				const more = document.createElement('div');
				more.className = 'comments-more';
				more.textContent = `+${comments.length - 3} more comments`;
				commentsEl.appendChild(ul);
				commentsEl.appendChild(more);
			} else commentsEl.appendChild(ul);
		}

		container.appendChild(header);
		container.appendChild(body);
		container.appendChild(filesEl);
		container.appendChild(meta);
		container.appendChild(commentsEl);

		return container;

		function escapeHtml(s) {
			if (!s) return '';
			return String(s).replace(/[&<>"]+/g, ch => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[ch] || ch));
		}
	}

	/**
	 * Create a simple profile element from profile JSON
	 * Expected shape: { id, username, displayName, bio, avatarUrl, counts }
	 */
	createProfileElement(profile) {
		const p = profile || {};
		const c = document.createElement('section');
		c.className = 'profile';
		c.innerHTML = `
			<div class="profile-header">
				<img class="avatar" src="${p.avatarUrl || ''}" alt="${p.displayName || p.username || 'avatar'}" />
				<div class="profile-names">
					<h2>${escape(p.displayName || p.username || '')}</h2>
					<div class="username">${escape(p.username || '')}</div>
				</div>
			</div>
			<div class="profile-bio">${escape(p.bio || '')}</div>
		`;
		return c;

		function escape(s) {
			return String(s || '').replace(/[&<>"]+/g, ch => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[ch] || ch));
		}
	}

	/**
	 * Render method: base class does nothing. Subclasses must override.
	 */
	render() {
		// default: clear root
		if (!this.root) return;
		this.root.innerHTML = '';
	}
}

// --- Example subclasses ---

/**
 * FeedScreen: displays a list of posts passed in props.posts or state.posts
 */
class FeedScreen extends Screen {
	constructor(opts = {}) {
		super(opts);
	}

	render() {
		if (!this.root) return;
		this.root.innerHTML = '';
		const container = document.createElement('div');
		container.className = 'feed-screen';
		const posts = this.state.posts || this.props.posts || [];
		if (!posts.length) {
			const empty = document.createElement('div');
			empty.className = 'empty-feed';
			empty.textContent = 'No posts yet.';
			container.appendChild(empty);
		} else {
			posts.forEach(p => {
				const el = this.translateJSON('post', p);
				// re-dispatch events to this.root so callers may listen at the screen root
				el.addEventListener('post:upvote', e => this.root.dispatchEvent(new CustomEvent('post:upvote', { detail: e.detail, bubbles: true })));
				el.addEventListener('post:downvote', e => this.root.dispatchEvent(new CustomEvent('post:downvote', { detail: e.detail, bubbles: true })));
				container.appendChild(el);
			});
		}
		this.root.appendChild(container);
	}
}

/**
 * ProfileScreen: shows a profile and a list of that user's posts
 */
class ProfileScreen extends Screen {
	render() {
		if (!this.root) return;
		this.root.innerHTML = '';
		const profile = this.state.profile || this.props.profile || {};
		const profileEl = this.createProfileElement(profile);
		this.root.appendChild(profileEl);
		const posts = this.state.posts || this.props.posts || [];
		if (posts.length) {
			const list = document.createElement('div');
			list.className = 'profile-posts';
			posts.forEach(p => list.appendChild(this.translateJSON('post', p)));
			this.root.appendChild(list);
		}
	}
}

/**
 * LoginScreen: minimal login form example
 */
class LoginScreen extends Screen {
	onMount() {
		// example: listen for form submit via delegation
		this.on('submit', '.login-form', e => {
			e.preventDefault();
			const form = e.target;
			const data = new FormData(form);
			const username = data.get('username');
			const password = data.get('password');
			this.root.dispatchEvent(new CustomEvent('login:submit', { detail: { username, password } }));
		});
	}

	render() {
		if (!this.root) return;
		this.root.innerHTML = '';
		const form = document.createElement('form');
		form.className = 'login-form';
		form.innerHTML = `
			<label>Username: <input name="username" /></label>
			<label>Password: <input name="password" type="password" /></label>
			<button type="submit">Log in</button>
		`;
		this.root.appendChild(form);
	}
}

// Export for Node.js/CommonJS and browsers
if (typeof module !== 'undefined' && typeof module.exports !== 'undefined') {
	module.exports = { Screen, FeedScreen, ProfileScreen, LoginScreen };
} else {
	window.Screen = Screen;
	window.FeedScreen = FeedScreen;
	window.ProfileScreen = ProfileScreen;
	window.LoginScreen = LoginScreen;
}

