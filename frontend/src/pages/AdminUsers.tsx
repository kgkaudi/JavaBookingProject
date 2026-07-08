import { useEffect, useState } from "react";
import { Table, Tag, Button, message, Modal, Form, Input, Select } from "antd";
import axios from "../api/axios";

interface User {
  id: string;
  name: string;
  email: string;
  phone: string;
  role: string; // ✅ single role
}

export default function AdminUsers() {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingUser, setEditingUser] = useState<User | null>(null);
  const [form] = Form.useForm();

  // ---------------------------------------------------------
  // LOAD USERS
  // ---------------------------------------------------------
  async function loadUsers() {
    try {
      setLoading(true);
      const res = await axios.get("/api/users");

      const mapped = res.data.map((u: any) => ({
        ...u,
        role: Array.isArray(u.roles) ? u.roles[0] : u.role,
      }));

      setUsers(mapped);
    } catch {
      message.error("Failed to load users");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadUsers();
  }, []);

  // ---------------------------------------------------------
  // OPEN MODAL (CREATE or EDIT)
  // ---------------------------------------------------------
  const openModal = (user?: User) => {
    if (user) {
      setEditingUser(user);
      form.setFieldsValue({
        name: user.name,
        email: user.email,
        phone: user.phone,
        role: user.role,
      });
    } else {
      setEditingUser(null);
      form.resetFields();
    }
    setModalVisible(true);
  };

  // ---------------------------------------------------------
  // SAVE USER (CREATE or UPDATE)
  // ---------------------------------------------------------
  const saveUser = async () => {
    try {
      const values = await form.validateFields();

      const payload = {
        ...values,
        role: values.role,
      };

      if (editingUser) {
        await axios.put(`/api/users/${editingUser.id}`, payload);
        message.success("User updated");
      } else {
        await axios.post("/api/users", payload);
        message.success("User created");
      }

      setModalVisible(false);
      loadUsers();
    } catch (err: any) {
      const msg = err?.response?.data?.message || "Failed to save user";
      message.error(msg);
    }
  };

  // ---------------------------------------------------------
  // DELETE USER
  // ---------------------------------------------------------
  const deleteUser = async (userId: string) => {
    Modal.confirm({
      title: "Delete User",
      content: "Are you sure you want to delete this user?",
      okText: "Delete",
      okType: "danger",
      onOk: async () => {
        try {
          await axios.delete(`/api/users/${userId}`);
          message.success("User deleted");
          loadUsers();
        } catch {
          message.error("Failed to delete user");
        }
      },
    });
  };

  // ---------------------------------------------------------
  // TABLE COLUMNS
  // ---------------------------------------------------------
  const columns = [
    {
      title: "Name",
      dataIndex: "name",
      key: "name",
      sorter: (a: User, b: User) => a.name.localeCompare(b.name),
    },
    {
      title: "Email",
      dataIndex: "email",
      key: "email",
      sorter: (a: User, b: User) => a.email.localeCompare(b.email),
    },
    {
      title: "Phone",
      dataIndex: "phone",
      key: "phone",
    },
    {
      title: "Role",
      dataIndex: "role",
      key: "role",
      render: (role: string | undefined) => (
        <Tag color={role === "ROLE_ADMIN" ? "red" : "blue"}>
          {role ? role.replace("ROLE_", "") : "Unknown"}
        </Tag>
      ),
    },
    {
      title: "Actions",
      key: "actions",
      render: (_: any, user: User) => (
        <div style={{ display: "flex", gap: 8 }}>
          <Button size="small" onClick={() => openModal(user)}>
            Edit
          </Button>
          <Button danger size="small" onClick={() => deleteUser(user.id)}>
            Delete
          </Button>
        </div>
      ),
    },
  ];

  return (
    <div style={{ padding: 16 }}>
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          marginBottom: 16,
          flexWrap: "wrap",
          gap: 12,
        }}
      >
        <h2 style={{ margin: 0 }}>Manage Users</h2>

        <div style={{ display: "flex", gap: 8 }}>
          <Button onClick={loadUsers}>Refresh</Button>
          <Button type="primary" onClick={() => openModal()}>
            Add User
          </Button>
        </div>
      </div>

      <Table
        columns={columns}
        dataSource={users}
        rowKey="id"
        loading={loading}
        pagination={{ pageSize: 10 }}
        scroll={{ x: "max-content" }}
      />

      {/* CREATE / EDIT MODAL */}
      <Modal
        title={editingUser ? "Edit User" : "Add User"}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={saveUser}
        okText={editingUser ? "Save Changes" : "Create User"}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="name"
            label="Full Name"
            rules={[{ required: true, message: "Name is required" }]}
          >
            <Input placeholder="Enter full name" />
          </Form.Item>

          <Form.Item
            name="email"
            label="Email"
            rules={[
              { required: true, message: "Email is required" },
              { type: "email", message: "Invalid email format" },
            ]}
          >
            <Input placeholder="Enter email" />
          </Form.Item>

          <Form.Item
            name="phone"
            label="Phone"
            rules={[{ required: true, message: "Phone is required" }]}
          >
            <Input placeholder="Enter phone number" />
          </Form.Item>

          <Form.Item
            name="role"
            label="Role"
            rules={[{ required: true, message: "Select a role" }]}
          >
            <Select
              options={[
                { value: "ROLE_USER", label: "User" },
                { value: "ROLE_ADMIN", label: "Admin" },
              ]}
              placeholder="Select role"
              style={{ width: "100%" }}
            />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
