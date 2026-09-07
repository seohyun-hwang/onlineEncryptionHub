import { useState, useEffect } from 'react';
import './App.css';
import "@fontsource/quicksand";
import "@fontsource/quicksand/700.css";

import type { Tab } from './Types';
import { refreshCursor } from './Utils';
import { CreateAccount, DeleteAccount } from './components/AccountComponents';
import { CreateMessage, FetchMessages, DeleteMessage } from './components/MessageComponents';
import { DeceptionTerminal } from './components/DeceptionComponents';

export default function App() {
  const [activeTab, setActiveTab] = useState<Tab>('accounts');
  const [globalStatusLoading, setGlobalStatusLoading] = useState(false);
  const [showDeceptionTerminal, setShowDeceptionTerminal] = useState(false);

  useEffect(() => {refreshCursor()}, [globalStatusLoading])

  return (
    <div className="container">
      <header>
        <h1>Online Encryption Hub</h1>
        <p>Fullstack Cryptography Application</p>
      </header>
      <div style={{ textAlign: 'center', marginBottom: '2.5rem' }}>
        <button
          onClick={(e) => {
              setShowDeceptionTerminal(!showDeceptionTerminal);
              e.currentTarget.blur();
              }}
          style={{
            background: showDeceptionTerminal ? 'rgba(239, 68, 68, 0.1)' : 'transparent',
            border: '1px dashed var(--danger)',
            color: 'var(--danger)',
            padding: '0.5rem 1rem',
            borderRadius: '6px',
            fontSize: '0.85rem',
            fontWeight: 'bold',
            cursor: 'pointer',
            marginBottom: '0.75rem',
            transition: 'all 0.2s'
          }}
        >
          {showDeceptionTerminal ? 'Close terminal' : 'LLM Honeypot Demonstration'}
        </button>
        <p style={{ fontSize: '0.8rem', color: 'gray', maxWidth: '650px', margin: '0 auto', lineHeight: '1.4' }}>
          <em><strong>Clarification:</strong> In actual production, this interactive deception terminal is completely hidden from the UI. In a real scenario, it would exist solely as an unlinked endpoint (<code>/api/admin</code>) to trap unauthorized API reconnaissance. It is exposed here purely for demonstration.</em>
        </p>
      </div>

      {showDeceptionTerminal ? (
        <DeceptionTerminal />
      ) : (
        <>
          <div className="tabs">
            <button
              data-loading={globalStatusLoading}
              disabled={globalStatusLoading}
              onClick={(e) => {
                    setActiveTab('accounts');
                    e.currentTarget.blur();
                  }}
              className={activeTab === 'accounts' ? 'active' : ''}
            >
              Accounts Tab
            </button>
            <button
              data-loading={globalStatusLoading}
              disabled={globalStatusLoading}
              onClick={(e) => {
                    setActiveTab('messages');
                    e.currentTarget.blur();
                  }}
              className={activeTab === 'messages' ? 'active' : ''}
            >
              Messages Tab
            </button>
          </div>

          <main>
            {activeTab === 'accounts' && (
              <div className="panel-grid">
                <CreateAccount setGlobalStatusLoading={setGlobalStatusLoading} />
                <DeleteAccount setGlobalStatusLoading={setGlobalStatusLoading} />
              </div>
            )}
            {activeTab === 'messages' && (
              <div className="panel-grid">
                <CreateMessage setGlobalStatusLoading={setGlobalStatusLoading} />
                <DeleteMessage setGlobalStatusLoading={setGlobalStatusLoading} />
                <FetchMessages setGlobalStatusLoading={setGlobalStatusLoading} />
              </div>
            )}
          </main>
        </>
      )}
    </div>
  );
}