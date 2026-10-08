'use client';

import { useState } from 'react';
import axios from 'axios';
import { UserPlus, ShieldCheck, CheckCircle2 } from 'lucide-react';
import Link from 'next/link';

export default function ClientPortal() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [statusMsg, setStatusMsg] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  const handleRegister = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setStatusMsg(null);

    try {
      const res = await axios.post('http://localhost:8081/api/auth/register', { username, password });
      if (res.data.success) {
        setStatusMsg({ type: 'success', text: `Successfully registered client account: ${username}` });
        setUsername('');
        setPassword('');
      } else {
        setStatusMsg({ type: 'error', text: 'Username already taken. Choose another.' });
      }
    } catch (err) {
      setStatusMsg({ type: 'error', text: 'Failed to connect to backend server on port 8081.' });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#020617] text-slate-100 flex items-center justify-center p-6">
      <div className="max-w-md w-full">
        
        <div className="mb-6">
          <Link href="/" className="text-xs text-slate-400 hover:text-slate-200 transition">
            ← Return to Main Portal
          </Link>
        </div>

        <div className="bg-slate-900/60 border border-slate-800 p-8 rounded-2xl backdrop-blur-md shadow-2xl">
          <div className="flex items-center gap-3 mb-6">
            <div className="p-3 bg-indigo-500/10 rounded-xl border border-indigo-500/20 text-indigo-400">
              <UserPlus className="w-6 h-6" />
            </div>
            <div>
              <h1 className="text-xl font-bold text-white">Client Registration</h1>
              <p className="text-xs text-slate-400">Join the Rhythm Coders Proxy Network</p>
            </div>
          </div>

          {statusMsg && (
            <div className={`mb-6 p-3.5 rounded-xl text-xs border flex items-center gap-2 ${
              statusMsg.type === 'success' 
                ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-400' 
                : 'bg-red-500/10 border-red-500/30 text-red-400'
            }`}>
              <CheckCircle2 className="w-4 h-4 shrink-0" />
              <span>{statusMsg.text}</span>
            </div>
          )}

          <form onSubmit={handleRegister} className="space-y-4">
            <div>
              <label className="block text-xs font-medium text-slate-400 mb-1 uppercase tracking-wider">Client Username</label>
              <input 
                type="text" 
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="e.g., student_client_01"
                required
                className="w-full bg-slate-950 border border-slate-700 rounded-lg py-2.5 px-4 text-sm text-slate-200 focus:outline-none focus:border-indigo-500"
              />
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-400 mb-1 uppercase tracking-wider">Password</label>
              <input 
                type="password" 
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                required
                className="w-full bg-slate-950 border border-slate-700 rounded-lg py-2.5 px-4 text-sm text-slate-200 focus:outline-none focus:border-indigo-500"
              />
              <p className="text-[11px] text-slate-500 mt-1">Secured via server-side BCrypt hashing.</p>
            </div>

            <button 
              type="submit"
              disabled={isLoading}
              className="w-full bg-indigo-600 hover:bg-indigo-500 text-white font-medium py-2.5 rounded-xl transition shadow-lg shadow-indigo-600/20 text-sm disabled:opacity-50"
            >
              {isLoading ? 'Registering...' : 'Register Account'}
            </button>
          </form>

        </div>

      </div>
    </div>
  );
}