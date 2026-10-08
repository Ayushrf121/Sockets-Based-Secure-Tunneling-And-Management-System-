import "./globals.css";

export const metadata = {
    title: "Secure Proxy",
    description: "Secure Proxy Access Management",
};

export default function RootLayout({ children }) {

    return (
        <html lang="en">
            <body>
                {children}
            </body>
        </html>
    );
}