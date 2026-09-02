export const API_BASE_URL = 'http://localhost:8080/api';

export type Tab = 'accounts' | 'messages';
export type StatusType = 'success' | 'error' | 'loading' | '';

export interface Status {
  type: StatusType;
  msg: string;
}