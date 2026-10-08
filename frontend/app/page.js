'use client';

import { Shield, UserCheck, ArrowRight } from 'lucide-react';
import { motion } from 'framer-motion';
import Link from 'next/link';

export default function HomePortal() {
  return (
    <div className="min-h-screen bg-[#020617] text-slate-100 flex items-center justify-center p-6">
      <div className="max-w-4xl w-full">
        
        <div className="text-center mb-12">
          <h1 className="text-4xl font-bold tracking-tight text-white mb-3">
            Rhythm Coders Secure Tunnel
          </h1>
          <p className="text-slate-400 text-sm">
            Application-Level TCP Proxy & Access Control System
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          
          {/* Admin Portal Card */}
          <Link href="/admin">
            <motion.div 
              whileHover={{ y: -5 }}
              className="bg-slate-900/60 border border-slate-800 hover:border-emerald-500/50 p-8 rounded-2xl backdrop-blur-md shadow-xl cursor-pointer group transition-all"
            >
              <div className="p-3 bg-emerald-500/10 rounded-xl border border-emerald-500/20 w-fit mb-6 text-emerald-400">
                <Shield className="w-8 h-8" />
              </div>
              <h2 className="text-xl font-bold text-white mb-2 flex items-center justify-between">
                Admin Dashboard 
                <ArrowRight className="w-5 h-5 text-slate-500 group-hover:text-emerald-400 group-hover:translate-x-1 transition-all" />
              </h2>
              <p className="text-slate-400 text-sm leading-relaxed">
                Manage live proxy streams, control server directives, inspect system logs, and monitor or revoke active client connections.
              </p>
            </motion.div>
          </Link>

          {/* Client Portal Card */}
          <Link href="/client">
            <motion.div 
              whileHover={{ y: -5 }}
              className="bg-slate-900/60 border border-slate-800 hover:border-indigo-500/50 p-8 rounded-2xl backdrop-blur-md shadow-xl cursor-pointer group transition-all"
            >
              <div className="p-3 bg-indigo-500/10 rounded-xl border border-indigo-500/20 w-fit mb-6 text-indigo-400">
                <UserCheck className="w-8 h-8" />
              </div>
              <h2 className="text-xl font-bold text-white mb-2 flex items-center justify-between">
                Client Registration 
                <ArrowRight className="w-5 h-5 text-slate-500 group-hover:text-indigo-400 group-hover:translate-x-1 transition-all" />
              </h2>
              <p className="text-slate-400 text-sm leading-relaxed">
                Register new client accounts with BCrypt-hashed credentials to gain authorized access to the proxy routing network.
              </p>
            </motion.div>
          </Link>

        </div>

      </div>
    </div>
  );
}