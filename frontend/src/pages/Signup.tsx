import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Card, message } from "antd";
import { ProForm, ProFormText } from "@ant-design/pro-components";
import { signup } from "../api/auth";
import { getErrorMessage } from "../api/errors";

interface SignupValues {
  name: string;
  email: string;
  password: string;
  confirmPassword: string;
  phone: string;
}

export default function Signup() {
  const navigate = useNavigate();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const handleSignup = async (values: SignupValues) => {
    setErrorMessage(null);

    try {
      await signup({
        name: values.name.trim(),
        email: values.email.trim(),
        password: values.password,
        phone: values.phone.trim(),
      });

      message.success("Account created successfully");
      navigate("/login");
    } catch (err) {
      // e.g. 409 "Email already in use", or 400 with the failing field names
      const finalMessage = getErrorMessage(err, "Signup failed");
      message.error(finalMessage);
      setErrorMessage(finalMessage);
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
      <Card title="Create Account" style={{ width: 420, maxWidth: "100%" }}>
        <ProForm<SignupValues>
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
            rules={[{ required: true, whitespace: true, message: "Name is required" }]}
          />

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

          {/* same rule as the backend: 8-72 characters */}
          <ProFormText.Password
            name="password"
            label="Password"
            placeholder="At least 8 characters"
            fieldProps={{ autoComplete: "new-password" }}
            rules={[
              { required: true, message: "Password is required" },
              { min: 8, message: "Password must be at least 8 characters" },
              { max: 72, message: "Password must be at most 72 characters" },
            ]}
          />

          <ProFormText.Password
            name="confirmPassword"
            label="Confirm Password"
            placeholder="Repeat your password"
            dependencies={["password"]}
            fieldProps={{ autoComplete: "new-password" }}
            rules={[
              { required: true, message: "Please confirm your password" },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  return !value || getFieldValue("password") === value
                    ? Promise.resolve()
                    : Promise.reject(new Error("Passwords do not match"));
                },
              }),
            ]}
          />

          <ProFormText
            name="phone"
            label="Phone Number"
            placeholder="Enter your phone number"
            rules={[{ required: true, whitespace: true, message: "Phone number is required" }]}
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
          Already have an account? <Link to="/login">Login</Link>
        </div>
      </Card>
    </div>
  );
}
