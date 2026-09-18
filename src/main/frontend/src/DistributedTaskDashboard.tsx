import { useState, useEffect } from 'react';

interface TaskResponse {
  taskId: string;
  status: string;
}

interface TaskStatusResponse {
  id: string;
  status: string;
  result?: string;
  error?: string;
}

export default function DistributedTaskDashboard() {
  const [payload, setPayload] = useState<string>('');
  const [taskId, setTaskId] = useState<string | null>(null);
  const [status, setStatus] = useState<string>('IDLE');
  const [result, setResult] = useState<string>('');
  const [error, setError] = useState<string>('');

  // Fixed TS7006: Type the form submit event
  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError('');
    setResult('');
    setStatus('SUBMITTING...');

    try {
      const response = await fetch('/api/tasks/submit', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ payload }),
      });

      if (!response.ok) throw new Error('Failed to submit task');

      const data: TaskResponse = await response.json();
      setTaskId(data.taskId);
      setStatus(data.status);
    } catch (err: unknown) {
      // Fixed TS18046: Safely narrow unknown error
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError(String(err));
      }
      setStatus('IDLE');
    }
  };

  useEffect(() => {
    // Fixed TS7034 & TS7005: Explicitly type the interval ID
    let pollingInterval: ReturnType<typeof setInterval> | undefined;

    const checkStatus = async () => {
      try {
        const response = await fetch(`/api/tasks/${taskId}/status`);
        if (response.status === 404) return;

        const data: TaskStatusResponse = await response.json();
        setStatus(data.status);

        if (data.status === 'COMPLETED') {
          setResult(data.result ?? '');
          if (pollingInterval) clearInterval(pollingInterval);
        } else if (data.status === 'FAILED') {
          setError(data.error || 'Task failed during processing');
          if (pollingInterval) clearInterval(pollingInterval);
        }
      } catch (err) {
        console.error('Polling error:', err);
      }
    };

    if (taskId && ['QUEUED', 'PENDING', 'IN_PROGRESS'].includes(status)) {
      pollingInterval = setInterval(checkStatus, 1500);
    }

    return () => {
      if (pollingInterval) clearInterval(pollingInterval);
    };
  }, [taskId, status]);

  const isWorking = ['QUEUED', 'PENDING', 'IN_PROGRESS'].includes(status);

  return (
    <div style={{ maxWidth: '500px', margin: '40px auto', fontFamily: 'sans-serif' }}>
      <h2>Online Encryption Hub</h2>

      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
        {/* Fixed TS2322: rows must be a number {4}, not string "4" */}
        <textarea
          rows={4}
          placeholder="Enter text to encrypt..."
          value={payload}
          onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => setPayload(e.target.value)}
          disabled={isWorking}
        />
        <button type="submit" disabled={!payload || isWorking}>
          Encrypt Data
        </button>
      </form>

      <div style={{ marginTop: '20px', padding: '15px', background: '#f5f5f5', borderRadius: '5px' }}>
        <p><strong>Status:</strong> {status}</p>
        {taskId && <p><strong>Task ID:</strong> {taskId}</p>}
        {error && <p style={{ color: 'red' }}><strong>Error:</strong> {error}</p>}

        {result && (
          <div>
            <strong>Encrypted Ciphertext:</strong>
            <pre style={{ background: '#333', color: '#0f0', padding: '10px', overflowX: 'auto' }}>
              {result}
            </pre>
          </div>
        )}
      </div>
    </div>
  );
}