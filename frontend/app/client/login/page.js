"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import api from "@/lib/axios";
import {
    Shield,
    ArrowLeft,
    LogIn,
    UserPlus,
} from "lucide-react";

export default function ClientLogin() {

    const router = useRouter();

    const [name, setName] = useState("");
    const [password, setPassword] = useState("");

    const [message, setMessage] = useState("");
    const [error, setError] = useState("");

    const [loading, setLoading] = useState(false);

    const login = async () => {

        setError("");
        setMessage("");
        setLoading(true);

        try {

            await api.post("/auth/client/login", {
                name,
                password,
            });

            router.push("/client/dashboard");

        } catch (error) {

            setError(
                error.response?.data?.error ||
                "Login failed"
            );

        } finally {
            setLoading(false);
        }
    };

    const requestAccess = async () => {

        setError("");
        setMessage("");
        setLoading(true);

        try {

            await api.post("/client/request-access", {
                name,
                password,
            });

            setMessage(
                "Access request submitted. Wait for administrator approval."
            );

        } catch (error) {

            setError(
                error.response?.data?.error ||
                "Unable to submit request"
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

                    <div className="flex items-center gap-3 mb-6">

                        <Shield
                            size={38}
                            className="text-green-500"
                        />

                        <div>
                            <h1 className="text-2xl font-bold">
                                Client Access
                            </h1>

                            <p className="text-gray-500 text-sm">
                                Secure network access
                            </p>
                        </div>

                    </div>

                    <label className="text-sm text-gray-400">
                        Name
                    </label>

                    <input
                        value={name}
                        onChange={(e) =>
                            setName(e.target.value)
                        }
                        placeholder="Enter your name"
                        className="w-full mt-2 mb-5 bg-black border border-[#242424] rounded-lg p-3 outline-none focus:border-green-500"
                    />

                    <label className="text-sm text-gray-400">
                        Password
                    </label>

                    <input
                        type="password"
                        value={password}
                        onChange={(e) =>
                            setPassword(e.target.value)
                        }
                        placeholder="Enter password"
                        className="w-full mt-2 mb-6 bg-black border border-[#242424] rounded-lg p-3 outline-none focus:border-green-500"
                    />

                    <button
                        onClick={login}
                        disabled={loading}
                        className="w-full bg-green-500 text-black font-semibold rounded-lg p-3 flex justify-center items-center gap-2 hover:bg-green-400 disabled:opacity-50"
                    >

                        <LogIn size={18} />

                        {loading
                            ? "Processing..."
                            : "Login"}

                    </button>

                    <button
                        onClick={requestAccess}
                        disabled={loading}
                        className="w-full mt-3 border border-[#333] rounded-lg p-3 flex justify-center items-center gap-2 hover:border-green-500 disabled:opacity-50"
                    >

                        <UserPlus size={18} />

                        Request Access

                    </button>

                    {message && (
                        <div className="mt-5 border border-green-500/20 bg-green-500/5 text-green-400 p-3 rounded-lg text-sm">
                            {message}
                        </div>
                    )}

                    {error && (
                        <div className="mt-5 border border-red-500/20 bg-red-500/5 text-red-400 p-3 rounded-lg text-sm">
                            {error}
                        </div>
                    )}

                </div>

            </div>

        </main>
    );
}