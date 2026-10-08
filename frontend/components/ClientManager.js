'use client';

import { useState, useEffect } from 'react';
import axios from 'axios';
import { Users, UserPlus, Trash2, ShieldAlert } from 'lucide-react';

export default function ClientManager({ onAction }) {
  const [clients, setClients] = useState([]);
  const [regUser, setRegUser] = useState('');
  const [regPass, setRegPass] = useState('');

  const fetchClients = async () => {
    try {
      const res = await axios.get('http://localhost:8081/api/clients');
      setClients(res.data);
    } catch (err) {
      // Backend offline or unreachable
    }
  };

  useEffect(() => {
    fetchClients();
    const interval = setInterval(fetchClients, 5000);
    return () => clearInterval(interval);
  }, []);

  const handleRegister = async (e) => {
    e.preventDefault();
    try {
      await axios.post('http://localhost:8081/api/auth/register', { username: regUser, password: regPass });
      onAction(`[SUCCESS] Registered new client user: ${regUser}`);
      setRegUser('');
      setRegPass('');
    } catch (err) {
      onAction(`[ERROR] Failed to register client.`);
    }
  };

  const handleDisconnect = async (clientId) => {
    try {
      await axios.post('http://localhost:8081/api/clients/disconnect', { clientId });
      onAction(`[WARNING] Revoked session for client ID: ${clientId}`);
      fetchClients();
    } catch (err) {
      onAction(`[ERROR] Could not terminate client session.`);
    }
  };

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mt-8">
      
      {/* Active Clients Table */}
      <div className="bg-slate-900/50 border border-slate-800 rounded-2xl p-6 backdrop-blur-md shadow-xl">
        <h2 className="text-lg font-semibold mb-4 flex items-center gap-2 text-slate-100">
          <Users className="w-5 h-5 text-emerald-400" /> Connected Client Sessions
        </h2>
        
        {clients.length === 0 ? (
          <p className="text-slate-500 text-sm py-6 text-center">No active client sockets connected.</p>
        ) : (
          <div className="space-y-3">
            {clients.map((client) => (
              <div key={client.clientId} className="flex items-center justify-between bg-slate-950 p-3.5 rounded-xl border border-slate-800">
                <div>
                  <div className="text-sm font-medium text-slate-200">{client.username || 'Anonymous Client'}</div>
                  <div className="text-xs font-mono text-slate-500">ID: {client.clientId}</div>
                </div>
                <button 
                  onClick={() => handleDisconnect(client.clientId)}
                  className="flex items-center gap-1.5 px-3 py-1.5 bg-red-500/10 hover:bg-red-500/20 text-red-400 border border-red-500/30 rounded-lg text-xs transition-all"
                >
                  <Trash2 className="w-3.5 h-3.5" /> Revoke
                </button>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Client Registration Form */}
      <div className="bg-slate-900/50 border border-slate-800 rounded-2xl p-6 backdrop-blur-md shadow-xl">
        <h2 className="text-lg font-semibold mb-4 flex items-center gap-2 text-slate-100">
          <UserPlus className="w-5 h-5 text-indigo-400" /> Register New Client Credentials
        </h2>
        
        <form onSubmit={handleRegister} className="space-y-4">
          <div>
            <label className="block text-xs font-medium text-slate-400 mb-1 uppercase tracking-wider">Client Username</label>
            <input 
              type="text" 
              value={regUser}
              onChange={(e) => setRegUser(e.target.value)}
              placeholder="e.g., client_dev_01"
              required
              className="w-full bg-slate-950 border border-slate-700 rounded-lg py-2 px-4 text-sm text-slate-200 focus:outline-none focus:border-indigo-500"
            />
          </div>
          <div>
            <label className="block text-xs font-medium text-slate-400 mb-1 uppercase tracking-wider">Password (BCrypt Hashed)</label>
            <input 
              type="password" 
              value={regPass}
              onChange={(e) => setRegPass(e.target.value)}
              placeholder="••••••••"
              required
              className="w-full bg-slate-950 border border-slate-700 rounded-lg py-2 px-4 text-sm text-slate-200 focus:outline-none focus:border-indigo-500"
            />
          </div>
          <button 
            type="submit"
            className="w-full flex items-center justify-center gap-2 bg-indigo-600 hover:bg-indigo-500 text-white font-medium py-2.5 px-4 rounded-xl transition-all shadow-lg shadow-indigo-600/20"
          >
            Register Client
          </button>
        </form>
      </div>

    </div>
  );
}