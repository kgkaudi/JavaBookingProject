import { Link, useNavigate } from "react-router-dom";
import axios from "../api/axios";
import { useAuth } from "../context/AuthContext";
import { Card, message } from "antd";
import { ProForm, ProFormText } from "@ant-design/pro-components";

export default function Login() {
  const navigate = useNavigate();
  const { login } = useAuth();

  const handleLogin = async (values: any) => {
    try {
      await login(values.email, values.password);
      message.success("Login successful");
      navigate("/");
    } catch (err: any) {
      message.error(err.response?.data?.message || "Invalid email or password");
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
      <Card title="Welcome Back" style={{ width: 420 }}>
        <p style={{ textAlign: "center", opacity: 0.7, marginBottom: 20 }}>
          Login to access your dashboard
        </p>

        <ProForm
          onFinish={handleLogin}
          submitter={{
            searchConfig: {
              submitText: "Login",
              resetText: "Reset",
            },
          }}
        >
          <ProFormText
            name="email"
            label="Email"
            placeholder="Enter your email"
            rules={[
              { required: true, message: "Email is required" },
              { type: "email", message: "Invalid email format" },
            ]}
          />

          <ProFormText.Password
            name="password"
            label="Password"
            placeholder="Enter your password"
            rules={[{ required: true, message: "Password is required" }]}
          />
        </ProForm>

        <div style={{ textAlign: "center", marginTop: 16 }}>
          Don’t have an account? <Link to="/signup">Sign Up</Link>
        </div>
      </Card>
    </div>
  );
}
