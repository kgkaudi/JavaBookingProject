import { ProDescriptions } from "@ant-design/pro-components";
import { Card, Tag, Form, Input, Button } from "antd";
import { useAuth } from "../context/AuthContext";

export default function Profile() {
  const { user, updateUser } = useAuth();

  const profile = user?.user || user;

  if (!profile) {
    return (
      <Card style={{ maxWidth: 800, margin: "0 auto" }}>
        <p>No user data available.</p>
      </Card>
    );
  }

  const [form] = Form.useForm();

  const handleSubmit = async (values: any) => {
    await updateUser(values);
  };

  return (
    <Card title="Profile" style={{ maxWidth: 800, margin: "0 auto" }}>
      {/* VIEW MODE */}
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

      {/* EDIT MODE */}
      <Form
        form={form}
        layout="vertical"
        initialValues={{
          name: profile.name,
          phone: profile.phone,
          email: profile.email,
        }}
        onFinish={handleSubmit}
        style={{ marginTop: 32 }}
      >
        <Form.Item
          name="name"
          label="Full Name"
          rules={[{ required: true, message: "Name is required" }]}
        >
          <Input placeholder="Enter your name" />
        </Form.Item>

        <Form.Item name="email" hidden>
          <Input type="hidden" />
        </Form.Item>

        <Form.Item name="phone" label="Phone">
          <Input placeholder="Enter your phone number" />
        </Form.Item>

        <Button type="primary" htmlType="submit">
          Update Profile
        </Button>
      </Form>
    </Card>
  );
}
