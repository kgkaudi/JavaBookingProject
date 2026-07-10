import { useEffect, useState } from "react";
import { Table, Button, Tag, Modal, DatePicker, Select, message, Alert } from "antd";
import api from "../api/axios";
import dayjs from "dayjs";

export default function Bookings() {
  const [bookings, setBookings] = useState([]);
  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(true);

  // Modal state
  const [open, setOpen] = useState(false);
  const [roomId, setRoomId] = useState("");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [creating, setCreating] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  // ---------------------------------------------------------
  // LOAD BOOKINGS
  // ---------------------------------------------------------
  const loadBookings = async () => {
    try {
      const res = await api.get("/api/bookings/me");
      setBookings(res.data);
    } catch {
      message.error("Failed to load bookings");
    } finally {
      setLoading(false);
    }
  };

  // ---------------------------------------------------------
  // LOAD ROOMS + BOOKINGS ON MOUNT
  // ---------------------------------------------------------
  useEffect(() => {
    const fetchRooms = async () => {
      try {
        const res = await api.get("/api/rooms");
        setRooms(res.data);
      } catch {
        message.error("Failed to load rooms");
      }
    };

    fetchRooms();
    loadBookings();
  }, []);

  // ---------------------------------------------------------
  // CREATE BOOKING
  // ---------------------------------------------------------
  const createBooking = async () => {
    try {
      setCreating(true);
      setErrorMessage("");

      // Validate fields
      if (!roomId || !startDate || !endDate) {
        setErrorMessage("Please fill all fields");
        setCreating(false);
        return;
      }

      // Check availability with new backend response format
      const availabilityRes = await api.get("/api/bookings/availability", {
        params: { roomId, startDate, endDate },
      });

      if (!availabilityRes.data.available) {
        const msg =
          availabilityRes.data.reason ||
          availabilityRes.data.error ||
          "Room not available";
        setErrorMessage(msg);
        message.error(msg);
        setCreating(false);
        return;
      }

      // Create booking
      await api.post("/api/bookings", null, {
        params: { roomId, startDate, endDate },
      });

      message.success("Booking created");
      await loadBookings();

      // Reset modal state
          // Reset modal state
        setRoomId("");
        setStartDate("");
        setEndDate("");
        setErrorMessage("");
        setOpen(false);
      } catch (err) {
        console.error("Booking error:", err);

        // Safely extract backend message
        const backendMsg =
          err.response?.data?.reason ||
          err.response?.data?.error ||
          err.response?.data?.message ||
          err.message ||
          "Failed to create booking";

        setErrorMessage(backendMsg); // show inline alert
        message.error(backendMsg);   // toast notification
      } finally {
        setCreating(false);
      }
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
      render: (num) => `Room ${num}`,
    },
    {
      title: "From",
      dataIndex: "startDate",
      key: "startDate",
      sorter: (a, b) =>
        new Date(a.startDate).getTime() - new Date(b.startDate).getTime(),
      render: (date) => dayjs(date).format("YYYY-MM-DD"),
    },
    {
      title: "To",
      dataIndex: "endDate",
      key: "endDate",
      sorter: (a, b) =>
        new Date(a.endDate).getTime() - new Date(b.endDate).getTime(),
      render: (date) => dayjs(date).format("YYYY-MM-DD"),
    },
    {
      title: "Status",
      dataIndex: "status",
      key: "status",
      render: (status) => {
        const color =
          status === "confirmed"
            ? "green"
            : status === "pending"
            ? "gold"
            : "red";
        return <Tag color={color}>{status}</Tag>;
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
        <h2 style={{ margin: 0 }}>My Bookings</h2>
        <Button type="primary" onClick={() => setOpen(true)}>
          + New Booking
        </Button>
      </div>

      <Table
        columns={columns}
        dataSource={bookings}
        rowKey="id"
        loading={loading}
        size="small"
        pagination={{ pageSize: 10 }}
        scroll={{ x: "max-content" }}
      />

      {/* ---------------------------------------------------------
          NEW BOOKING MODAL
      --------------------------------------------------------- */}
      <Modal
        title="Create Booking"
        open={open}
        onCancel={() => {
          setOpen(false);
          setErrorMessage("");
        }}
        footer={[
          <Button key="cancel" onClick={() => setOpen(false)}>
            Cancel
          </Button>,
          <Button
            key="create"
            type="primary"
            loading={creating}
            disabled={!roomId || !startDate || !endDate}
            onClick={createBooking}
          >
            Create
          </Button>,
        ]}
      >
        {/* Inline error display */}
        {errorMessage && (
          <Alert
            message={errorMessage}
            type="error"
            showIcon
            style={{ marginBottom: 16 }}
          />
        )}

        <div style={{ marginBottom: 16 }}>
          <label>Room</label>
          <Select
            style={{ width: "100%", marginTop: 6 }}
            placeholder="Select a room"
            value={roomId}
            onChange={setRoomId}
          >
            {rooms.map((r) => (
              <Select.Option key={r.id} value={r.id}>
                Room {r.roomNumber} — {r.type}
              </Select.Option>
            ))}
          </Select>
        </div>

        <div style={{ marginBottom: 16 }}>
          <label>Start Date</label>
          <DatePicker
            style={{ width: "100%", marginTop: 6 }}
            value={startDate ? dayjs(startDate) : null}
            onChange={(d) => setStartDate(d?.format("YYYY-MM-DD") || "")}
          />
        </div>

        <div style={{ marginBottom: 16 }}>
          <label>End Date</label>
          <DatePicker
            style={{ width: "100%", marginTop: 6 }}
            value={endDate ? dayjs(endDate) : null}
            onChange={(d) => setEndDate(d?.format("YYYY-MM-DD") || "")}
          />
        </div>
      </Modal>
    </div>
  );
}
