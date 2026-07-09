import { useState } from "react";
import { Card, Input, Button, message } from "antd";
import { useSearchParams } from "react-router-dom";
import api from "../api/axios";

export default function ResetPassword() {
  const [params] = useSearchParams();
  const token = params.get("token");

  const [newPassword, setNewPassword] = useState("");
  const [loading, setLoading] = useState(false);

  const resetPassword = async () => {
    try {
      setLoading(true);
      await api.post("/api/auth/reset-password", {
        token,
        newPassword,
      });
      message.success("Password updated!");
    } catch (err) {
      message.error(err.response?.data || "Failed to reset password");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ display: "flex", justifyContent: "center", marginTop: 80 }}>
      <Card title="Reset Password" style={{ width: 350 }}>
        <Input.Password
          placeholder="New Password"
          value={newPassword}
          onChange={(e) => setNewPassword(e.target.value)}
          style={{ marginBottom: 12 }}
        />

        <Button
          type="primary"
          block
          loading={loading}
          disabled={!newPassword}
          onClick={resetPassword}
        >
          Update Password
        </Button>
      </Card>
    </div>
  );
}
