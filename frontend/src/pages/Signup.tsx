import { Link, useNavigate } from "react-router-dom";
import axios from "../api/axios";
import { Card, message } from "antd";
import { ProForm, ProFormText } from "@ant-design/pro-components";
import { useState } from "react";

export default function Signup() {
  const navigate = useNavigate();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const handleSignup = async (values: any) => {
    setErrorMessage(null); // clear previous error

    try {
      await axios.post("/api/auth/signup", {
        name: values.name,
        email: values.email,
        password: values.password,
        phone: values.phone,
      });

      message.success("Account created successfully");
      navigate("/login");
    } catch (err: any) {
      const res = err?.response?.data;

      // Handle both string and object responses
      const backendMessage =
        typeof res === "string"
          ? res
          : res?.message ||
            res?.error ||
            res?.details ||
            (Array.isArray(res?.errors) ? res.errors.join(", ") : null);

      const finalMessage = backendMessage || "Signup failed";

      message.error(finalMessage);
      setErrorMessage(finalMessage); // show message in UI
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
      <Card title="Create Account" style={{ width: 420 }}>
        <ProForm
          onFinish={handleSignup}
          submitter={{
            searchConfig: {
              submitText: "Sign Up",
              resetText: "Reset",
            },
          }}
        >
          <ProFormText
            name="name"
            label="Full Name"
            placeholder="Enter your full name"
            rules={[{ required: true, message: "Name is required" }]}
          />

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

          <ProFormText
            name="phone"
            label="Phone Number"
            placeholder="Enter your phone number"
            rules={[{ required: true, message: "Phone number is required" }]}
          />
        </ProForm>

        {/* 👇 Display backend error visibly */}
        {errorMessage && (
          <p
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
          Already have an account? <Link to="/login">Login</Link>
        </div>
      </Card>
    </div>
  );
}
