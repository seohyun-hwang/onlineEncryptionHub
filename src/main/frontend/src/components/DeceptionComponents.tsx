import { useState, useEffect } from 'react';
import { API_BASE_URL, type Status } from '../Types';
import { StatusMessage, useStatusTimer, refreshCursor } from '../Utils';

export function DeceptionTerminal() {
  const [commandInput, setCommandInput] = useState('');
  const [terminalHistory, setTerminalHistory] = useState<Array<{ sender: 'user' | 'system'; text: string }>>([
    { sender: 'system', text: 'Welcome to the onsite server terminal reserved for administrative personnel.' },
    { sender: 'system', text: 'Type a shell command or query; an AI agent is here to help you.' }
  ]);
  const [status, setStatus] = useState<Status>({ type: '', msg: '' });
  const [loading, setLoading] = useState(false);
  useEffect(() => {refreshCursor();}, [status.type])

  useStatusTimer(5000, status, setStatus);

  const handleCommandSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!commandInput.trim() || loading) return;

    const userCmd = commandInput;
    setCommandInput('');
    setTerminalHistory(prev => [...prev, { sender: 'user', text: `$ ${userCmd}` }]);
    setLoading(true);

    try {
      const response = await fetch(`${API_BASE_URL}/deception/admin`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ command: userCmd })
      });

      if (response.ok) {
        const data = await response.json();
        setTerminalHistory(prev => [...prev, { sender: 'system', text: data.response }]);
      } else {
        setTerminalHistory(prev => [...prev, { sender: 'system', text: 'Connection refused by remote host.' }]);
      }
    } catch (err) {
      setTerminalHistory(prev => [...prev, { sender: 'system', text: 'Network error communicating with server node.' }]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="card full-width danger-zone">
      <h2>Restricted Onsite AI-supported Linux BASH Terminal</h2>
      <p style={{ color: 'gray', fontSize: '0.85rem', marginBottom: '1rem' }}>
        This is a honeypot environment intended to log all interactions for analysis.
        It implements defenses against LLM prompt injections.
      </p>

      <div className="terminal-window" style={{
        background: '#090d16',
        border: '1px solid var(--border-color)',
        borderRadius: '8px',
        padding: '1rem',
        height: '300px',
        overflowY: 'auto',
        fontFamily: 'monospace',
        fontSize: '0.9rem',
        marginBottom: '1rem',
        display: 'flex',
        flexDirection: 'column',
        gap: '0.5rem'
      }}>
        {terminalHistory.map((entry, idx) => (
          <div key={idx} style={{ color: entry.sender === 'user' ? '#38bdf8' : '#34d399', whiteSpace: 'pre-wrap' }}>
            {entry.text}
          </div>
        ))}
        {loading && <div style={{ color: '#fbbf24' }}>[Processing demand...]</div>}
      </div>

      <form onSubmit={handleCommandSubmit} className="inline-form">
        <input
          type="text"
          value={commandInput}
          onChange={e => setCommandInput(e.target.value)}
          placeholder="Enter command here..."
          data-loading={loading}
        />
        <button
            type="submit"
            data-loading={loading}
            onMouseDown={(e) => e.preventDefault()}
            >
          Execute
        </button>
      </form>
      <StatusMessage status={status} />
    </section>
  );
}