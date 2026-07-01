import { useEffect, useState } from "react";
import { Table, Button, Tag, Modal, DatePicker, Select, message } from "antd";
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

      // Check availability
      const availabilityRes = await api.get("/api/bookings/availability", {
        params: { roomId, startDate, endDate },
      });

      if (!availabilityRes.data) {
        message.error("Room is not available for the selected dates");
        setCreating(false);
        return;
      }

      // Create booking
      await api.post("/api/bookings", null, {
        params: { roomId, startDate, endDate },
      });

      message.success("Booking created");
      await loadBookings();

      setRoomId("");
      setStartDate("");
      setEndDate("");
      setOpen(false);
    } catch (err) {
      message.error(err.response?.data?.message || "Failed to create booking");
    } finally {
      setCreating(false);
    }
  };

  // ---------------------------------------------------------
  // TABLE COLUMNS (responsive)
  // ---------------------------------------------------------
  const columns = [
    {
      title: "Room",
      dataIndex: "roomName",
      key: "roomName",
      responsive: ["xs", "sm", "md", "lg"],
      sorter: (a, b) => a.roomName.localeCompare(b.roomName),
    },
    {
      title: "From",
      dataIndex: "startDate",
      key: "startDate",
      responsive: ["xs", "sm", "md", "lg"],
      sorter: (a, b) =>
        new Date(a.startDate).getTime() - new Date(b.startDate).getTime(),
    },
    {
      title: "To",
      dataIndex: "endDate",
      key: "endDate",
      responsive: ["xs", "sm", "md", "lg"],
      sorter: (a, b) =>
        new Date(a.endDate).getTime() - new Date(b.endDate).getTime(),
    },
    {
      title: "Status",
      dataIndex: "status",
      key: "status",
      responsive: ["xs", "sm", "md", "lg"],
      render: (status) => {
        const color =
          status === "CONFIRMED"
            ? "green"
            : status === "PENDING"
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
        size="small" // compact for mobile
        pagination={{ pageSize: 10 }}
        scroll={{ x: "max-content" }} // horizontal scroll on mobile
      />

      {/* ---------------------------------------------------------
          NEW BOOKING MODAL
      --------------------------------------------------------- */}
      <Modal
        title="Create Booking"
        open={open}
        onCancel={() => setOpen(false)}
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
                {r.name || `Room ${r.roomNumber}`} — {r.type}
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
