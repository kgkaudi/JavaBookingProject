import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { Button, Table, Tag, message } from "antd";
import type { TableColumnsType } from "antd";
import { getRooms } from "../api/rooms";
import { getErrorMessage } from "../api/errors";
import type { Room } from "../types";

export default function Rooms() {
  const [rooms, setRooms] = useState<Room[]>([]);
  const [loading, setLoading] = useState(true);

  // initial load
  useEffect(() => {
    let active = true;

    getRooms()
      .then((data) => {
        if (active) setRooms(data);
      })
      .catch((err) => message.error(getErrorMessage(err, "Failed to load rooms")))
      .finally(() => {
        if (active) setLoading(false);
      });

    return () => {
      active = false;
    };
  }, []);

  // manual refresh
  const refresh = async () => {
    setLoading(true);
    try {
      setRooms(await getRooms());
    } catch (err) {
      message.error(getErrorMessage(err, "Failed to load rooms"));
    } finally {
      setLoading(false);
    }
  };

  const columns: TableColumnsType<Room> = [
    {
      title: "Room Number",
      dataIndex: "roomNumber",
      key: "roomNumber",
      sorter: (a, b) => a.roomNumber - b.roomNumber,
      render: (_, room) => <Link to={`/rooms/${room.id}`}>Room {room.roomNumber}</Link>,
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
      onFilter: (value, record) => record.type.toLowerCase() === String(value).toLowerCase(),
    },
    {
      title: "Capacity",
      dataIndex: "capacity",
      key: "capacity",
      responsive: ["md", "lg"],
      sorter: (a, b) => a.capacity - b.capacity,
    },
    {
      title: "Price / night (€)",
      dataIndex: "price",
      key: "price",
      responsive: ["md", "lg"],
      sorter: (a, b) => a.price - b.price,
    },
    {
      title: "Status",
      dataIndex: "available",
      key: "available",
      filters: [
        { text: "Available", value: true },
        { text: "Unavailable", value: false },
      ],
      onFilter: (value, record) => record.available === value,
      render: (available: boolean) =>
        available ? <Tag color="green">Available</Tag> : <Tag color="red">Unavailable</Tag>,
    },
    {
      title: "Action",
      key: "action",
      render: (_, room) =>
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
        <Button onClick={refresh}>Refresh</Button>
      </div>

      <Table<Room>
        columns={columns}
        dataSource={rooms}
        rowKey="id"
        loading={loading}
        size="small"
        pagination={{ pageSize: 10 }}
        scroll={{ x: "max-content" }}
      />
    </div>
  );
}
