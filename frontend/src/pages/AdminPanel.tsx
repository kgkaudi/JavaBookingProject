import { Card, Button, Tag } from "antd";
import { useAuth } from "../context/AuthContext";
import { Link } from "react-router-dom";

export default function AdminPanel() {
  const { user } = useAuth();

  return (
    <div
      style={{
        padding: 24,
        display: "flex",
        justifyContent: "center",
      }}
    >
      <Card title="Admin Panel" style={{ width: 700, maxWidth: "100%" }}>
        <h2 style={{ marginBottom: 16 }}>
          Welcome, {user?.name} <Tag color="red">ADMIN</Tag>
        </h2>

        <p style={{ opacity: 0.8, marginBottom: 24 }}>
          This section is restricted to administrators. Manage rooms, bookings and users.
        </p>

        {/* Admin Tools */}
        <div style={{ marginBottom: 32 }}>
          <h3 style={{ marginBottom: 12 }}>Admin Tools</h3>

          <div style={{ display: "flex", gap: 12, flexWrap: "wrap" }}>
            <Link to="/admin/rooms">
              <Button type="primary">Manage Rooms</Button>
            </Link>

            <Link to="/admin/bookings">
              <Button type="primary">Manage Bookings</Button>
            </Link>

            <Link to="/admin/users">
              <Button type="primary">Manage Users</Button>
            </Link>
          </div>
        </div>

        {/* Navigation */}
        <div>
          <h3 style={{ marginBottom: 12 }}>Quick Navigation</h3>

          <ul style={{ lineHeight: "2rem" }}>
            <li>
              <Link to="/rooms">Browse Rooms</Link>
            </li>
            <li>
              <Link to="/bookings">View Bookings</Link>
            </li>
            <li>
              <Link to="/profile">Profile Settings</Link>
            </li>
          </ul>
        </div>
      </Card>
    </div>
  );
}
