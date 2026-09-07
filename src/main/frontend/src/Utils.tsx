import { useEffect } from 'react';
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
      }, duration);
      return () => clearTimeout(timer);
    }
  }, [status, setStatus]);
}

export function refreshCursor() {
  if (document.activeElement instanceof HTMLElement) {
      document.activeElement.blur();
    }
  const event = new MouseEvent('mousemove', { bubbles: true, cancelable: true });
  window.dispatchEvent(event);
}

export function clearPassword(form: HTMLFormElement) {
  const passwordInput = form.elements.namedItem('password') as HTMLInputElement;
  if (passwordInput) passwordInput.value = '';
}