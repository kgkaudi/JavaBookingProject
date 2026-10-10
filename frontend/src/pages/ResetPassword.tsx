import { useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { Alert, Button, Card, Form, Input, message } from "antd";
import { resetPassword } from "../api/auth";
import { getErrorMessage } from "../api/errors";

interface ResetValues {
  newPassword: string;
  confirmPassword: string;
}

export default function ResetPassword() {
  const [params] = useSearchParams();
  const navigate = useNavigate();
  const token = params.get("token");

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const onFinish = async ({ newPassword }: ResetValues) => {
    if (!token) return;

    setLoading(true);
    setError(null);

    try {
      await resetPassword(token, newPassword);
      message.success("Password updated! You can now log in.");
      navigate("/login", { replace: true });
    } catch (err) {
      setError(getErrorMessage(err, "Failed to reset password"));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ display: "flex", justifyContent: "center", marginTop: 80, padding: 16 }}>
      <Card title="Reset Password" style={{ width: 380, maxWidth: "100%" }}>
        {!token ? (
          <Alert
            type="error"
            showIcon
            message="This reset link is invalid"
            description={
              <>
                The link is missing its token. <Link to="/forgot-password">Request a new one</Link>.
              </>
            }
          />
        ) : (
          <Form<ResetValues> layout="vertical" onFinish={onFinish}>
            <Form.Item
              name="newPassword"
              label="New password"
              rules={[
                { required: true, message: "Password is required" },
                { min: 8, message: "Password must be at least 8 characters" },
                { max: 72, message: "Password must be at most 72 characters" },
              ]}
            >
              <Input.Password placeholder="At least 8 characters" autoComplete="new-password" />
            </Form.Item>

            <Form.Item
              name="confirmPassword"
              label="Confirm password"
              dependencies={["newPassword"]}
              rules={[
                { required: true, message: "Please confirm your password" },
                ({ getFieldValue }) => ({
                  validator(_, value) {
                    return !value || getFieldValue("newPassword") === value
                      ? Promise.resolve()
                      : Promise.reject(new Error("Passwords do not match"));
                  },
                }),
              ]}
            >
              <Input.Password placeholder="Repeat the password" autoComplete="new-password" />
            </Form.Item>

            {error && (
              <Alert
                type="error"
                showIcon
                message={error}
                description={
                  <Link to="/forgot-password">Request a new reset link</Link>
                }
                style={{ marginBottom: 16 }}
              />
            )}

            <Button type="primary" htmlType="submit" block loading={loading}>
              Update Password
            </Button>
          </Form>
        )}
      </Card>
    </div>
  );
}
