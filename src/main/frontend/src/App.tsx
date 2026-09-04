import React, { useState } from 'react';
import './App.css';
import "@fontsource/quicksand";
import "@fontsource/quicksand/700.css";

import type { Tab, StatusType } from './Types';
import { CreateAccount, DeleteAccount } from './components/AccountComponents';
import { CreateMessage, FetchMessages, DeleteMessage } from './components/MessageComponents';

export default function App() {
  const [activeTab, setActiveTab] = useState<Tab>('accounts');
  const [globalStatusLoading, setGlobalStatusLoading] = useState(false);

  return (
    <div className="container">
      <header>
        <h1>Online Encryption Hub</h1>
        <p>Fullstack Cryptography Application</p>
      </header>

      <div className="tabs">
        <button
          disabled={globalStatusLoading}
          onClick={() => setActiveTab('accounts')}
          className={activeTab === 'accounts' ? 'active' : ''}
        >
          Accounts Tab
        </button>
        <button
          disabled={globalStatusLoading}
          onClick={() => setActiveTab('messages')}
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
    </div>
  );
}