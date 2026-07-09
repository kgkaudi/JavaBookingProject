import { useState } from "react";
import { Card, Input, Button, message } from "antd";
import api from "../api/axios";

export default function ForgotPassword() {
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);

  const requestReset = async () => {
    try {
      setLoading(true);
      await api.post("/api/auth/request-reset", { email });
      message.success("Reset link sent! Check console for link.");
    } catch (err) {
      message.error(err.response?.data || "Failed to send reset link");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ display: "flex", justifyContent: "center", marginTop: 80 }}>
      <Card title="Forgot Password" style={{ width: 350 }}>
        <Input
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          style={{ marginBottom: 12 }}
        />

        <Button
          type="primary"
          block
          loading={loading}
          disabled={!email}
          onClick={requestReset}
        >
          Send Reset Link
        </Button>
      </Card>
    </div>
  );
}
