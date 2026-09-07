import { useState, useEffect } from 'react';
import { API_BASE_URL, type Status } from '../Types';
import { StatusMessage, useStatusTimer, clearPassword, refreshCursor } from '../Utils';

interface CreateAccountProps {
  setGlobalStatusLoading: React.Dispatch<React.SetStateAction<boolean>>;
}

export function CreateAccount({ setGlobalStatusLoading }: CreateAccountProps) {
  const [status, setStatus] = useState<Status>({ type: '', msg: '' });
  const [cipherModeFrontend, setCipherModeFrontend] = useState<'GCM' | 'CBC'>('GCM');
  useEffect(() => {refreshCursor();}, [status.type])

  useStatusTimer(5000, status, setStatus);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setStatus({ type: 'loading', msg: 'Creating and storing account...' });
    setGlobalStatusLoading(true);

    const form = e.currentTarget;
    const formData = new FormData(form);
    const payload = Object.fromEntries(formData.entries());

    payload.ciphermode = cipherModeFrontend;

    try {
      const response = await fetch(`${API_BASE_URL}/accounts/create`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (response.ok) {
        const accountId = await response.json();
        setStatus({ type: 'success', msg: `Account ${accountId} created successfully! Cipher mode: ${cipherModeFrontend}` });
        form.reset();
        setCipherModeFrontend('GCM');
      } else if (response.status === 409) {
        setStatus({ type: 'error', msg: 'Username is already taken.' });
      } else if (response.status === 400) {
        const errors = await response.json();
        setStatus({ type: 'error', msg: Object.values(errors).join(', ') });
      } else {
        setStatus({ type: 'error', msg: 'An unexpected error occurred.' });
      }
    } catch (err) {
      setStatus({ type: 'error', msg: 'Cannot connect to server.' });
    } finally {
      clearPassword(form);
      setGlobalStatusLoading(false);
    }
  };

  return (
    <section className="card">
      <h2>Create Account</h2>
      <form onSubmit={(e) => {
        if (status.type === 'loading') return;
        handleSubmit(e);
      }}>
        <input name="username" type="text" placeholder="Username" required />
        <input name="password" type="password" placeholder="Password" required />

        <div className="form-group">
          <label className="group-label">Select Cipher Mode:</label>
          <div className="button-group">
            <button
              type="button"
              data-loading={status.type === 'loading'}
              className={cipherModeFrontend === 'GCM' ? 'active' : ''}
              onMouseDown={(e) => e.preventDefault()}
              onClick={() => setCipherModeFrontend('GCM')}
            >
              AES-256-GCM
              <br />
              (Recommended)
            </button>
            <button
              type="button"
              data-loading={status.type === 'loading'}
              className={cipherModeFrontend === 'CBC' ? 'active' : ''}
              onMouseDown={(e) => e.preventDefault()}
              onClick={() => setCipherModeFrontend('CBC')}
            >
              AES-256-CBC
              <br />
              (Classic)
            </button>
          </div>
        </div>

        <button type="submit" data-loading={status.type === 'loading'}>
        {status.type === 'loading' ? 'Please Hold On' : 'Register Account'}
        </button>
      </form>
      <StatusMessage status={status} />
    </section>
  );
}

export function DeleteAccount({ setGlobalStatusLoading }: CreateAccountProps) {
  const [status, setStatus] = useState<Status>({ type: '', msg: '' });
  useEffect(() => {refreshCursor();}, [status.type])

  useStatusTimer(5000, status, setStatus);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!window.confirm("Are you sure? This action wipes out all your stored messages!")) return;

    setStatus({ type: 'loading', msg: 'Crushing account data...' });
    setGlobalStatusLoading(true);
    const form = e.currentTarget;
    const payload = Object.fromEntries(new FormData(form).entries());

    try {
      const response = await fetch(`${API_BASE_URL}/accounts/delete`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (response.ok) {
        const accountId = await response.json();
        setStatus({ type: 'success', msg: `Account ${accountId} blown into smithereens.` });
        form.reset();
      } else if (response.status === 401) {
        setStatus({ type: 'error', msg: 'Invalid credentials.' });
      } else {
        setStatus({ type: 'error', msg: 'Error deleting account.' });
      }
    } catch (err) {
      setStatus({ type: 'error', msg: 'Cannot connect to server.' });
    } finally {
      clearPassword(form);
      setGlobalStatusLoading(false);
    }
  };

  return (
    <section className="card danger-zone">
      <h2>Delete Account</h2>
      <form onSubmit={(e) => {
        if (status.type === 'loading') return;
        handleSubmit(e);
      }}>
        <input name="username" type="text" placeholder="Username" required />
        <input name="password" type="password" placeholder="Password" required />
        <button
            type="submit"
            className="danger-btn"
            data-loading={status.type === 'loading'}
            onMouseDown={(e) => e.preventDefault()}
            >
        {status.type === 'loading' ? 'Please Hold On' : 'Delete Forever'}
        </button>
      </form>
      <StatusMessage status={status} />
    </section>
  );
}