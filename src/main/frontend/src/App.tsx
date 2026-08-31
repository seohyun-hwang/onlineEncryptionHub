import React, { useEffect, useState } from 'react';
import './App.css';
import "@fontsource/quicksand";
import "@fontsource/quicksand/700.css";

const API_BASE_URL = 'http://localhost:8080/api';

type Tab = 'accounts' | 'messages';
type StatusType = 'success' | 'error' | 'loading' | '';

interface Status {
  type: StatusType;
  msg: string;
}


// MAIN

export default function App() {
  const [activeTab, setActiveTab] = useState<Tab>('accounts');

  return (
    <div className="container">
      <header>
        <h1>Online Encryption Hub</h1>
        <p>Fullstack Cryptography Application</p>
      </header>

      <div className="tabs">
        <button 
          onClick={() => setActiveTab('accounts')} 
          className={activeTab === 'accounts' ? 'active' : ''}
        >
          Accounts Tab
        </button>
        <button 
          onClick={() => setActiveTab('messages')} 
          className={activeTab === 'messages' ? 'active' : ''}
        >
          Messages Tab
        </button>
      </div>

      <main>
        {activeTab === 'accounts' && (
          <div className="panel-grid">
            <CreateAccount />
            <DeleteAccount />
          </div>
        )}
        {activeTab === 'messages' && (
          <div className="panel-grid">
            <CreateMessage />
            <DeleteMessage />
            <FetchMessages />
          </div>
        )}
      </main>
    </div>
  );
}

// ACCOUNT COMPONENTS

