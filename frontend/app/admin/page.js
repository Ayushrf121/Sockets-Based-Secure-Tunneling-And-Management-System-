'use client';

import { useState, useEffect } from 'react';
import axios from 'axios';
import { Activity, Server, Lock, ShieldCheck, KeyRound, Trash2, Users } from 'lucide-react';
import ControlPanel from '@/components/ControlPanel';
import TerminalLogs from '@/components/TerminalLogs';
import MetricCard from '@/components/MetricCard';
import Header from '@/components/Header';
import Link from 'next/link';

export default function AdminDashboard() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [passcode, setPasscode] = useState('');
  const [authError, setAuthError] = useState(false);

  const [status, setStatus] = useState('Checking...');
  const [activeTunnels, setActiveTunnels] = useState(0);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [clients, setClients] = useState([]);
  const [logs, setLogs] = useState([
    '[SYSTEM] Protected Admin panel initialized.',
    '[SYSTEM] Awaiting management API heartbeat...'
  ]);

  const addLog = (message) => {
    const timestamp = new Date().toLocaleTimeString();
    setLogs((prev) => [...prev.slice(-12), `[${timestamp}] ${message}`]);
  };

  const handleLogin = (e) => {
    e.preventDefault();
    if (passcode === 'admin123') { // Simple demo protection passcode
      setIsAuthenticated(true);
      addLog('[SECURITY] Admin authenticated successfully.');
    } else {
      setAuthError(true);
    }
  };

  const fetchBackendData = async () => {
    if (!isAuthenticated) return;
    setIsRefreshing(true);
    try {
      const resStatus = await axios.get('http://localhost:8081/api/status');
      setStatus(resStatus.data.status);
      setActiveTunnels(resStatus.data.activeTunnels || 0);

      const resClients = await axios.get('http://localhost:8081/api/clients');
      setClients(resClients.data);
    } catch (err) {
      setStatus('Offline');
      setActiveTunnels(0);
    } finally {
      setTimeout(() => setIsRefreshing(false), 500);
    }
  };

  const handleDisconnect = async (clientId) => {
    try {
      await axios.post('http://localhost:8081/api/clients/disconnect', { clientId });
      addLog(`[WARNING] Admin forcefully revoked session for client: ${clientId}`);
      fetchBackendData();
    } catch (err) {
      addLog(`[ERROR] Failed to terminate client session.`);
    }
  };

  useEffect(() => {
    if (isAuthenticated) {
      fetchBackendData();
      const interval = setInterval(fetchBackendData, 5000);
      return () => clearInterval(interval);
    }
  }, [isAuthenticated]);

  // Authentication Guard Screen
  if (!isAuthenticated) {
    return (
      <div className="min-h-screen bg-[#020617] text-slate-100 flex items-center justify-center p-6">
        <div className="max-w-md w-full bg-slate-900/60 border border-slate-800 p-8 rounded-2xl backdrop-blur-md shadow-2xl">
          <div className="flex items-center gap-3 mb-6">
            <div className="p-3 bg-red-500/10 rounded-xl border border-red-500/20 text-red-400">
              <KeyRound className="w-6 h-6" />
            </div>
            <div>
              <h1 className="text-xl font-bold text-white">Admin Authentication</h1>
              <p className="text-xs text-slate-400">Restricted Area: Enter Admin Passcode</p>
            </div>
          </div>

          <form onSubmit={handleLogin} className="space-y-4">
            <div>
              <input 
                type="password" 
                value={passcode}
                onChange={(e) => { setPasscode(e.target.value); setAuthError(false); }}
                placeholder="Enter passcode (admin123)"
                className="w-full bg-slate-950 border border-slate-700 rounded-lg py-2.5 px-4 text-sm text-slate-200 focus:outline-none focus:border-emerald-500"
                required
              />
              {authError && <p className="text-xs text-red-400 mt-1.5">Invalid admin passcode. Try 'admin123'.</p>}
            </div>

            <button 
              type="submit"
              className="w-full bg-emerald-600 hover:bg-emerald-500 text-white font-medium py-2.5 rounded-xl transition shadow-lg shadow-emerald-600/20 text-sm"
            >
              Access Admin Dashboard
            </button>
          </form>

          <div className="mt-6 pt-4 border-t border-slate-800 text-center">
            <Link href="/" className="text-xs text-slate-400 hover:text-slate-200 transition">
              ← Return to Main Portal
            </Link>
          </div>
        </div>
      </div>
    );
  }

  // Full Admin Dashboard View
  return (
    <div className="min-h-screen bg-[#020617] text-slate-100 font-sans p-6 md:p-10 selection:bg-emerald-500/30">
      <div className="max-w-7xl mx-auto">
        
        <div className="flex justify-between items-center mb-6">
          <Link href="/" className="text-xs text-slate-400 hover:text-slate-200 transition">
            ← Back to Portal Selector
          </Link>
          <span className="text-xs font-mono bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 px-3 py-1 rounded-full">
            Role: Administrator
          </span>
        </div>

        <Header onRefresh={fetchBackendData} isRefreshing={isRefreshing} />

        {/* Top Metrics Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          <MetricCard 
            title="Backend Service"
            value={status === 'running' ? 'Operational' : 'Offline'}
            subtitle="com.rhythmcoders.tunnel"
            icon={Server}
            statusColor={status === 'running' ? 'bg-emerald-500' : 'bg-red-500'}
            delay={0.1}
          />
          <MetricCard 
            title="Active Tunnels"
            value={`${activeTunnels} Channels`}
            subtitle="Port :8080 Forwarding"
            icon={Activity}
            delay={0.2}
          />
          <MetricCard 
            title="Security Protocol"
            value="BCrypt Auth"
            subtitle="Salt-hashed Verification"
            icon={Lock}
            delay={0.3}
          />
        </div>

        {/* Control and Terminal Section */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
          <div className="lg:col-span-1">
            <ControlPanel onAction={addLog} />
          </div>
          <div className="lg:col-span-2">
            <TerminalLogs logs={logs} />
          </div>
        </div>

        {/* Live Connected Clients Management Section */}
        <div className="bg-slate-900/50 border border-slate-800 rounded-2xl p-6 backdrop-blur-md shadow-xl">
          <h2 className="text-lg font-semibold mb-4 flex items-center gap-2 text-slate-100">
            <Users className="w-5 h-5 text-emerald-400" /> Active Client Session Monitor & Revocation
          </h2>
          
          {clients.length === 0 ? (
            <p className="text-slate-500 text-sm py-6 text-center">No client sockets currently active on port 8080.</p>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {clients.map((client) => (
                <div key={client.clientId} className="flex items-center justify-between bg-slate-950 p-4 rounded-xl border border-slate-800">
                  <div>
                    <div className="text-sm font-medium text-slate-200">{client.username || 'Anonymous Client'}</div>
                    <div className="text-xs font-mono text-slate-500 truncate max-w-[180px]">ID: {client.clientId}</div>
                  </div>
                  <button 
                    onClick={() => handleDisconnect(client.clientId)}
                    className="flex items-center gap-1 px-3 py-1.5 bg-red-500/10 hover:bg-red-500/20 text-red-400 border border-red-500/30 rounded-lg text-xs transition-all"
                  >
                    <Trash2 className="w-3.5 h-3.5" /> Revoke
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>

      </div>
    </div>
  );
}