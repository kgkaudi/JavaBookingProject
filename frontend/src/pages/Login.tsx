import { useState } from "react";
import { Link, Navigate, useLocation, useNavigate } from "react-router-dom";
import { Card, message } from "antd";
import { ProForm, ProFormText } from "@ant-design/pro-components";
import { useAuth } from "../context/AuthContext";
import { getErrorMessage } from "../api/errors";

interface LoginValues {
  email: string;
  password: string;
}

export default function Login() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login, token, user } = useAuth();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  // send the user back to the page they originally asked for
  const from = (location.state as { from?: { pathname?: string } } | null)?.from?.pathname ?? "/";

  // already logged in → nothing to do here
  if (token && user) {
    return <Navigate to={from} replace />;
  }

  const handleLogin = async (values: LoginValues) => {
    setErrorMessage(null);
    setLoading(true);

    try {
      await login(values.email.trim(), values.password);
      message.success("Login successful");
      navigate(from, { replace: true });
    } catch (err) {
      const finalMessage = getErrorMessage(err, "Invalid email or password");
      message.error(finalMessage);
      setErrorMessage(finalMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      style={{
        minHeight: "100vh",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        background: "#f5f5f5",
        padding: 24,
      }}
    >
      <Card title="Welcome Back" style={{ width: 420, maxWidth: "100%" }}>
        <p style={{ textAlign: "center", opacity: 0.7, marginBottom: 20 }}>
          Login to access your dashboard
        </p>

        <ProForm<LoginValues>
          onFinish={handleLogin}
          submitter={{
            searchConfig: {
              submitText: loading ? "Logging in..." : "Login",
              resetText: "Reset",
            },
            submitButtonProps: {
              loading,
              disabled: loading,
            },
          }}
        >
          <ProFormText
            name="email"
            label="Email"
            placeholder="Enter your email"
            fieldProps={{ autoComplete: "username" }}
            rules={[
              { required: true, message: "Email is required" },
              { type: "email", message: "Invalid email format" },
            ]}
          />

          <ProFormText.Password
            name="password"
            label="Password"
            placeholder="Enter your password"
            fieldProps={{ autoComplete: "current-password" }}
            rules={[{ required: true, message: "Password is required" }]}
          />
        </ProForm>

        {errorMessage && (
          <p
            role="alert"
            style={{
              color: "#ff4d4f",
              textAlign: "center",
              marginTop: 12,
              fontWeight: 500,
            }}
          >
            {errorMessage}
          </p>
        )}

        <div style={{ textAlign: "center", marginTop: 16 }}>
          Don’t have an account? <Link to="/signup">Sign Up</Link>
        </div>

        <div style={{ marginTop: 12, textAlign: "center" }}>
          <Link to="/forgot-password">Forgot password?</Link>
        </div>
      </Card>
    </div>
  );
}
