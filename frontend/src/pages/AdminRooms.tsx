import { useEffect, useState } from "react";
import {
  Table,
  Tag,
  Button,
  message,
  Modal,
  Form,
  InputNumber,
  Select,
  App,
} from "antd";
import axios from "../api/axios";

interface Room {
  id: string;
  roomNumber: number;
  capacity: number;
  type: string;
  price: number;
  available: boolean;
}

export default function AdminRooms() {
  const { modal } = App.useApp(); // ✅ React 18 compatible modal
  const [rooms, setRooms] = useState<Room[]>([]);
  const [loading, setLoading] = useState(false);

  const [modalVisible, setModalVisible] = useState(false);
  const [editingRoom, setEditingRoom] = useState<Room | null>(null);

  const [form] = Form.useForm();

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
  // OPEN MODAL (CREATE or EDIT)
  // ---------------------------------------------------------
  const openModal = (room?: Room) => {
    if (room) {
      setEditingRoom(room);
      form.setFieldsValue(room);
    } else {
      setEditingRoom(null);
      form.resetFields();
    }
    setModalVisible(true);
  };

  // ---------------------------------------------------------
  // SAVE ROOM
  // ---------------------------------------------------------
  const saveRoom = async () => {
    try {
      const values = await form.validateFields();

      if (editingRoom) {
        await axios.put(`/api/rooms/${editingRoom.id}`, values);
        message.success("Room updated");
      } else {
        await axios.post("/api/rooms", values);
        message.success("Room created");
      }

      setModalVisible(false);
      loadRooms();
    } catch (err: any) {
      const msg = err?.response?.data?.message || "Failed to save room";
      message.error(msg);
    }
  };

  // ---------------------------------------------------------
  // DELETE ROOM (React 18 safe)
  // ---------------------------------------------------------
  const deleteRoom = (room: Room) => {
    modal.confirm({
      title: `Delete Room ${room.roomNumber}?`,
      content: `This will permanently remove room ${room.roomNumber}.`,
      okText: "Delete",
      okType: "danger",
      onOk: async () => {
        try {
          await axios.delete(`/api/rooms/${room.id}`);
          message.success("Room deleted");
          loadRooms();
        } catch {
          message.error("Failed to delete room");
        }
      },
    });
  };

  // ---------------------------------------------------------
  // TOGGLE AVAILABILITY
  // ---------------------------------------------------------
  const toggleAvailability = async (room: Room) => {
    try {
      await axios.patch(`/api/rooms/${room.id}/availability`, {
        available: !room.available,
      });
      message.success("Room availability updated");
      loadRooms();
    } catch {
      message.error("Failed to update availability");
    }
  };

  // ---------------------------------------------------------
  // TABLE COLUMNS
  // ---------------------------------------------------------
  const columns = [
    {
      title: "Room Number",
      dataIndex: "roomNumber",
      key: "roomNumber",
      sorter: (a: Room, b: Room) => a.roomNumber - b.roomNumber,
    },
    {
      title: "Type",
      dataIndex: "type",
      key: "type",
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
      title: "Actions",
      key: "actions",
      render: (_: any, room: Room) => (
        <div style={{ display: "flex", gap: 8 }}>
          <Button size="small" onClick={() => openModal(room)}>
            Edit
          </Button>

          <Button
            size="small"
            onClick={() => toggleAvailability(room)}
            type={room.available ? "default" : "primary"}
          >
            {room.available ? "Mark Booked" : "Mark Available"}
          </Button>

          <Button danger size="small" onClick={() => deleteRoom(room)}>
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
        <h2 style={{ margin: 0 }}>Manage Rooms</h2>

        <div style={{ display: "flex", gap: 8 }}>
          <Button onClick={loadRooms}>Refresh</Button>
          <Button type="primary" onClick={() => openModal()}>
            Add Room
          </Button>
        </div>
      </div>

      <Table
        columns={columns}
        dataSource={rooms}
        rowKey="id"
        loading={loading}
        pagination={{ pageSize: 10 }}
        scroll={{ x: "max-content" }}
      />

      {/* CREATE / EDIT MODAL */}
      <Modal
        title={editingRoom ? "Edit Room" : "Add Room"}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={saveRoom}
        okText={editingRoom ? "Save Changes" : "Create Room"}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="roomNumber"
            label="Room Number"
            rules={[{ required: true, message: "Room number is required" }]}
          >
            <InputNumber min={1} style={{ width: "100%" }} />
          </Form.Item>

          <Form.Item
            name="type"
            label="Type"
            rules={[{ required: true, message: "Room type is required" }]}
          >
            <Select
              options={[
                { value: "single", label: "Single" },
                { value: "double", label: "Double" },
                { value: "suite", label: "Suite" },
              ]}
            />
          </Form.Item>

          <Form.Item
            name="capacity"
            label="Capacity"
            rules={[{ required: true, message: "Capacity is required" }]}
          >
            <InputNumber min={1} style={{ width: "100%" }} />
          </Form.Item>

          <Form.Item
            name="price"
            label="Price (€)"
            rules={[{ required: true, message: "Price is required" }]}
          >
            <InputNumber min={1} style={{ width: "100%" }} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
