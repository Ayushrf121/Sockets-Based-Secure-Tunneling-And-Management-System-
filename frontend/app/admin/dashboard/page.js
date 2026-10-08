"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import api from "@/lib/axios";
import { connectAdminSocket } from "@/lib/websocket";

import {
    Shield,
    Users,
    Activity,
    Clock,
    Check,
    X,
    Trash2,
    LogOut,
    Bell,
} from "lucide-react";

export default function AdminDashboard() {

    const router = useRouter();

    const [stats, setStats] = useState({
        totalClients: 0,
        activeClients: 0,
        pendingClients: 0,
    });

    const [clients, setClients] = useState([]);
    const [notification, setNotification] = useState("");

    const loadDashboard = async () => {

        try {

            const me =
                await api.get("/auth/me");

            if (me.data.role !== "ADMIN") {
                router.push("/");
                return;
            }

            const [dashboard, pending] =
                await Promise.all([
                    api.get("/admin/dashboard"),
                    api.get("/admin/clients/pending"),
                ]);

            setStats(dashboard.data);
            setClients(pending.data);

        } catch {

            router.push("/admin/login");
        }
    };

    useEffect(() => {

        loadDashboard();

        const socket = connectAdminSocket(
            (message) => {

                setNotification(message);

                loadDashboard();

                setTimeout(() => {
                    setNotification("");
                }, 5000);
            }
        );

        return () => {
            socket.deactivate();
        };

    }, []);

    const approve = async (id) => {

        await api.post(
            `/admin/clients/${id}/approve`
        );

        loadDashboard();
    };

    const reject = async (id) => {

        await api.post(
            `/admin/clients/${id}/reject`
        );

        loadDashboard();
    };

    const remove = async (id) => {

        await api.delete(
            `/admin/clients/${id}`
        );

        loadDashboard();
    };

    const logout = async () => {

        await api.post("/auth/logout");

        router.push("/admin/login");
    };

    return (
        <main className="min-h-screen bg-[#050505] text-white">

            {/* Header */}

            <header className="border-b border-[#242424]">

                <div className="max-w-7xl mx-auto px-6 py-5 flex justify-between items-center">

                    <div className="flex items-center gap-3">

                        <Shield
                            className="text-green-500"
                            size={28}
                        />

                        <div>
                            <p className="font-bold">
                                SECURE PROXY
                            </p>

                            <p className="text-xs text-gray-600">
                                ADMIN CONSOLE
                            </p>
                        </div>

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

            {/* Notification */}

            {notification && (

                <div className="fixed top-6 right-6 z-50 bg-[#0D0D0D] border border-green-500/40 rounded-xl px-5 py-4 shadow-xl">

                    <div className="flex gap-3 items-center">

                        <Bell
                            size={20}
                            className="text-green-500"
                        />

                        <span>
                            {notification}
                        </span>

                    </div>

                </div>
            )}

            <div className="max-w-7xl mx-auto px-6 py-10">

                <div className="mb-8">

                    <h1 className="text-3xl font-bold">
                        Dashboard
                    </h1>

                    <p className="text-gray-500 mt-1">
                        Network access management
                    </p>

                </div>

                {/* Stats */}

                <div className="grid md:grid-cols-3 gap-5">

                    <StatCard
                        icon={<Users />}
                        title="Total Clients"
                        value={stats.totalClients}
                    />

                    <StatCard
                        icon={<Activity />}
                        title="Active Clients"
                        value={stats.activeClients}
                    />

                    <StatCard
                        icon={<Clock />}
                        title="Pending Requests"
                        value={stats.pendingClients}
                    />

                </div>

                {/* Pending */}

                <section className="mt-8 bg-[#0D0D0D] border border-[#242424] rounded-2xl p-6">

                    <div className="flex items-center justify-between mb-6">

                        <div>
                            <h2 className="text-xl font-semibold">
                                Access Requests
                            </h2>

                            <p className="text-gray-500 text-sm mt-1">
                                Clients waiting for approval
                            </p>
                        </div>

                        <span className="bg-yellow-500/10 text-yellow-500 px-3 py-1 rounded-full text-sm">
                            {clients.length} Pending
                        </span>

                    </div>

                    {clients.length === 0 ? (

                        <div className="border border-dashed border-[#333] rounded-xl p-10 text-center text-gray-600">
                            No pending access requests.
                        </div>

                    ) : (

                        <div className="space-y-3">

                            {clients.map((client) => (

                                <div
                                    key={client.id}
                                    className="border border-[#242424] rounded-xl p-4 flex flex-col md:flex-row md:items-center md:justify-between gap-4"
                                >

                                    <div>

                                        <p className="font-semibold">
                                            {client.name}
                                        </p>

                                        <p className="text-sm text-yellow-500 mt-1">
                                            {client.status}
                                        </p>

                                    </div>

                                    <div className="flex gap-2">

                                        <button
                                            onClick={() =>
                                                approve(client.id)
                                            }
                                            className="bg-green-500 text-black p-2.5 rounded-lg hover:bg-green-400"
                                            title="Approve"
                                        >
                                            <Check size={18} />
                                        </button>

                                        <button
                                            onClick={() =>
                                                reject(client.id)
                                            }
                                            className="bg-red-500 text-white p-2.5 rounded-lg hover:bg-red-400"
                                            title="Reject"
                                        >
                                            <X size={18} />
                                        </button>

                                        <button
                                            onClick={() =>
                                                remove(client.id)
                                            }
                                            className="border border-[#333] p-2.5 rounded-lg hover:border-red-500 hover:text-red-500"
                                            title="Remove"
                                        >
                                            <Trash2 size={18} />
                                        </button>

                                    </div>

                                </div>

                            ))}

                        </div>

                    )}

                </section>

            </div>

        </main>
    );
}

function StatCard({
    icon,
    title,
    value,
}) {

    return (
        <div className="bg-[#0D0D0D] border border-[#242424] rounded-2xl p-6">

            <div className="text-green-500 mb-5">
                {icon}
            </div>

            <p className="text-gray-500">
                {title}
            </p>

            <p className="text-3xl font-bold mt-2">
                {value}
            </p>

        </div>
    );
}