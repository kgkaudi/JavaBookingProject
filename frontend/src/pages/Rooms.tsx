import { useEffect, useState } from "react";
import { ProTable } from "@ant-design/pro-components";
import axios from "../api/axios";
import { message, Tag, Button, ConfigProvider } from "antd";
import { Link } from "react-router-dom";
import enUS from "antd/es/locale/en_US";

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

  async function loadRooms() {
    try {
      setLoading(true);
      const res = await axios.get("/api/rooms");
      setRooms(res.data);
    } catch (err) {
      message.error("Failed to load rooms");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadRooms();
  }, []);

  const columns = [
    {
      title: "Room Number",
      dataIndex: "roomNumber",
      key: "roomNumber",
      sorter: (a: Room, b: Room) => a.roomNumber - b.roomNumber,
      render: (_, room) => (
        <Link to={`/rooms/${room.id}`}>Room {room.roomNumber}</Link>
      ),
    },
    {
      title: "Type",
      dataIndex: "type",
      key: "type",
      filters: [
        { text: "Single", value: "Single" },
        { text: "Double", value: "Double" },
        { text: "Suite", value: "Suite" },
      ],
      onFilter: (value: string, record: Room) => record.type === value,
    },
    {
      title: "Capacity",
      dataIndex: "capacity",
      key: "capacity",
      sorter: (a: Room, b: Room) => a.capacity - b.capacity,
    },
    {
      title: "Price (€)",
      dataIndex: "price",
      key: "price",
      sorter: (a: Room, b: Room) => a.price - b.price,
    },
    {
      title: "Status",
      dataIndex: "available",
      key: "available",
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
      render: (_, room: Room) =>
        room.available ? (
          <Link to={`/bookings?roomId=${room.id}`}>
            <Button type="primary">Book Room</Button>
          </Link>
        ) : (
          <Button disabled>Unavailable</Button>
        ),
    },
  ];

  return (
    <ConfigProvider locale={enUS}>
      <ProTable<Room>
        headerTitle="Rooms"
        loading={loading}
        columns={columns}
        dataSource={rooms}
        rowKey="id"
        search={{ filterType: "light" }}
        pagination={{ pageSize: 10 }}
      />
    </ConfigProvider>
  );
}
