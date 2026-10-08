'use client';

import { useState, useEffect } from 'react';
import axios from 'axios';
import { Activity, Server, Lock } from 'lucide-react';import ControlPanel from '@/components/ControlPanel';
import TerminalLogs from '@/components/TerminalLogs';
import MetricCard from '@/components/MetricCard';
import Header from '@/components/Header';

export default function Dashboard() {
  const [status, setStatus] = useState('Checking...');
  const [activeTunnels, setActiveTunnels] = useState(0);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [logs, setLogs] = useState([
    '[SYSTEM] Boot sequence initiated...',
    '[SYSTEM] Tunnel Management API ready on port :8081',
    '[PROXY] TCP Socket listener active on port :8080'
  ]);

  const addLog = (message) => {
    const timestamp = new Date().toLocaleTimeString();
    setLogs((prev) => [...prev.slice(-12), `[${timestamp}] ${message}`]);
  };

  const fetchBackendStatus = async () => {
    setIsRefreshing(true);
    try {
      const res = await axios.get('http://localhost:8081/api/status');
      setStatus(res.data.status);
      setActiveTunnels(res.data.activeTunnels || 1);
      addLog(`[API] Status 200 OK - Backend is ${res.data.status}`);
    } catch (err) {
      setStatus('Offline');
      setActiveTunnels(0);
      addLog('[ERROR] Connection refused. Is the Java backend running?');
    } finally {
      setTimeout(() => setIsRefreshing(false), 500);
    }
  };

  useEffect(() => {
    fetchBackendStatus();
    const interval = setInterval(fetchBackendStatus, 15000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div className="min-h-screen bg-[#020617] text-slate-100 font-sans p-6 md:p-10 selection:bg-emerald-500/30">
      <div className="max-w-7xl mx-auto">
        
        <Header onRefresh={fetchBackendStatus} isRefreshing={isRefreshing} />

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

        {/* Lower Control Section */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          
          {/* 2. Use the imported ControlPanel component and pass addLog to onAction */}
          <div className="lg:col-span-1">
            <ControlPanel onAction={addLog} />
          </div>

          <div className="lg:col-span-2">
            <TerminalLogs logs={logs} />
          </div>

        </div>
      </div>
    </div>
  );
}