import { ProDescriptions } from "@ant-design/pro-components";
import { Card } from "antd";

export default function Profile() {
  // In a real app, you would fetch this from your backend or AuthContext
  const user = {
    name: "Kostas",
    email: "kostas@example.com",
    phone: "+30 690 000 0000",
    role: "Admin",
    joined: "2024-01-15",
  };

  return (
    <Card title="Profile" style={{ maxWidth: 800, margin: "0 auto" }}>
      <ProDescriptions
        column={1}
        title="User Information"
        dataSource={user}
      >
        <ProDescriptions.Item label="Full Name">
          {user.name}
        </ProDescriptions.Item>

        <ProDescriptions.Item label="Email">
          {user.email}
        </ProDescriptions.Item>

        <ProDescriptions.Item label="Phone">
          {user.phone}
        </ProDescriptions.Item>

        <ProDescriptions.Item label="Role">
          {user.role}
        </ProDescriptions.Item>

        <ProDescriptions.Item label="Member Since">
          {user.joined}
        </ProDescriptions.Item>
      </ProDescriptions>
    </Card>
  );
}
