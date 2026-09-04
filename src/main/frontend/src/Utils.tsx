import React, { useEffect } from 'react';
import type { Status } from './Types';

export function StatusMessage({ status }: { status: Status }) {
  if (!status.msg) return null;
  return <div className={`status-badge ${status.type}`}>{status.msg}</div>;
}

export function useStatusTimer(duration: number, status: Status, setStatus: React.Dispatch<React.SetStateAction<Status>>) {
  useEffect(() => {
    if (status.msg && status.type !== 'loading') {
      const timer = setTimeout(() => {
        setStatus({ type: '', msg: '' });
      }, 5000);
      return () => clearTimeout(timer);
    }
  }, [status, setStatus]);
}

export function clearPassword(form: HTMLFormElement) {
  const passwordInput = form.elements.namedItem('password') as HTMLInputElement;
  if (passwordInput) passwordInput.value = '';
}