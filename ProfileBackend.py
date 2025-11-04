"""
Henry Jackson
AI wrote most of this, not tested
I truly dont know if this will work, see js version
needs extensive testing
"""

"""
ProfileBackend.py

Lightweight Python backend for profiles, posts, files, and messages.
Provides in-memory store with optional JSON file persistence.

This mirrors the JS ProfileBackend but in Python. Passwords are stored
as salted SHA-256 hashes (simple; replace with bcrypt/argon2 for real apps).
"""

from __future__ import annotations

import json
import os
import uuid
import hashlib
from datetime import datetime
from typing import Optional, Callable, Dict, Any, List


def now_iso() -> str:
    return datetime.utcnow().isoformat() + 'Z'


def _hash_password(password: str, salt: Optional[str] = None) -> Dict[str, str]:
    if salt is None:
        salt = uuid.uuid4().hex
    # simple SHA256(salt + password)
    h = hashlib.sha256((salt + password).encode('utf-8')).hexdigest()
    return {'salt': salt, 'hash': h}


class ProfileBackend:
    """Manage users, posts, and messages with optional JSON persistence."""

    def __init__(self, path: Optional[str] = None):
        self.path = path
        self._data = {
            'users': {},  # id -> {id, username, password_hash, salt, profile}
            'posts': {},  # id -> post dict
            'messages': {},  # id -> message dict
        }
        self._next_id = 1
        if self.path and not isinstance(self.path, str):
            raise ValueError('path must be a string if provided')

    def _alloc_id(self, prefix: str = 'id') -> str:
        ident = f"{prefix}_{self._next_id}_{int(datetime.utcnow().timestamp() * 1000)}"
        self._next_id += 1
        return ident

    # Persistence
    def load(self) -> None:
        if not self.path:
            raise RuntimeError('No persistence path configured')
        if not os.path.exists(self.path):
            return
        with open(self.path, 'r', encoding='utf-8') as fh:
            obj = json.load(fh)
        self._data = obj.get('data', self._data)
        self._next_id = obj.get('next_id', self._next_id)

    def save(self) -> None:
        if not self.path:
            raise RuntimeError('No persistence path configured')
        obj = {'data': self._data, 'next_id': self._next_id}
        with open(self.path, 'w', encoding='utf-8') as fh:
            json.dump(obj, fh, indent=2, ensure_ascii=False)

    # Users
    def create_user(self, username: str, password: str, profile: Optional[Dict[str, Any]] = None) -> Dict[str, Any]:
        if not username or not password:
            raise ValueError('username and password required')
        if self.find_user_by_username(username):
            raise ValueError('username taken')
        uid = self._alloc_id('user')
        ph = _hash_password(password)
        user = {
            'id': uid,
            'username': username,
            'password_hash': ph['hash'],
            'salt': ph['salt'],
            'profile': dict({'createdAt': now_iso()}, **(profile or {})),
        }
        self._data['users'][uid] = user
        return user

    def find_user_by_username(self, username: str) -> Optional[Dict[str, Any]]:
        for u in self._data['users'].values():
            if u.get('username') == username:
                return u
        return None

    def get_user_by_id(self, uid: str) -> Optional[Dict[str, Any]]:
        return self._data['users'].get(uid)

    def authenticate(self, username: str, password: str) -> Optional[Dict[str, Any]]:
        u = self.find_user_by_username(username)
        if not u:
            return None
        ph = _hash_password(password, salt=u.get('salt'))
        if ph['hash'] == u.get('password_hash'):
            return u
        return None

    def update_profile(self, user_id: str, patch: Dict[str, Any]) -> Dict[str, Any]:
        u = self.get_user_by_id(user_id)
        if not u:
            raise KeyError('user not found')
        u['profile'] = {**u.get('profile', {}), **(patch or {})}
        u['profile']['updatedAt'] = now_iso()
        return u['profile']

    # Posts
    def create_post(self, author_id: str, title: str = '', body: str = '', files: Optional[List[Dict[str, Any]]] = None, metadata: Optional[Dict[str, Any]] = None) -> Dict[str, Any]:
        if not author_id or not self.get_user_by_id(author_id):
            raise ValueError('valid author_id required')
        pid = self._alloc_id('post')
        user = self.get_user_by_id(author_id)
        post = {
            'id': pid,
            'account': user.get('username'),
            'authorId': author_id,
            'title': title or '',
            'body': body or '',
            'files': files or [],
            'upvoters': [],
            'downvoters': [],
            'comments': [],
            'metadata': {**({'createdAt': now_iso(), 'updatedAt': now_iso()}), **(metadata or {})},
        }
        self._data['posts'][pid] = post
        return post

    def get_post(self, pid: str) -> Optional[Dict[str, Any]]:
        return self._data['posts'].get(pid)

    def list_posts(self, filter_fn: Optional[Callable[[Dict[str, Any]], bool]] = None) -> List[Dict[str, Any]]:
        all_posts = list(self._data['posts'].values())
        if filter_fn:
            return [p for p in all_posts if filter_fn(p)]
        return all_posts

    def upvote_post(self, post_id: str, user_id: str) -> Dict[str, Any]:
        p = self.get_post(post_id)
        if not p:
            raise KeyError('post not found')
        user = self.get_user_by_id(user_id)
        if not user:
            raise KeyError('user not found')
        username = user.get('username')
        p['downvoters'] = [u for u in p.get('downvoters', []) if u != username]
        if username not in p.get('upvoters', []):
            p.setdefault('upvoters', []).append(username)
        p['metadata']['updatedAt'] = now_iso()
        return p

    def downvote_post(self, post_id: str, user_id: str) -> Dict[str, Any]:
        p = self.get_post(post_id)
        if not p:
            raise KeyError('post not found')
        user = self.get_user_by_id(user_id)
        if not user:
            raise KeyError('user not found')
        username = user.get('username')
        p['upvoters'] = [u for u in p.get('upvoters', []) if u != username]
        if username not in p.get('downvoters', []):
            p.setdefault('downvoters', []).append(username)
        p['metadata']['updatedAt'] = now_iso()
        return p

    # Messages
    def send_message(self, from_id: str, to_id: str, text: str) -> Dict[str, Any]:
        if not from_id or not to_id:
            raise ValueError('from_id and to_id required')
        mid = self._alloc_id('msg')
        m = {'id': mid, 'from': from_id, 'to': to_id, 'text': text or '', 'createdAt': now_iso()}
        self._data['messages'][mid] = m
        return m

    def list_messages_for_user(self, user_id: str) -> List[Dict[str, Any]]:
        return [m for m in self._data['messages'].values() if m.get('from') == user_id or m.get('to') == user_id]

    # Helpers for screens
    def posts_for_screen(self, filter_fn: Optional[Callable[[Dict[str, Any]], bool]] = None) -> List[Dict[str, Any]]:
        return [self._format_post_for_screen(p) for p in self.list_posts(filter_fn)]

    def _format_post_for_screen(self, p: Dict[str, Any]) -> Dict[str, Any]:
        return {
            'id': p.get('id'),
            'account': p.get('account'),
            'title': p.get('title'),
            'body': p.get('body'),
            'files': p.get('files', []),
            'upvoters': p.get('upvoters', []),
            'downvoters': p.get('downvoters', []),
            'comments': p.get('comments', []),
            'metadata': p.get('metadata', {}),
        }

    def export_json(self) -> Dict[str, Any]:
        return json.loads(json.dumps(self._data))


if __name__ == '__main__':
    # quick smoke demo
    pb = ProfileBackend()
    u = pb.create_user('alice', 'password')
    v = pb.create_user('bob', 'hunter2')
    post = pb.create_post(u['id'], 'Hello', 'This is a test post')
    print('users:', list(pb._data['users'].keys()))
    print('posts:', list(pb._data['posts'].keys()))
