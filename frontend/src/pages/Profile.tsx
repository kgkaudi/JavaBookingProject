import { ProDescriptions } from "@ant-design/pro-components";
import { Card, Tag } from "antd";
import { useAuth } from "../context/AuthContext";
import dayjs from "dayjs";

export default function Profile() {
  const { user } = useAuth();
  // console.log(user); // keep for debugging

  // Handle nested structure
  const profile = user?.user || user;

  if (!profile) {
    return (
      <Card style={{ maxWidth: 800, margin: "0 auto" }}>
        <p>No user data available.</p>
      </Card>
    );
  }

  return (
    <Card title="Profile" style={{ maxWidth: 800, margin: "0 auto" }}>
      <ProDescriptions column={1} title="User Information">
        <ProDescriptions.Item label="Full Name">
          {profile.name || "—"}
        </ProDescriptions.Item>

        <ProDescriptions.Item label="Email">
          {profile.email || "—"}
        </ProDescriptions.Item>

        <ProDescriptions.Item label="Roles">
          {profile.roles?.length ? (
            profile.roles.map((r) => (
              <Tag color={r === "ROLE_ADMIN" ? "red" : "blue"} key={r}>
                {r.replace("ROLE_", "")}
              </Tag>
            ))
          ) : (
            "—"
          )}
        </ProDescriptions.Item>

        <ProDescriptions.Item label="Phone">
          {profile.phone || "—"}
        </ProDescriptions.Item>
      </ProDescriptions>
    </Card>
  );
}
