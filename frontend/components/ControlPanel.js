import { useState } from 'react';
import axios from 'axios';
import { Server, Play, Square, Network } from 'lucide-react';
import { motion } from 'framer-motion';

export default function ControlPanel({ onAction }) {
  const [host, setHost] = useState('example.com');
  const [port, setPort] = useState(80);
  const [isLoading, setIsLoading] = useState(false);

  const handleStart = async () => {
    setIsLoading(true);
    onAction(`[ACTION] Dispatching START command -> ${host}:${port}`);
    try {
      await axios.post('http://localhost:8081/api/start', { host, port: parseInt(port) });
      onAction(`[SUCCESS] Proxy listening on :8080 forwarding to ${host}:${port}`);
    } catch (err) {
      onAction(`[ERROR] Failed to start tunnel. Check Java backend.`);
    }
    setIsLoading(false);
  };

  const handleStop = async () => {
    setIsLoading(true);
    onAction('[ACTION] Dispatching STOP command...');
    try {
      await axios.post('http://localhost:8081/api/stop');
      onAction('[SUCCESS] All active proxy sockets terminated.');
    } catch (err) {
      onAction('[ERROR] Failed to stop tunnel.');
    }
    setIsLoading(false);
  };

  return (
    <motion.div 
      initial={{ opacity: 0, x: -20 }}
      animate={{ opacity: 1, x: 0 }}
      transition={{ delay: 0.3 }}
      className="bg-slate-900/50 border border-slate-800 rounded-2xl p-6 backdrop-blur-md"
    >
      <h2 className="text-lg font-semibold mb-2 flex items-center gap-2 text-slate-100">
        <Server className="w-5 h-5 text-emerald-400" /> Tunnel Configuration
      </h2>
      <p className="text-slate-400 text-sm mb-6">
        Specify the destination server. Traffic entering localhost:8080 will be routed here.
      </p>

      {/* Dynamic Input Fields */}
      <div className="space-y-4 mb-8">
        <div>
          <label className="block text-xs font-medium text-slate-400 mb-1.5 uppercase tracking-wider">Target Host / IP</label>
          <div className="relative">
            <Network className="w-4 h-4 text-slate-500 absolute left-3 top-3" />
            <input 
              type="text" 
              value={host}
              onChange={(e) => setHost(e.target.value)}
              className="w-full bg-slate-950 border border-slate-700 rounded-lg py-2 pl-9 pr-4 text-sm text-slate-200 focus:outline-none focus:border-emerald-500 focus:ring-1 focus:ring-emerald-500 transition-all"
              placeholder="e.g., 93.184.215.14"
            />
          </div>
        </div>
        <div>
          <label className="block text-xs font-medium text-slate-400 mb-1.5 uppercase tracking-wider">Target Port</label>
          <input 
            type="number" 
            value={port}
            onChange={(e) => setPort(e.target.value)}
            className="w-full bg-slate-950 border border-slate-700 rounded-lg py-2 px-4 text-sm text-slate-200 focus:outline-none focus:border-emerald-500 focus:ring-1 focus:ring-emerald-500 transition-all"
            placeholder="80"
          />
        </div>
      </div>

      <div className="space-y-3 border-t border-slate-800/80 pt-6">
        <button 
          onClick={handleStart}
          disabled={isLoading}
          className="w-full flex items-center justify-center gap-2 bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 font-medium py-2.5 px-4 rounded-xl transition-all active:scale-95 disabled:opacity-50"
        >
          <Play className="w-4 h-4" /> Initialize Tunnel
        </button>

        <button 
          onClick={handleStop}
          disabled={isLoading}
          className="w-full flex items-center justify-center gap-2 bg-red-500/5 hover:bg-red-500/10 text-red-400 border border-red-500/20 font-medium py-2.5 px-4 rounded-xl transition-all active:scale-95 disabled:opacity-50"
        >
          <Square className="w-4 h-4" /> Drop Connections
        </button>
      </div>
    </motion.div>
  );
}