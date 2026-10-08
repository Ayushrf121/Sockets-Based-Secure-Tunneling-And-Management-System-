"use client";

import Link from "next/link";
import {
    Shield,
    User,
    Lock,
    ArrowRight,
} from "lucide-react";

export default function page() {

    return (
        <main className="min-h-screen bg-[#050505] text-white">

            {/* Navbar */}

            <nav className="border-b border-[#242424]">
                <div className="max-w-7xl mx-auto px-6 py-5 flex justify-between items-center">

                    <div className="flex items-center gap-3">
                        <Shield
                            size={28}
                            className="text-green-500"
                        />

                        <span className="font-bold tracking-wide">
                            SECURE PROXY
                        </span>
                    </div>

                    <Link
                        href="/admin/login"
                        className="text-sm text-gray-400 hover:text-white transition"
                    >
                        Admin
                    </Link>

                </div>
            </nav>

            {/* Hero */}

            <section className="max-w-7xl mx-auto px-6 py-24 text-center">

                <div className="inline-flex items-center gap-2 border border-green-500/20 bg-green-500/5 rounded-full px-4 py-2 text-sm text-green-400 mb-8">

                    <Shield size={16} />

                    Secure Network Access

                </div>

                <h1 className="text-5xl md:text-7xl font-bold tracking-tight">

                    Secure Access.
                    <br />

                    <span className="text-green-500">
                        Controlled Network.
                    </span>

                </h1>

                <p className="max-w-2xl mx-auto mt-6 text-gray-500 text-lg">

                    A secure proxy access management system
                    for controlling and monitoring network clients.

                </p>

            </section>

            {/* Options */}

            <section className="max-w-5xl mx-auto px-6 pb-24">

                <div className="grid md:grid-cols-2 gap-6">

                    <AccessCard
                        icon={<User size={38} />}
                        title="Client Access"
                        description="Request network access and monitor your approval status."
                        href="/client/login"
                    />

                    <AccessCard
                        icon={<Lock size={38} />}
                        title="Administrator"
                        description="Manage clients, access requests and network activity."
                        href="/admin/login"
                    />

                </div>

            </section>

        </main>
    );
}

function AccessCard({
    icon,
    title,
    description,
    href,
}) {

    return (
        <Link
            href={href}
            className="group bg-[#0D0D0D] border border-[#242424] rounded-2xl p-8 hover:border-green-500/50 transition"
        >

            <div className="flex justify-between items-start">

                <div className="text-green-500">
                    {icon}
                </div>

                <ArrowRight
                    className="text-gray-600 group-hover:text-green-500 group-hover:translate-x-1 transition"
                />

            </div>

            <h2 className="text-2xl font-semibold mt-8">
                {title}
            </h2>

            <p className="text-gray-500 mt-3">
                {description}
            </p>

        </Link>
    );
}