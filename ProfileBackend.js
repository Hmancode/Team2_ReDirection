// Henry Jackson
// This I truly do not know if it will work - also see python version

// ProfileBackend.js
// Backend manager for profiles, posts, files, and messages.
// Provides simple in-memory store with optional JSON file persistence (Node.js fs).

'use strict';

/**
 * ProfileBackend responsibilities:
 * - manage users (create, authenticate, profile data)
 * - manage posts (CRUD, link to users)
 * - manage direct messages
 * - persist to a JSON file when running under Node.js (optional)
 *
 * This is intentionally lightweight and dependency-free. For production use,
 * replace with a proper DB.
 */

const isNode = typeof process !== 'undefined' && process.versions && process.versions.node;
let fs = null;
if (isNode) {
  try {
    fs = require('fs');
  } catch (e) {
    fs = null;
  }
}

function nowISO() {
  return new Date().toISOString();
}

class ProfileBackend {
  /**
   * @param {Object} opts
   * @param {string} [opts.path] - optional file path for JSON persistence (Node.js)
   */
  constructor({ path = null } = {}) {
    this.path = path;
    this._data = {
      users: {}, // id -> { id, username, passwordHash, profile }
      posts: {}, // id -> post JSON
      messages: {}, // id -> { from, to, text, createdAt }
    };
    this._nextId = 1;
    if (this.path && !fs) throw new Error('File persistence requested but fs not available (not running on Node)');
  }

  _allocId(prefix = 'id') {
    return `${prefix}_${this._nextId++}_${Date.now()}`;
  }

  // --- persistence ---
  loadSync() {
    if (!this.path || !fs) throw new Error('No persistence path configured');
    const raw = fs.readFileSync(this.path, 'utf8');
    const obj = JSON.parse(raw || '{}');
    this._data = obj.data || this._data;
    this._nextId = obj.nextId || this._nextId;
  }

  saveSync() {
    if (!this.path || !fs) throw new Error('No persistence path configured');
    const obj = { data: this._data, nextId: this._nextId };
    fs.writeFileSync(this.path, JSON.stringify(obj, null, 2), 'utf8');
  }

  // --- users ---
  createUser({ username, password, profile = {} }) {
    if (!username || !password) throw new Error('username and password required');
    if (this.findUserByUsername(username)) throw new Error('username taken');
    const id = this._allocId('user');
    // NOTE: this stores plain password; in real systems always hash+salt!
    this._data.users[id] = { id, username, passwordHash: String(password), profile: Object.assign({ createdAt: nowISO() }, profile) };
    return this._data.users[id];
  }

  findUserByUsername(username) {
    const users = Object.values(this._data.users);
    return users.find(u => u.username === username) || null;
  }

  getUserById(id) {
    return this._data.users[id] || null;
  }

  authenticate(username, password) {
    const u = this.findUserByUsername(username);
    if (!u) return null;
    // plain check; production must use secure compare + hashing
    if (String(password) === String(u.passwordHash)) return u;
    return null;
  }

  updateProfile(userId, patch) {
    const u = this.getUserById(userId);
    if (!u) throw new Error('user not found');
    u.profile = Object.assign({}, u.profile || {}, patch || {});
    u.profile.updatedAt = nowISO();
    return u.profile;
  }

  // --- posts ---
  createPost({ authorId, title, body, files = [], metadata = {} }) {
    if (!authorId || !this.getUserById(authorId)) throw new Error('valid authorId required');
    const id = this._allocId('post');
    const post = {
      id,
      account: this._data.users[authorId].username,
      authorId,
      title: title || '',
      body: body || '',
      files: files || [],
      upvoters: [],
      downvoters: [],
      comments: [],
      metadata: Object.assign({ createdAt: nowISO(), updatedAt: nowISO() }, metadata || {}),
    };
    this._data.posts[id] = post;
    return post;
  }

  getPost(id) {
    return this._data.posts[id] || null;
  }

  listPosts(filterFn = null) {
    const all = Object.values(this._data.posts || {});
    return typeof filterFn === 'function' ? all.filter(filterFn) : all;
  }

  upvotePost(postId, userId) {
    const p = this.getPost(postId);
    if (!p) throw new Error('post not found');
    const username = this._data.users[userId] && this._data.users[userId].username;
    if (!username) throw new Error('user not found');
    // remove from downvoters
    p.downvoters = (p.downvoters || []).filter(u => u !== username);
    if (!p.upvoters) p.upvoters = [];
    if (!p.upvoters.includes(username)) p.upvoters.push(username);
    p.metadata.updatedAt = nowISO();
    return p;
  }

  downvotePost(postId, userId) {
    const p = this.getPost(postId);
    if (!p) throw new Error('post not found');
    const username = this._data.users[userId] && this._data.users[userId].username;
    if (!username) throw new Error('user not found');
    p.upvoters = (p.upvoters || []).filter(u => u !== username);
    if (!p.downvoters) p.downvoters = [];
    if (!p.downvoters.includes(username)) p.downvoters.push(username);
    p.metadata.updatedAt = nowISO();
    return p;
  }

  // --- messages ---
  sendMessage({ fromId, toId, text }) {
    if (!fromId || !toId) throw new Error('fromId and toId required');
    const fid = this._allocId('msg');
    const m = { id: fid, from: fromId, to: toId, text: text || '', createdAt: nowISO() };
    this._data.messages[fid] = m;
    return m;
  }

  listMessagesForUser(userId) {
    return Object.values(this._data.messages).filter(m => m.from === userId || m.to === userId);
  }

  // --- helpers to glue to Screen objects ---
  /**
   * Return posts formatted for screens (basic sanitized JSON)
   */
  postsForScreen(filterFn = null) {
    return this.listPosts(filterFn).map(p => this._formatPostForScreen(p));
  }

  _formatPostForScreen(p) {
    return {
      id: p.id,
      account: p.account,
      title: p.title,
      body: p.body,
      files: p.files || [],
      upvoters: p.upvoters || [],
      downvoters: p.downvoters || [],
      comments: p.comments || [],
      metadata: p.metadata || {},
    };
  }

  // small utility to export the internal store as JSON
  exportJSON() {
    return JSON.parse(JSON.stringify(this._data));
  }
}

// Export
if (typeof module !== 'undefined' && typeof module.exports !== 'undefined') module.exports = ProfileBackend;
else window.ProfileBackend = ProfileBackend;
