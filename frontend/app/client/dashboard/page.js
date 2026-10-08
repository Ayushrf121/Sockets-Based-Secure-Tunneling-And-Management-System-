"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import api from "@/lib/axios";
import {
    Shield,
    LogOut,
    Activity,
} from "lucide-react";

export default function ClientDashboard() {

    const router = useRouter();

    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {

        api.get("/auth/me")
            .then((response) => {

                if (response.data.role !== "CLIENT") {
                    router.push("/");
                    return;
                }

                setUser(response.data);

            })
            .catch(() => {
                router.push("/client/login");
            })
            .finally(() => {
                setLoading(false);
            });

    }, [router]);

    const logout = async () => {

        await api.post("/auth/logout");

        router.push("/client/login");
    };

    if (loading || !user) {

        return (
            <main className="min-h-screen bg-[#050505] text-white flex items-center justify-center">
                Loading...
            </main>
        );
    }

    const isActive = user.status === "ACTIVE";

    return (
        <main className="min-h-screen bg-[#050505] text-white">

            <header className="border-b border-[#242424]">

                <div className="max-w-7xl mx-auto px-6 py-5 flex justify-between items-center">

                    <div className="flex items-center gap-3">

                        <Shield className="text-green-500" />

                        <span className="font-bold">
                            SECURE PROXY
                        </span>

                    </div>

                    <button
                        onClick={logout}
                        className="flex items-center gap-2 text-gray-400 hover:text-white"
                    >
                        <LogOut size={18} />
                        Logout
                    </button>

                </div>

            </header>

            <div className="max-w-7xl mx-auto px-6 py-10">

                <h1 className="text-3xl font-bold">
                    Client Dashboard
                </h1>

                <p className="text-gray-500 mt-2">
                    Welcome, {user.name}
                </p>

                <div className="mt-8 bg-[#0D0D0D] border border-[#242424] rounded-2xl p-8">

                    <Activity
                        size={42}
                        className={
                            isActive
                                ? "text-green-500"
                                : "text-yellow-500"
                        }
                    />

                    <p className="text-gray-500 mt-6">
                        Access Status
                    </p>

                    <h2
                        className={`text-4xl font-bold mt-2 ${
                            isActive
                                ? "text-green-500"
                                : "text-yellow-500"
                        }`}
                    >
                        {user.status}
                    </h2>

                    <p className="text-gray-500 mt-4">

                        {isActive
                            ? "Your network access has been approved."
                            : "Your request is waiting for administrator approval."
                        }

                    </p>

                </div>

            </div>

        </main>
    );
}