import { Card, Tag } from "antd";
import { useAuth } from "../context/AuthContext";
import { Link } from "react-router-dom";

export default function Dashboard() {
  const { user, isAdmin, isUser } = useAuth();

  return (
    <div
      style={{
        padding: 24,
        display: "flex",
        justifyContent: "center",
      }}
    >
      <Card
        title={`Welcome, ${user?.name || "User"}`}
        style={{ width: 600 }}
      >
        <p style={{ marginBottom: 16 }}>
          <strong>Email:</strong> {user?.email}
        </p>

        <p style={{ marginBottom: 16 }}>
          <strong>Roles:</strong>{" "}
          {user?.roles?.map((role) => (
            <Tag color="blue" key={role}>
              {role}
            </Tag>
          ))}
        </p>

        {/* Quick navigation */}
        <div style={{ marginTop: 24 }}>
          <h3 style={{ marginBottom: 12 }}>Quick Links</h3>

          <ul style={{ lineHeight: "2rem" }}>
            <li>
              <Link to="/rooms">Browse Rooms</Link>
            </li>
            <li>
              <Link to="/bookings">Your Bookings</Link>
            </li>
            <li>
              <Link to="/profile">Profile Settings</Link>
            </li>

            {isAdmin && (
              <li>
                <Link to="/admin">Admin Panel</Link>
              </li>
            )}
          </ul>
        </div>
      </Card>
    </div>
  );
}
