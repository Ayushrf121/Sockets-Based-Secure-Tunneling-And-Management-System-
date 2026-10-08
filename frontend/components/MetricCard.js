import { motion } from 'framer-motion';

export default function MetricCard({ title, value, subtitle, icon: Icon, statusColor, delay = 0 }) {
  return (
    <motion.div 
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay }}
      whileHover={{ y: -5 }}
      className="bg-slate-900/50 border border-slate-800 p-6 rounded-2xl backdrop-blur-md shadow-xl relative overflow-hidden group"
    >
      <div className="absolute inset-0 bg-gradient-to-br from-white/[0.02] to-transparent opacity-0 group-hover:opacity-100 transition-opacity" />
      
      <div className="flex items-center justify-between mb-4">
        <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">{title}</span>
        {Icon && <Icon className="w-5 h-5 text-slate-500 group-hover:text-slate-300 transition-colors" />}
      </div>
      
      <div className="flex items-center gap-3">
        {statusColor && (
          <span className="relative flex h-3.5 w-3.5">
            {statusColor === 'bg-emerald-500' && (
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
            )}
            <span className={`relative inline-flex rounded-full h-3.5 w-3.5 ${statusColor}`}></span>
          </span>
        )}
        <span className="text-2xl font-bold text-white tracking-wide">{value}</span>
      </div>
      <p className="text-xs text-slate-500 mt-2">{subtitle}</p>
    </motion.div>
  );
}