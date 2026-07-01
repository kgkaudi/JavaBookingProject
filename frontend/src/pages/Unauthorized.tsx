import { Card } from "antd";
import { Link } from "react-router-dom";

export default function Unauthorized() {
  return (
    <div
      style={{
        minHeight: "100vh",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        background: "#f5f5f5",
        padding: 24,
      }}
    >
      <Card
        title="Access Denied"
        style={{ width: 420, textAlign: "center" }}
      >
        <h2 style={{ color: "#ff4d4f", marginBottom: 16 }}>
          403 — Unauthorized
        </h2>

        <p style={{ opacity: 0.8, marginBottom: 24 }}>
          You do not have permission to view this page.
        </p>

        <Link to="/" style={{ fontWeight: 500 }}>
          Go back to Home
        </Link>
      </Card>
    </div>
  );
}
