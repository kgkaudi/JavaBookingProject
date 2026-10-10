import { useEffect, useState } from "react";
import { App, Button, Form, Input, Modal, Select, Table, Tag, Tooltip, message } from "antd";
import type { TableColumnsType } from "antd";
import { useAuth } from "../context/AuthContext";
import {
  createUser,
  deleteUser,
  demoteUser,
  getUsers,
  promoteUser,
  updateUser,
} from "../api/users";
import { getErrorMessage } from "../api/errors";
import { ROLE_ADMIN, ROLE_USER } from "../types";
import type { User } from "../types";

interface UserFormValues {
  name: string;
  email: string;
  phone?: string;
  password?: string;
  role: string;
}

const roleOf = (user: User) => (user.roles?.includes(ROLE_ADMIN) ? ROLE_ADMIN : ROLE_USER);

export default function AdminUsers() {
  const { modal } = App.useApp();
  const { user: currentUser } = useAuth();

  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingUser, setEditingUser] = useState<User | null>(null);
  const [formKey, setFormKey] = useState(0);
  const [form] = Form.useForm<UserFormValues>();

  // ---------------------------------------------------------
  // LOAD USERS
  // ---------------------------------------------------------
  useEffect(() => {
    let active = true;

    getUsers()
      .then((data) => {
        if (active) setUsers(data);
      })
      .catch((err) => message.error(getErrorMessage(err, "Failed to load users")))
      .finally(() => {
        if (active) setLoading(false);
      });

    return () => {
      active = false;
    };
  }, []);

  const loadUsers = async () => {
    setLoading(true);
    try {
      setUsers(await getUsers());
    } catch (err) {
      message.error(getErrorMessage(err, "Failed to load users"));
    } finally {
      setLoading(false);
    }
  };

  // ---------------------------------------------------------
  // OPEN MODAL (CREATE or EDIT)
  // The form is filled through `initialValues` + `preserve={false}` (see below), NOT with
  // form.setFieldsValue(): the form does not exist yet when this runs, and React StrictMode
  // (used in dev) would wipe values that were set before it mounted.
  // ---------------------------------------------------------
  const openModal = (user?: User) => {
    setEditingUser(user ?? null);
    setFormKey((k) => k + 1); // fresh form every time the modal opens
    setModalVisible(true);
  };

  // every field is listed (even when empty) so nothing from a previous open can leak into this one
  const initialValues: Partial<UserFormValues> = editingUser
    ? {
        name: editingUser.name,
        email: editingUser.email,
        phone: editingUser.phone ?? "",
        role: roleOf(editingUser),
      }
    : { name: "", email: "", phone: "", password: "", role: ROLE_USER };

  // ---------------------------------------------------------
  // SAVE USER (CREATE or UPDATE)
  // ---------------------------------------------------------
  const saveUser = async () => {
    try {
      const values = await form.validateFields();
      setSaving(true);

      if (editingUser) {
        // 1) profile fields
        await updateUser(editingUser.id, {
          name: values.name.trim(),
          email: values.email.trim(),
          phone: values.phone?.trim(),
        });

        // 2) role changes use their own admin-only endpoints
        const wasAdmin = roleOf(editingUser) === ROLE_ADMIN;
        const wantsAdmin = values.role === ROLE_ADMIN;
        if (wantsAdmin && !wasAdmin) {
          await promoteUser(editingUser.id);
        } else if (!wantsAdmin && wasAdmin) {
          await demoteUser(editingUser.id);
        }

        message.success("User updated");
      } else {
        await createUser({
          name: values.name.trim(),
          email: values.email.trim(),
          phone: values.phone?.trim(),
          password: values.password ?? "",
          roles: [values.role],
        });
        message.success("User created");
      }

      setModalVisible(false);
      loadUsers();
    } catch (err) {
      // form validation failures have no response; show nothing extra for those
      if (err && typeof err === "object" && "errorFields" in err) return;
      message.error(getErrorMessage(err, "Failed to save user"));
    } finally {
      setSaving(false);
    }
  };

  // ---------------------------------------------------------
  // DELETE USER
  // ---------------------------------------------------------
  const handleDelete = (user: User) => {
    modal.confirm({
      title: `Delete ${user.name}?`,
      content: `This will permanently remove ${user.email}.`,
      okText: "Delete",
      okType: "danger",
      onOk: async () => {
        try {
          await deleteUser(user.id);
          message.success("User deleted");
          loadUsers();
        } catch (err) {
          message.error(getErrorMessage(err, "Failed to delete user"));
        }
      },
    });
  };

  const editingSelf = !!editingUser && editingUser.id === currentUser?.id;

  // ---------------------------------------------------------
  // TABLE COLUMNS
  // ---------------------------------------------------------
  const columns: TableColumnsType<User> = [
    {
      title: "Name",
      dataIndex: "name",
      key: "name",
      sorter: (a, b) => a.name.localeCompare(b.name),
    },
    {
      title: "Email",
      dataIndex: "email",
      key: "email",
      sorter: (a, b) => a.email.localeCompare(b.email),
    },
    {
      title: "Phone",
      dataIndex: "phone",
      key: "phone",
    },
    {
      title: "Role",
      key: "role",
      filters: [
        { text: "Admin", value: ROLE_ADMIN },
        { text: "User", value: ROLE_USER },
      ],
      onFilter: (value, record) => roleOf(record) === value,
      render: (_, user) => {
        const role = roleOf(user);
        return <Tag color={role === ROLE_ADMIN ? "red" : "blue"}>{role.replace("ROLE_", "")}</Tag>;
      },
    },
    {
      title: "Actions",
      key: "actions",
      render: (_, user) => {
        const isSelf = user.id === currentUser?.id;
        return (
          <div style={{ display: "flex", gap: 8 }}>
            <Button size="small" onClick={() => openModal(user)}>
              Edit
            </Button>
            <Tooltip title={isSelf ? "You cannot delete your own account" : undefined}>
              <Button danger size="small" disabled={isSelf} onClick={() => handleDelete(user)}>
                Delete
              </Button>
            </Tooltip>
          </div>
        );
      },
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

      <Table<User>
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
        confirmLoading={saving}
        okText={editingUser ? "Save Changes" : "Create User"}
        destroyOnHidden
      >
        <Form
          key={formKey}
          form={form}
          layout="vertical"
          initialValues={initialValues}
          preserve={false} // forget values on close so the next open starts from initialValues
        >
          <Form.Item
            name="name"
            label="Full Name"
            rules={[{ required: true, whitespace: true, message: "Name is required" }]}
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

          <Form.Item name="phone" label="Phone">
            <Input placeholder="Enter phone number" />
          </Form.Item>

          {/* the backend requires an initial password for new users */}
          {!editingUser && (
            <Form.Item
              name="password"
              label="Initial Password"
              rules={[
                { required: true, message: "Password is required" },
                { min: 8, message: "Password must be at least 8 characters" },
                { max: 72, message: "Password must be at most 72 characters" },
              ]}
            >
              <Input.Password placeholder="At least 8 characters" autoComplete="new-password" />
            </Form.Item>
          )}

          <Form.Item
            name="role"
            label="Role"
            rules={[{ required: true, message: "Select a role" }]}
            extra={editingSelf ? "You cannot change your own role." : undefined}
          >
            <Select
              disabled={editingSelf}
              options={[
                { value: ROLE_USER, label: "User" },
                { value: ROLE_ADMIN, label: "Admin" },
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
