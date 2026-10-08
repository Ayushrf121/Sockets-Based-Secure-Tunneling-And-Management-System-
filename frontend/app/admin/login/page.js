"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import api from "@/lib/axios";
import {
    Shield,
    ArrowLeft,
    Lock,
} from "lucide-react";

export default function AdminLogin() {

    const router = useRouter();

    const [name, setName] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const login = async () => {

        setError("");
        setLoading(true);

        try {

            await api.post("/auth/admin/login", {
                name,
                password,
            });

            router.push("/admin/dashboard");

        } catch (error) {

            setError(
                error.response?.data?.error ||
                "Invalid administrator credentials"
            );

        } finally {
            setLoading(false);
        }
    };

    return (
        <main className="min-h-screen bg-[#050505] text-white flex items-center justify-center px-6">

            <div className="w-full max-w-md">

                <Link
                    href="/"
                    className="inline-flex items-center gap-2 text-gray-500 hover:text-white mb-8"
                >
                    <ArrowLeft size={18} />
                    Back
                </Link>

                <div className="bg-[#0D0D0D] border border-[#242424] rounded-2xl p-8">

                    <Shield
                        size={42}
                        className="text-green-500 mb-6"
                    />

                    <h1 className="text-2xl font-bold">
                        Administrator Login
                    </h1>

                    <p className="text-gray-500 mt-2">
                        Secure Proxy Control Panel
                    </p>

                    <input
                        value={name}
                        onChange={(e) =>
                            setName(e.target.value)
                        }
                        placeholder="Username"
                        className="w-full mt-8 bg-black border border-[#242424] rounded-lg p-3 outline-none focus:border-green-500"
                    />

                    <input
                        type="password"
                        value={password}
                        onChange={(e) =>
                            setPassword(e.target.value)
                        }
                        placeholder="Password"
                        className="w-full mt-3 bg-black border border-[#242424] rounded-lg p-3 outline-none focus:border-green-500"
                    />

                    <button
                        onClick={login}
                        disabled={loading}
                        className="w-full mt-5 bg-green-500 text-black font-semibold rounded-lg p-3 flex justify-center items-center gap-2 hover:bg-green-400 disabled:opacity-50"
                    >

                        <Lock size={18} />

                        {loading
                            ? "Authenticating..."
                            : "Sign In"}

                    </button>

                    {error && (
                        <div className="mt-5 text-red-400 bg-red-500/5 border border-red-500/20 rounded-lg p-3 text-sm">
                            {error}
                        </div>
                    )}

                </div>

            </div>

        </main>
    );
}