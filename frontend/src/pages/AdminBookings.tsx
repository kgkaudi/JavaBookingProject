import { useEffect, useState } from "react";
import {
  App,
  Button,
  DatePicker,
  Form,
  Input,
  Modal,
  Popconfirm,
  Select,
  Table,
  Tag,
  message,
} from "antd";
import type { TableColumnsType } from "antd";
import dayjs from "dayjs";
import type { Dayjs } from "dayjs";
import {
  cancelBooking,
  deleteBooking,
  getAllBookings,
  updateBooking,
} from "../api/bookings";
import { getUsers } from "../api/users";
import { getErrorMessage } from "../api/errors";
import type { Booking, BookingStatus, User } from "../types";

const DATE_FORMAT = "YYYY-MM-DD";

const STATUS_COLORS: Record<string, string> = {
  confirmed: "green",
  pending: "blue",
  cancelled: "red",
};

interface BookingFormValues {
  status: BookingStatus;
  dates: [Dayjs, Dayjs];
}

export default function AdminBookings() {
  const { modal } = App.useApp();

  const [bookings, setBookings] = useState<Booking[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  const [modalVisible, setModalVisible] = useState(false);
  const [editingBooking, setEditingBooking] = useState<Booking | null>(null);
  const [formKey, setFormKey] = useState(0);

  const [form] = Form.useForm<BookingFormValues>();

  const userLabel = (userId: string) => {
    const user = users.find((u) => u.id === userId);
    return user ? `${user.name} (${user.email})` : userId;
  };

  // ---------------------------------------------------------
  // LOAD BOOKINGS + USERS (users are only used to show names)
  // ---------------------------------------------------------
  useEffect(() => {
    let active = true;

    Promise.all([getAllBookings(), getUsers()])
      .then(([allBookings, allUsers]) => {
        if (!active) return;
        setBookings(allBookings);
        setUsers(allUsers);
      })
      .catch((err) => message.error(getErrorMessage(err, "Failed to load bookings")))
      .finally(() => {
        if (active) setLoading(false);
      });

    return () => {
      active = false;
    };
  }, []);

  const loadBookings = async () => {
    setLoading(true);
    try {
      setBookings(await getAllBookings());
    } catch (err) {
      message.error(getErrorMessage(err, "Failed to load bookings"));
    } finally {
      setLoading(false);
    }
  };

  // ---------------------------------------------------------
  // OPEN MODAL (EDIT ONLY)
  // The form is filled through `initialValues` + `preserve={false}` (see below), NOT with
  // form.setFieldsValue(): the form does not exist yet when this runs, and React StrictMode
  // (used in dev) would wipe values that were set before it mounted.
  // ---------------------------------------------------------
  const openModal = (booking: Booking) => {
    setEditingBooking(booking);
    setFormKey((k) => k + 1); // fresh form every time the modal opens
    setModalVisible(true);
  };

  const initialValues: Partial<BookingFormValues> | undefined = editingBooking
    ? {
        status: editingBooking.status,
        dates: [dayjs(editingBooking.startDate), dayjs(editingBooking.endDate)],
      }
    : undefined;

  // ---------------------------------------------------------
  // SAVE BOOKING (UPDATE) — the backend only changes status and dates;
  // the price is recalculated server-side.
  // ---------------------------------------------------------
  const saveBooking = async () => {
    if (!editingBooking) return;

    try {
      const values = await form.validateFields();
      setSaving(true);

      await updateBooking(editingBooking.id, {
        status: values.status,
        startDate: values.dates[0].format(DATE_FORMAT),
        endDate: values.dates[1].format(DATE_FORMAT),
      });

      message.success("Booking updated");
      setModalVisible(false);
      loadBookings();
    } catch (err) {
      if (err && typeof err === "object" && "errorFields" in err) return; // form validation
      // e.g. 409 "Dates overlap with existing booking"
      message.error(getErrorMessage(err, "Failed to update booking"));
    } finally {
      setSaving(false);
    }
  };

  // ---------------------------------------------------------
  // CANCEL BOOKING
  // ---------------------------------------------------------
  const handleCancel = async (bookingId: string) => {
    try {
      await cancelBooking(bookingId);
      message.success("Booking cancelled");
      loadBookings();
    } catch (err) {
      message.error(getErrorMessage(err, "Failed to cancel booking"));
    }
  };

  // ---------------------------------------------------------
  // DELETE BOOKING
  // ---------------------------------------------------------
  const handleDelete = (bookingId: string) => {
    modal.confirm({
      title: "Delete Booking",
      content: "Are you sure you want to delete this booking? This cannot be undone.",
      okText: "Delete",
      okType: "danger",
      onOk: async () => {
        try {
          await deleteBooking(bookingId);
          message.success("Booking deleted");
          loadBookings();
        } catch (err) {
          message.error(getErrorMessage(err, "Failed to delete booking"));
        }
      },
    });
  };

  // ---------------------------------------------------------
  // TABLE COLUMNS
  // ---------------------------------------------------------
  const columns: TableColumnsType<Booking> = [
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
      render: (userId: string) => userLabel(userId),
    },
    {
      title: "Check-in",
      dataIndex: "startDate",
      key: "startDate",
      sorter: (a, b) => a.startDate.localeCompare(b.startDate),
      defaultSortOrder: "descend",
      render: (date: string) => dayjs(date).format(DATE_FORMAT),
    },
    {
      title: "Check-out",
      dataIndex: "endDate",
      key: "endDate",
      render: (date: string) => dayjs(date).format(DATE_FORMAT),
    },
    {
      title: "Total",
      dataIndex: "totalPrice",
      key: "totalPrice",
      render: (total: number) => `€${total.toFixed(2)}`,
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
      onFilter: (value, record) => record.status === value,
      render: (status: string) => <Tag color={STATUS_COLORS[status] ?? "default"}>{status}</Tag>,
    },
    {
      title: "Actions",
      key: "actions",
      render: (_, booking) => (
        <div style={{ display: "flex", gap: 8 }}>
          <Button size="small" onClick={() => openModal(booking)}>
            Edit
          </Button>

          {booking.status !== "cancelled" && (
            <Popconfirm
              title="Cancel this booking?"
              okText="Yes, cancel"
              cancelText="No"
              okButtonProps={{ danger: true }}
              onConfirm={() => handleCancel(booking.id)}
            >
              <Button size="small">Cancel</Button>
            </Popconfirm>
          )}

          <Button danger size="small" onClick={() => handleDelete(booking.id)}>
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

      <Table<Booking>
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
        confirmLoading={saving}
        okText="Save Changes"
        destroyOnHidden
      >
        <Form
          key={formKey}
          form={form}
          layout="vertical"
          initialValues={initialValues}
          preserve={false} // forget values on close so the next open starts from initialValues
        >
          {/* room and guest cannot be changed on an existing booking */}
          <Form.Item label="Room">
            <Input disabled value={editingBooking ? `Room ${editingBooking.roomNumber}` : ""} />
          </Form.Item>

          <Form.Item label="Guest">
            <Input disabled value={editingBooking ? userLabel(editingBooking.userId) : ""} />
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
            label="Check-in / check-out"
            rules={[{ required: true, message: "Dates are required" }]}
            extra="The price is recalculated automatically."
          >
            <DatePicker.RangePicker style={{ width: "100%" }} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
