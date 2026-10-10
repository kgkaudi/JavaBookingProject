import { useState } from "react";
import { ProDescriptions } from "@ant-design/pro-components";
import { Button, Card, Form, Input, Tag } from "antd";
import { useAuth } from "../context/AuthContext";

interface ProfileValues {
  name: string;
  phone?: string;
}

export default function Profile() {
  const { user, updateUser } = useAuth();

  // hooks must run on every render, before any early return
  const [form] = Form.useForm<ProfileValues>();
  const [saving, setSaving] = useState(false);

  if (!user) {
    return (
      <Card style={{ maxWidth: 800, margin: "0 auto" }}>
        <p>No user data available.</p>
      </Card>
    );
  }

  const handleSubmit = async (values: ProfileValues) => {
    setSaving(true);
    try {
      // only name and phone can be changed here (the backend ignores anything else)
      await updateUser({ name: values.name.trim(), phone: values.phone?.trim() });
    } finally {
      setSaving(false);
    }
  };

  return (
    <Card title="Profile" style={{ maxWidth: 800, margin: "0 auto" }}>
      {/* VIEW MODE */}
      <ProDescriptions column={1} title="User Information">
        <ProDescriptions.Item label="Full Name">{user.name || "—"}</ProDescriptions.Item>

        <ProDescriptions.Item label="Email">{user.email || "—"}</ProDescriptions.Item>

        <ProDescriptions.Item label="Roles">
          {user.roles?.length
            ? user.roles.map((r) => (
                <Tag color={r === "ROLE_ADMIN" ? "red" : "blue"} key={r}>
                  {r.replace("ROLE_", "")}
                </Tag>
              ))
            : "—"}
        </ProDescriptions.Item>

        <ProDescriptions.Item label="Phone">{user.phone || "—"}</ProDescriptions.Item>
      </ProDescriptions>

      {/* EDIT MODE */}
      <Form<ProfileValues>
        form={form}
        layout="vertical"
        initialValues={{ name: user.name, phone: user.phone }}
        onFinish={handleSubmit}
        style={{ marginTop: 32 }}
      >
        <Form.Item
          name="name"
          label="Full Name"
          rules={[{ required: true, whitespace: true, message: "Name is required" }]}
        >
          <Input placeholder="Enter your name" />
        </Form.Item>

        <Form.Item name="phone" label="Phone">
          <Input placeholder="Enter your phone number" />
        </Form.Item>

        <Button type="primary" htmlType="submit" loading={saving}>
          Update Profile
        </Button>
      </Form>
    </Card>
  );
}
