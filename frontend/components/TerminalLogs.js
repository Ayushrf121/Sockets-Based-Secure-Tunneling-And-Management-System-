import { Terminal } from 'lucide-react';
import { motion } from 'framer-motion';

export default function TerminalLogs({ logs }) {
  return (
    <motion.div 
      initial={{ opacity: 0, x: 20 }}
      animate={{ opacity: 1, x: 0 }}
      transition={{ delay: 0.3 }}
      className="bg-[#0a0f1c] border border-slate-800 rounded-2xl overflow-hidden flex flex-col shadow-2xl h-full"
    >
      <div className="bg-slate-900/80 px-5 py-4 border-b border-slate-800 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <Terminal className="w-4 h-4 text-emerald-400" />
          <span className="text-xs font-mono font-semibold text-slate-300">SYSTEM.LOG</span>
        </div>
        <div className="flex gap-2">
          <span className="w-3 h-3 rounded-full bg-red-500/20 border border-red-500/50" />
          <span className="w-3 h-3 rounded-full bg-yellow-500/20 border border-yellow-500/50" />
          <span className="w-3 h-3 rounded-full bg-emerald-500/20 border border-emerald-500/50" />
        </div>
      </div>

      <div className="p-5 font-mono text-xs text-emerald-400/90 h-80 overflow-y-auto space-y-3">
        {logs.map((log, index) => (
          <motion.div 
            key={index}
            initial={{ opacity: 0, x: -10 }}
            animate={{ opacity: 1, x: 0 }}
            className="leading-relaxed border-l-2 border-emerald-500/30 pl-3"
          >
            {log}
          </motion.div>
        ))}
      </div>
    </motion.div>
  );
}