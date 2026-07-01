import { useEffect, useState } from "react";
import { Table, Tag, Button, message } from "antd";
import axios from "../api/axios";
import { Link } from "react-router-dom";

interface Room {
  id: string;
  roomNumber: number;
  capacity: number;
  type: string;
  price: number;
  available: boolean;
}

export default function Rooms() {
  const [rooms, setRooms] = useState<Room[]>([]);
  const [loading, setLoading] = useState(false);

  // ---------------------------------------------------------
  // LOAD ROOMS
  // ---------------------------------------------------------
  async function loadRooms() {
    try {
      setLoading(true);
      const res = await axios.get("/api/rooms");
      setRooms(res.data);
    } catch {
      message.error("Failed to load rooms");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadRooms();
  }, []);

  // ---------------------------------------------------------
  // TABLE COLUMNS (with responsive breakpoints)
  // ---------------------------------------------------------
  const columns = [
    {
      title: "Room Number",
      dataIndex: "roomNumber",
      key: "roomNumber",
      sorter: (a: Room, b: Room) => a.roomNumber - b.roomNumber,
      responsive: ["xs", "sm", "md", "lg"],
      render: (_: any, room: Room) => (
        <Link to={`/rooms/${room.id}`}>Room {room.roomNumber}</Link>
      ),
    },
    {
      title: "Type",
      dataIndex: "type",
      key: "type",
      responsive: ["sm", "md", "lg"], // hide on extra-small screens
      filters: [
        { text: "Single", value: "single" },
        { text: "Double", value: "double" },
        { text: "Suite", value: "suite" },
      ],
      onFilter: (value: string, record: Room) =>
        record.type.toLowerCase() === value.toLowerCase(),
    },
    {
      title: "Capacity",
      dataIndex: "capacity",
      key: "capacity",
      responsive: ["md", "lg"], // hide on phones
      sorter: (a: Room, b: Room) => a.capacity - b.capacity,
    },
    {
      title: "Price (€)",
      dataIndex: "price",
      key: "price",
      responsive: ["md", "lg"], // hide on phones
      sorter: (a: Room, b: Room) => a.price - b.price,
    },
    {
      title: "Status",
      dataIndex: "available",
      key: "available",
      responsive: ["xs", "sm", "md", "lg"],
      filters: [
        { text: "Available", value: true },
        { text: "Booked", value: false },
      ],
      onFilter: (value: boolean, record: Room) => record.available === value,
      render: (available: boolean) =>
        available ? (
          <Tag color="green">Available</Tag>
        ) : (
          <Tag color="red">Booked</Tag>
        ),
    },
    {
      title: "Action",
      key: "action",
      responsive: ["xs", "sm", "md", "lg"],
      render: (_: any, room: Room) =>
        room.available ? (
          <Link to={`/bookings?roomId=${room.id}`}>
            <Button type="primary" size="small">
              Book
            </Button>
          </Link>
        ) : (
          <Button disabled size="small">
            Unavailable
          </Button>
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
        <h2 style={{ margin: 0 }}>Rooms</h2>
        <Button onClick={loadRooms}>Refresh</Button>
      </div>

      <Table
        columns={columns}
        dataSource={rooms}
        rowKey="id"
        loading={loading}
        size="small" // compact for mobile
        pagination={{ pageSize: 10 }}
        scroll={{ x: "max-content" }} // horizontal scroll on mobile
      />
    </div>
  );
}