function CreateAccount() {
  const [status, setStatus] = useState<Status>({ type: '', msg: '' });

  useEffect(() => {
    if (status.msg) {
      const timer = setTimeout(() => {
        setStatus({ type: '', msg: '' });
      }, 5000);

      return () => clearTimeout(timer); 
    }
  }, [status]);

  const handleSubmit = async (e: React.SubmitEvent<HTMLFormElement>) => {
    e.preventDefault();
    setStatus({ type: 'loading', msg: 'Please wait for generation and storage of account...' });
    
    const form = e.currentTarget;
    const formData = new FormData(form);
    const payload = Object.fromEntries(formData.entries());

    try {
      const serverResponse = await fetch(`${API_BASE_URL}/accounts/create`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (serverResponse.ok) {
        setStatus({ type: 'success', msg: 'Account created successfully!' });
        form.reset();
      } else if (serverResponse.status === 409) {
        setStatus({ type: 'error', msg: 'Username is already taken.' });
      } else if (serverResponse.status === 400) {
        const errors = await serverResponse.json();
        setStatus({ type: 'error', msg: Object.values(errors).join(', ') });
      } else {
        setStatus({ type: 'error', msg: 'An unexpected error occurred.' });
      }
    } catch (err) {
      setStatus({ type: 'error', msg: 'Cannot connect to server.' });
    } finally {
      const passwordInput = form.elements.namedItem('password') as HTMLInputElement;
      if (passwordInput) passwordInput.value = ''; 
    }
  };

  return (
    <section className="card">
      <h2>Create Account</h2>
      <form onSubmit={handleSubmit}>
        <input name="username" type="text" placeholder="Username" required />
        <input name="password" type="password" placeholder="Password" required />
        <button type="submit">Register</button>
      </form>
      <StatusMessage status={status} />
    </section>
  );
}

function DeleteAccount() {
  const [status, setStatus] = useState<Status>({ type: '', msg: '' });

  useEffect(() => {
    if (status.msg) {
      const timer = setTimeout(() => {
        setStatus({ type: '', msg: '' });
      }, 5000);

      return () => clearTimeout(timer); 
    }
  }, [status]);

  const handleSubmit = async (e: React.SubmitEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!window.confirm("Are you sure? This action wipes out all your stored messages!")) return;
    
    setStatus({ type: 'loading', msg: 'Please wait for authentication...' });
    const form = e.currentTarget;
    const formData = new FormData(form);
    const payload = Object.fromEntries(formData.entries());
    
    try {
      const serverResponse = await fetch(`${API_BASE_URL}/accounts/delete`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (serverResponse.ok) {
        setStatus({ type: 'success', msg: 'All account data annihilated!!!' });
        form.reset();
      } else if (serverResponse.status === 401) {
        setStatus({ type: 'error', msg: 'Invalid credentials.' });
      } else {
        setStatus({ type: 'error', msg: 'Error deleting account.' });
      }
    } catch (err) {
      setStatus({ type: 'error', msg: 'Cannot connect to server.' });
    } finally {
      const passwordInput = form.elements.namedItem('password') as HTMLInputElement;
      if (passwordInput) passwordInput.value = '';
    }
  };

  return (
    <section className="card danger-zone">
      <h2>Delete Account</h2>
      <form onSubmit={handleSubmit}>
        <input name="username" type="text" placeholder="Username" required />
        <input name="password" type="password" placeholder="Password" required />
        <button type="submit" className="danger-btn">Delete Forever</button>
      </form>
      <StatusMessage status={status} />
    </section>
  );
}

// MESSAGE COMPONENTS

function CreateMessage() {
  const [status, setStatus] = useState<Status>({ type: '', msg: '' });

  useEffect(() => {
    if (status.msg) {
      const timer = setTimeout(() => {
        setStatus({ type: '', msg: '' });
      }, 5000);

      return () => clearTimeout(timer); 
    }
  }, [status]);

  const handleSubmit = async (e: React.SubmitEvent<HTMLFormElement>) => {
    e.preventDefault();
    setStatus({ type: 'loading', msg: 'Please wait for encryption and saving...' });
    const form = e.currentTarget;
    const formData = new FormData(form);
    const payload = Object.fromEntries(formData.entries());

    try {
      const serverResponse = await fetch(`${API_BASE_URL}/messages/create`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (serverResponse.ok) {
        setStatus({ type: 'success', msg: 'Message encrypted and stored in database!' });
        const plaintextInput = form.elements.namedItem('messagePlaintext') as HTMLTextAreaElement;
        if (plaintextInput) plaintextInput.value = ''; 
      } else if (serverResponse.status === 401) {
        setStatus({ type: 'error', msg: 'Invalid credentials.' });
      } else {
        setStatus({ type: 'error', msg: 'Error saving message.' });
      }
    } catch (err) {
      setStatus({ type: 'error', msg: 'Cannot connect to server.' });
    } finally {
      const passwordInput = form.elements.namedItem('password') as HTMLInputElement;
      if (passwordInput) passwordInput.value = '';
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
          <button type="submit">Encrypt & Save</button>
        </div>
      </form>
      <StatusMessage status={status} />
    </section>
  );
}

function FetchMessages() {
  const [status, setStatus] = useState<Status>({ type: '', msg: '' });
  const [messages, setMessages] = useState<Record<number, string>>({});

  /*
  I don't want to use a display timer for FetchMessages.

  useEffect(() => {
    if (status.msg) {
      const timer = setTimeout(() => {
        setStatus({ type: '', msg: '' });
      }, 5000);

      return () => clearTimeout(timer); 
    }
  }, [status]);
  */

  const handleSubmit = async (e: React.SubmitEvent<HTMLFormElement>) => {
    e.preventDefault();
    setStatus({ type: 'loading', msg: 'Please wait for fetching and decrypting...' });
    setMessages({});
    
    const form = e.currentTarget;
    const formData = new FormData(form);
    const payload = Object.fromEntries(formData.entries());

    try {
      const serverResponse = await fetch(`${API_BASE_URL}/messages/search`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (serverResponse.ok) {
        const data: Record<number, string> = await serverResponse.json();
        setMessages(data);
        setStatus({ type: 'success', msg: `Decrypted ${Object.keys(data).length} messages.` });
      } else if (serverResponse.status === 401) {
        setStatus({ type: 'error', msg: 'Invalid credentials.' });
      } else {
        setStatus({ type: 'error', msg: 'Error retrieving messages.' });
      }
    } catch (err) {
      setStatus({ type: 'error', msg: 'Cannot connect to server.' });
    } finally {
      const passwordInput = form.elements.namedItem('password') as HTMLInputElement;
      if (passwordInput) passwordInput.value = '';
    }
  };

  return (
    <section className="card full-width">
      <h2>View Stored Messages</h2>
      <form onSubmit={handleSubmit} className="inline-form">
        <input name="username" type="text" placeholder="Username" required />
        <input name="password" type="password" placeholder="Account Password" required />
        <button type="submit">Fetch & Decrypt</button>
      </form>
      <StatusMessage status={status} />
      
      {Object.keys(messages).length > 0 && (
        <div className="message-list">
          {Object.entries(messages).map(([id, text]) => (
            <div key={id} className="message-item">
              <span className="msg-id">ID: {id}</span>
              <p className="msg-text">{text}</p>
            </div>
          ))}
        </div>
      )}
    </section>
  );
}

function DeleteMessage() {
  const [status, setStatus] = useState<Status>({ type: '', msg: '' });

  useEffect(() => {
    if (status.msg) {
      const timer = setTimeout(() => {
        setStatus({ type: '', msg: '' });
      }, 5000);

      return () => clearTimeout(timer); 
    }
  }, [status]);

  const handleSubmit = async (e: React.SubmitEvent<HTMLFormElement>) => {
    e.preventDefault();
    
    setStatus({ type: 'loading', msg: 'Please wait for authentication...' });
    const form = e.currentTarget;
    const formData = new FormData(form);
    const payload = Object.fromEntries(formData.entries());
    
    try {
      const serverResponse = await fetch(`${API_BASE_URL}/messages/delete`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (serverResponse.ok) {
        setStatus({ type: 'success', msg: 'Message deleted forever.' });
        form.reset();
      } else if (serverResponse.status === 401) {
        setStatus({ type: 'error', msg: 'Invalid credentials.' });
      } else {
        setStatus({ type: 'error', msg: 'Error deleting message.' });
      }
    } catch (err) {
      setStatus({ type: 'error', msg: 'Cannot connect to server.' });
    } finally {
      const passwordInput = form.elements.namedItem('password') as HTMLInputElement;
      if (passwordInput) passwordInput.value = '';
    }
  };

  return (
    <section className="card danger-zone">
      <h2>Delete Message</h2>
      <form onSubmit={handleSubmit}>
        <input name="username" type="text" placeholder="Username" required />
        <input name="messageId" type="text" placeholder="Message ID" required />
        <input name="password" type="password" placeholder="Account Password" required />
        <button type="submit" className="danger-btn">Delete Forever</button>
      </form>
      <StatusMessage status={status} />
    </section>
  );
}

// UTILITY COMPONENT

interface StatusMessageProps {
  status: Status;
}

function StatusMessage({ status }: StatusMessageProps) {
  if (!status.msg) return null;
  return <div className={`status-badge ${status.type}`}>{status.msg}</div>;
}
