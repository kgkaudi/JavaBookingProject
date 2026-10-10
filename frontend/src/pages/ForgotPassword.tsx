import { useState } from "react";
import { Link } from "react-router-dom";
import { Alert, Button, Card, Form, Input } from "antd";
import { requestPasswordReset } from "../api/auth";
import { getErrorMessage } from "../api/errors";

interface ForgotValues {
  email: string;
}

export default function ForgotPassword() {
  const [loading, setLoading] = useState(false);
  const [sentTo, setSentTo] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const onFinish = async ({ email }: ForgotValues) => {
    setLoading(true);
    setError(null);

    try {
      await requestPasswordReset(email.trim());
      setSentTo(email.trim());
    } catch (err) {
      setError(getErrorMessage(err, "Failed to send reset link"));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ display: "flex", justifyContent: "center", marginTop: 80, padding: 16 }}>
      <Card title="Forgot Password" style={{ width: 380, maxWidth: "100%" }}>
        {sentTo ? (
          // same wording whether or not the account exists (the backend does not reveal it)
          <Alert
            type="success"
            showIcon
            message="Check your inbox"
            description={`If an account exists for ${sentTo}, a password reset link has been sent. The link is valid for 30 minutes.`}
          />
        ) : (
          <Form<ForgotValues> layout="vertical" onFinish={onFinish}>
            <Form.Item
              name="email"
              label="Email"
              rules={[
                { required: true, message: "Email is required" },
                { type: "email", message: "Invalid email format" },
              ]}
            >
              <Input placeholder="Enter your email" autoComplete="username" />
            </Form.Item>

            {error && <Alert type="error" showIcon message={error} style={{ marginBottom: 16 }} />}

            <Button type="primary" htmlType="submit" block loading={loading}>
              Send Reset Link
            </Button>
          </Form>
        )}

        <div style={{ textAlign: "center", marginTop: 16 }}>
          <Link to="/login">Back to login</Link>
        </div>
      </Card>
    </div>
  );
}
