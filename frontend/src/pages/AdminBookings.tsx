import { useEffect, useState } from "react";
import {
  Table,
  Tag,
  Button,
  message,
  Modal,
  Form,
  DatePicker,
  Select,
} from "antd";
import axios from "../api/axios";
import dayjs from "dayjs";
import { App } from "antd";

interface Booking {
  id: string;
  roomNumber: number;
  userId: string;
  startDate: string;
  endDate: string;
  status: string;
}

interface Room {
  id: string;
  roomNumber: number;
}

interface User {
  id: string;
  name: string;
  email: string;
}

export default function AdminBookings() {
  const { modal } = App.useApp();
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [rooms, setRooms] = useState<Room[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(false);

  const [modalVisible, setModalVisible] = useState(false);
  const [editingBooking, setEditingBooking] = useState<Booking | null>(null);

  const [form] = Form.useForm();

  // ---------------------------------------------------------
  // LOAD BOOKINGS
  // ---------------------------------------------------------
  async function loadBookings() {
    try {
      setLoading(true);
      const res = await axios.get("/api/bookings");
      setBookings(res.data);
    } catch {
      message.error("Failed to load bookings");
    } finally {
      setLoading(false);
    }
  }

  // ---------------------------------------------------------
  // LOAD ROOMS + USERS (for dropdowns)
  // ---------------------------------------------------------
  async function loadRoomsAndUsers() {
    try {
      const [roomsRes, usersRes] = await Promise.all([
        axios.get("/api/rooms"),
        axios.get("/api/users"),
      ]);

      setRooms(roomsRes.data);
      setUsers(usersRes.data);
    } catch {
      message.error("Failed to load rooms or users");
    }
  }

  useEffect(() => {
    loadBookings();
    loadRoomsAndUsers();
  }, []);

  // ---------------------------------------------------------
  // OPEN MODAL (EDIT ONLY)
  // ---------------------------------------------------------
  const openModal = (booking: Booking) => {
    setEditingBooking(booking);

    form.setFieldsValue({
      roomId: rooms.find((r) => r.roomNumber === booking.roomNumber)?.id,
      userId: booking.userId,
      status: booking.status,
      dates: [dayjs(booking.startDate), dayjs(booking.endDate)],
    });

    setModalVisible(true);
  };

  // ---------------------------------------------------------
  // SAVE BOOKING (UPDATE)
  // ---------------------------------------------------------
  const saveBooking = async () => {
    try {
      const values = await form.validateFields();

      const payload = {
        roomId: values.roomId,
        userId: values.userId,
        status: values.status,
        startDate: values.dates[0].format("YYYY-MM-DD"),
        endDate: values.dates[1].format("YYYY-MM-DD"),
      };

      await axios.put(`/api/bookings/${editingBooking!.id}`, payload);

      message.success("Booking updated");
      setModalVisible(false);
      loadBookings();
    } catch (err: any) {
      const msg = err?.response?.data?.message || "Failed to update booking";
      message.error(msg);
    }
  };

  // ---------------------------------------------------------
  // CANCEL BOOKING
  // ---------------------------------------------------------
  const cancelBooking = async (bookingId: string) => {
    try {
      await axios.patch(`/api/bookings/${bookingId}/cancel`);
      message.success("Booking cancelled");
      loadBookings();
    } catch {
      message.error("Failed to cancel booking");
    }
  };

  // ---------------------------------------------------------
  // DELETE BOOKING
  // ---------------------------------------------------------
  const deleteBooking = async (bookingId: string) => {
    modal.confirm({
      title: "Delete Booking",
      content: "Are you sure you want to delete this booking?",
      okText: "Delete",
      okType: "danger",
      onOk: async () => {
        try {
          await axios.delete(`/api/bookings/${bookingId}`);
          message.success("Booking deleted");
          loadBookings();
        } catch {
          message.error("Failed to delete booking");
        }
      },
    });
  };

  // ---------------------------------------------------------
  // TABLE COLUMNS
  // ---------------------------------------------------------
  const columns = [
    {
      title: "Room",
      dataIndex: "roomNumber",
      key: "roomNumber",
      sorter: (a, b) => a.roomNumber - b.roomNumber,
      render: (num: number) => `Room ${num}`,
    },
    {
      title: "User",
      dataIndex: "userId",
      key: "userId",
      render: (userId: string) => {
        const user = users.find((u) => u.id === userId);
        return user ? `${user.name} (${user.email})` : userId;
      },
    },
    {
      title: "Start Date",
      dataIndex: "startDate",
      key: "startDate",
      render: (date: string) => dayjs(date).format("YYYY-MM-DD"),
    },
    {
      title: "End Date",
      dataIndex: "endDate",
      key: "endDate",
      render: (date: string) => dayjs(date).format("YYYY-MM-DD"),
    },
    {
      title: "Status",
      dataIndex: "status",
      key: "status",
      filters: [
        { text: "Pending", value: "pending" },
        { text: "Confirmed", value: "confirmed" },
        { text: "Cancelled", value: "cancelled" },
      ],
      onFilter: (value: string, record: Booking) => record.status === value,
      render: (status: string) => {
        const color =
          status === "confirmed"
            ? "green"
            : status === "pending"
              ? "blue"
              : "red";
        return <Tag color={color}>{status}</Tag>;
      },
    },
    {
      title: "Actions",
      key: "actions",
      render: (_: any, booking: Booking) => (
        <div style={{ display: "flex", gap: 8 }}>
          <Button size="small" onClick={() => openModal(booking)}>
            Edit
          </Button>

          {booking.status !== "cancelled" && (
            <Button size="small" onClick={() => cancelBooking(booking.id)}>
              Cancel
            </Button>
          )}

          <Button danger size="small" onClick={() => deleteBooking(booking.id)}>
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
        <h2 style={{ margin: 0 }}>Manage Bookings</h2>
        <Button onClick={loadBookings}>Refresh</Button>
      </div>

      <Table
        columns={columns}
        dataSource={bookings}
        rowKey="id"
        loading={loading}
        pagination={{ pageSize: 10 }}
        scroll={{ x: "max-content" }}
      />

      {/* EDIT MODAL */}
      <Modal
        title="Edit Booking"
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={saveBooking}
        okText="Save Changes"
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="roomId"
            label="Room"
            rules={[{ required: true, message: "Room is required" }]}
          >
            <Select
              options={rooms.map((r) => ({
                value: r.id,
                label: `Room ${r.roomNumber}`,
              }))}
            />
          </Form.Item>

          <Form.Item
            name="userId"
            label="User"
            rules={[{ required: true, message: "User is required" }]}
          >
            <Select
              options={users.map((u) => ({
                value: u.id,
                label: `${u.name} (${u.email})`,
              }))}
            />
          </Form.Item>

          <Form.Item
            name="status"
            label="Status"
            rules={[{ required: true, message: "Status is required" }]}
          >
            <Select
              options={[
                { value: "pending", label: "Pending" },
                { value: "confirmed", label: "Confirmed" },
                { value: "cancelled", label: "Cancelled" },
              ]}
            />
          </Form.Item>

          <Form.Item
            name="dates"
            label="Booking Dates"
            rules={[{ required: true, message: "Dates are required" }]}
          >
            <DatePicker.RangePicker style={{ width: "100%" }} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
