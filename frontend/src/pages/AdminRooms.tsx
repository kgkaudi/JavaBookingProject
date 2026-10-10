import { useEffect, useState } from "react";
import { App, Button, Form, InputNumber, Modal, Select, Switch, Table, Tag, message } from "antd";
import type { TableColumnsType } from "antd";
import { createRoom, deleteRoom, getRooms, updateRoom } from "../api/rooms";
import { getErrorMessage } from "../api/errors";
import type { Room, RoomInput } from "../types";

export default function AdminRooms() {
  const { modal } = App.useApp();

  const [rooms, setRooms] = useState<Room[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  const [modalVisible, setModalVisible] = useState(false);
  const [editingRoom, setEditingRoom] = useState<Room | null>(null);
  const [formKey, setFormKey] = useState(0);

  const [form] = Form.useForm<RoomInput>();

  // ---------------------------------------------------------
  // LOAD ROOMS
  // ---------------------------------------------------------
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

  const loadRooms = async () => {
    setLoading(true);
    try {
      setRooms(await getRooms());
    } catch (err) {
      message.error(getErrorMessage(err, "Failed to load rooms"));
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
  const openModal = (room?: Room) => {
    setEditingRoom(room ?? null);
    setFormKey((k) => k + 1); // fresh form every time the modal opens
    setModalVisible(true);
  };

  // every field is listed (even when empty) so nothing from a previous open can leak into this one;
  // new rooms are bookable unless the admin says otherwise
  const initialValues: Partial<RoomInput> = editingRoom
    ? {
        roomNumber: editingRoom.roomNumber,
        type: editingRoom.type,
        capacity: editingRoom.capacity,
        price: editingRoom.price,
        available: editingRoom.available,
      }
    : { roomNumber: undefined, type: undefined, capacity: undefined, price: undefined, available: true };

  // ---------------------------------------------------------
  // SAVE ROOM
  // ---------------------------------------------------------
  const saveRoom = async () => {
    try {
      const values = await form.validateFields();
      setSaving(true);

      if (editingRoom) {
        await updateRoom(editingRoom.id, values);
        message.success("Room updated");
      } else {
        await createRoom(values);
        message.success("Room created");
      }

      setModalVisible(false);
      loadRooms();
    } catch (err) {
      if (err && typeof err === "object" && "errorFields" in err) return; // form validation
      // e.g. 409 "Room number already exists" or field errors from the backend
      message.error(getErrorMessage(err, "Failed to save room"));
    } finally {
      setSaving(false);
    }
  };

  // ---------------------------------------------------------
  // DELETE ROOM
  // ---------------------------------------------------------
  const handleDelete = (room: Room) => {
    modal.confirm({
      title: `Delete Room ${room.roomNumber}?`,
      content: `This will permanently remove room ${room.roomNumber}.`,
      okText: "Delete",
      okType: "danger",
      onOk: async () => {
        try {
          await deleteRoom(room.id);
          message.success("Room deleted");
          loadRooms();
        } catch (err) {
          message.error(getErrorMessage(err, "Failed to delete room"));
        }
      },
    });
  };

  // ---------------------------------------------------------
  // TOGGLE AVAILABILITY (the backend has no dedicated endpoint: update the room)
  // ---------------------------------------------------------
  const toggleAvailability = async (room: Room) => {
    try {
      await updateRoom(room.id, {
        roomNumber: room.roomNumber,
        type: room.type,
        capacity: room.capacity,
        price: room.price,
        available: !room.available,
      });
      message.success("Room availability updated");
      loadRooms();
    } catch (err) {
      message.error(getErrorMessage(err, "Failed to update availability"));
    }
  };

  // ---------------------------------------------------------
  // TABLE COLUMNS
  // ---------------------------------------------------------
  const columns: TableColumnsType<Room> = [
    {
      title: "Room Number",
      dataIndex: "roomNumber",
      key: "roomNumber",
      sorter: (a, b) => a.roomNumber - b.roomNumber,
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
      onFilter: (value, record) => record.type.toLowerCase() === String(value).toLowerCase(),
    },
    {
      title: "Capacity",
      dataIndex: "capacity",
      key: "capacity",
      sorter: (a, b) => a.capacity - b.capacity,
    },
    {
      title: "Price / night (€)",
      dataIndex: "price",
      key: "price",
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
      title: "Actions",
      key: "actions",
      render: (_, room) => (
        <div style={{ display: "flex", gap: 8 }}>
          <Button size="small" onClick={() => openModal(room)}>
            Edit
          </Button>

          <Button
            size="small"
            onClick={() => toggleAvailability(room)}
            type={room.available ? "default" : "primary"}
          >
            {room.available ? "Mark Unavailable" : "Mark Available"}
          </Button>

          <Button danger size="small" onClick={() => handleDelete(room)}>
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

      <Table<Room>
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
        confirmLoading={saving}
        okText={editingRoom ? "Save Changes" : "Create Room"}
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
            name="roomNumber"
            label="Room Number"
            rules={[{ required: true, message: "Room number is required" }]}
          >
            <InputNumber min={1} precision={0} style={{ width: "100%" }} />
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
            <InputNumber min={1} precision={0} style={{ width: "100%" }} />
          </Form.Item>

          <Form.Item
            name="price"
            label="Price per night (€)"
            rules={[{ required: true, message: "Price is required" }]}
          >
            <InputNumber min={0.01} step={5} precision={2} style={{ width: "100%" }} />
          </Form.Item>

          <Form.Item name="available" label="Available for booking" valuePropName="checked">
            <Switch />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
