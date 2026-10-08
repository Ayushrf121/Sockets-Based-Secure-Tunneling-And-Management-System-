import { ShieldCheck, RefreshCw } from 'lucide-react';
import { motion } from 'framer-motion';

export default function Header({ onRefresh, isRefreshing }) {
  return (
    <motion.header 
      initial={{ opacity: 0, y: -20 }}
      animate={{ opacity: 1, y: 0 }}
      className="flex flex-col md:flex-row md:items-center justify-between border-b border-slate-800 pb-6 mb-8 gap-4"
    >
      <div className="flex items-center gap-4">
        <div className="p-3 bg-emerald-500/10 rounded-xl border border-emerald-500/20 shadow-[0_0_15px_rgba(16,185,129,0.15)]">
          <ShieldCheck className="w-8 h-8 text-emerald-400" />
        </div>
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-white">
            Smart Tunnel Admin
          </h1>
          <p className="text-slate-400 text-sm mt-1">
            Rhythm Coders • Secure Application-Level Proxy
          </p>
        </div>
      </div>

      <button 
        onClick={onRefresh}
        disabled={isRefreshing}
        className="flex items-center gap-2 px-5 py-2.5 text-sm font-medium bg-slate-900 hover:bg-slate-800 text-slate-200 border border-slate-700 rounded-lg transition-all active:scale-95"
      >
        <RefreshCw className={`w-4 h-4 ${isRefreshing ? 'animate-spin text-emerald-400' : 'text-slate-400'}`} />
        Sync Status
      </button>
    </motion.header>
  );
}