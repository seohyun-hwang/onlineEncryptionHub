import React, { useState } from 'react';
import { API_BASE_URL, type Status } from '../Types';
import { StatusMessage, useStatusTimer, clearPassword } from '../Utils';

export function CreateMessage() {
  const [status, setStatus] = useState<Status>({ type: '', msg: '' });

  useStatusTimer(status, setStatus);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setStatus({ type: 'loading', msg: 'Encrypting and saving...' });

    const form = e.currentTarget;
    const payload = Object.fromEntries(new FormData(form).entries());

    try {
      const response = await fetch(`${API_BASE_URL}/messages/create`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (response.ok) {
        const data = await response.json();
        setStatus({ type: 'success', msg: `Message ${data.messageId} encrypted and stored via ${data.cipherMode}.`});
        const plaintextInput = form.elements.namedItem('messagePlaintext') as HTMLTextAreaElement;
        if (plaintextInput) plaintextInput.value = '';
      } else if (response.status === 401) {
        setStatus({ type: 'error', msg: 'Invalid credentials.' });
      } else {
        setStatus({ type: 'error', msg: 'Error saving message.' });
      }
    } catch (err) {
      setStatus({ type: 'error', msg: 'Cannot connect to server.' });
    } finally {
      clearPassword(form);
    }
  };

  return (
    <section className="card">
      <h2>Store New Message</h2>
      <form onSubmit={handleSubmit}>
        <input name="username" type="text" placeholder="Username" required />
        <textarea name="messagePlaintext" placeholder="Write your secret message here..." required rows={4}></textarea>
        <div className="auth-row">
          <input name="password" type="password" placeholder="Account Password" required />
          <button type="submit" disabled={status.type === 'loading'}>
          {status.type === 'loading' ? 'Please Hold On' : 'Save Encrypted'}
          </button>
        </div>
      </form>
      <StatusMessage status={status} />
    </section>
  );
}

export function FetchMessages() {
  const [status, setStatus] = useState<Status>({ type: '', msg: '' });
  const [messages, setMessages] = useState<Record<number, string>>({});

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setStatus({ type: 'loading', msg: 'Fetching and decrypting...' });
    setMessages({});

    const form = e.currentTarget;
    const payload = Object.fromEntries(new FormData(form).entries());

    try {
      const response = await fetch(`${API_BASE_URL}/messages/search`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (response.ok) {
        const data: Record<number, string> = await response.json();
        setMessages(data);
        setStatus({ type: 'success', msg: `Decrypted ${Object.keys(data).length} messages.` });
      } else if (response.status === 401) {
        setStatus({ type: 'error', msg: 'Invalid credentials.' });
      } else {
        setStatus({ type: 'error', msg: 'Error retrieving messages.' });
      }
    } catch (err) {
      setStatus({ type: 'error', msg: 'Cannot connect to server.' });
    } finally {
      clearPassword(form);
    }
  };

  return (
    <section className="card full-width">
      <h2>View Stored Messages</h2>
      <form onSubmit={handleSubmit} className="inline-form">
        <input name="username" type="text" placeholder="Username" required />
        <input name="password" type="password" placeholder="Account Password" required />
        <button type="submit" disabled={status.type === 'loading'}>
        {status.type === 'loading' ? 'Please Hold On' : 'Fetch Decrypted'}
        </button>
      </form>
      <StatusMessage status={status} />

      {Object.keys(messages).length > 0 && (
        <div className="message-list">
          {Object.entries(messages).map(([id, text]) => (
            <div key={id} className="message-item">
              <span className="msg-id">Message ID: {id}</span>
              <p className="msg-text">{text}</p>
            </div>
          ))}
        </div>
      )}
    </section>
  );
}

export function DeleteMessage() {
  const [status, setStatus] = useState<Status>({ type: '', msg: '' });

  useStatusTimer(status, setStatus);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setStatus({ type: 'loading', msg: 'Pulverizing message data...' });

    const form = e.currentTarget;
    const payload = Object.fromEntries(new FormData(form).entries());

    if (typeof payload.messageId === 'string') {
        payload.messageId = parseInt(payload.messageId, 10) as any;
    }

    try {
      const response = await fetch(`${API_BASE_URL}/messages/delete`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (response.ok) {
        const messageId = await response.json();
        setStatus({ type: 'success', msg: `Message ${messageId} annihilated.` });
        form.reset();
      } else if (response.status === 401) {
        setStatus({ type: 'error', msg: 'Invalid credentials or missing message.' });
      } else {
        setStatus({ type: 'error', msg: 'Error deleting message.' });
      }
    } catch (err) {
      setStatus({ type: 'error', msg: 'Cannot connect to server.' });
    } finally {
      clearPassword(form);
    }
  };

  return (
    <section className="card danger-zone">
      <h2>Delete Message</h2>
      <form onSubmit={handleSubmit}>
        <input name="username" type="text" placeholder="Username" required />
        <input name="messageId" type="number" placeholder="Message ID" required />
        <input name="password" type="password" placeholder="Account Password" required />
        <button type="submit" className="danger-btn" disabled={status.type === 'loading'}>
        {status.type === 'loading' ? 'Please Hold On' : 'Delete Forever'}
        </button>
      </form>
      <StatusMessage status={status} />
    </section>
  );
}