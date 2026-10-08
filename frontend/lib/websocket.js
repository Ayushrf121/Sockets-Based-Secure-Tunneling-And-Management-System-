import { Client } from "@stomp/stompjs";

export function connectAdminSocket(onMessage) {

    const client = new Client({
        brokerURL: "ws://localhost:8080/ws",

        reconnectDelay: 5000,

        onConnect: () => {

            client.subscribe(
                "/topic/admin",
                (message) => {
                    onMessage(message.body);
                }
            );
        },

        onStompError: (frame) => {
            console.error(
                "WebSocket error:",
                frame.headers["message"]
            );
        },
    });

    client.activate();

    return client;
}