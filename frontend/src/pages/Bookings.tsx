import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { Alert, Button, DatePicker, Modal, Popconfirm, Select, Table, Tag, message } from "antd";
import type { TableColumnsType } from "antd";
import dayjs from "dayjs";
import type { Dayjs } from "dayjs";
import { cancelBooking, checkAvailability, createBooking, getMyBookings } from "../api/bookings";
import { getRooms } from "../api/rooms";
import { getErrorMessage } from "../api/errors";
import type { Booking, Room } from "../types";

const DATE_FORMAT = "YYYY-MM-DD";

const STATUS_COLORS: Record<string, string> = {
  confirmed: "green",
  pending: "gold",
  cancelled: "default",
};

export default function Bookings() {
  // /bookings?roomId=... comes from the "Book" buttons on the rooms pages
  const [searchParams, setSearchParams] = useSearchParams();
  const preselectedRoomId = searchParams.get("roomId");

  const [bookings, setBookings] = useState<Booking[]>([]);
  const [rooms, setRooms] = useState<Room[]>([]);
  const [loading, setLoading] = useState(true);

  // Modal state
  const [open, setOpen] = useState(Boolean(preselectedRoomId));
  const [roomId, setRoomId] = useState<string | undefined>(preselectedRoomId ?? undefined);
  const [range, setRange] = useState<[Dayjs, Dayjs] | null>(null);
  const [creating, setCreating] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  // ---------------------------------------------------------
  // LOAD ROOMS + BOOKINGS ON MOUNT
  // ---------------------------------------------------------
  useEffect(() => {
    let active = true;

    Promise.all([getMyBookings(), getRooms()])
      .then(([myBookings, allRooms]) => {
        if (!active) return;
        setBookings(myBookings);
        setRooms(allRooms);
      })
      .catch((err) => message.error(getErrorMessage(err, "Failed to load your bookings")))
      .finally(() => {
        if (active) setLoading(false);
      });

    return () => {
      active = false;
    };
  }, []);

  const refreshBookings = async () => {
    setLoading(true);
    try {
      setBookings(await getMyBookings());
    } catch (err) {
      message.error(getErrorMessage(err, "Failed to load your bookings"));
    } finally {
      setLoading(false);
    }
  };

  // ---------------------------------------------------------
  // MODAL HELPERS
  // ---------------------------------------------------------
  const closeModal = () => {
    setOpen(false);
    setErrorMessage("");
    if (searchParams.has("roomId")) {
      setSearchParams({}, { replace: true });
    }
  };

  const nights = range ? range[1].diff(range[0], "day") : 0;
  const selectedRoom = rooms.find((r) => r.id === roomId);
  const estimatedTotal = selectedRoom && nights > 0 ? selectedRoom.price * nights : null;

  // ---------------------------------------------------------
  // CREATE BOOKING
  // ---------------------------------------------------------
  const handleCreate = async () => {
    if (!roomId || !range) {
      setErrorMessage("Please choose a room and your dates");
      return;
    }
    if (nights < 1) {
      setErrorMessage("Check-out must be at least one day after check-in");
      return;
    }

    const startDate = range[0].format(DATE_FORMAT);
    const endDate = range[1].format(DATE_FORMAT);

    setCreating(true);
    setErrorMessage("");

    try {
      const availability = await checkAvailability(roomId, startDate, endDate);

      if (!availability.available) {
        setErrorMessage(availability.reason ?? "Room not available for these dates");
        return;
      }

      await createBooking(roomId, startDate, endDate);

      message.success("Booking created");
      await refreshBookings();

      setRoomId(undefined);
      setRange(null);
      closeModal();
    } catch (err) {
      // e.g. 409 when someone else booked the room in the meantime
      setErrorMessage(getErrorMessage(err, "Failed to create booking"));
    } finally {
      setCreating(false);
    }
  };

  // ---------------------------------------------------------
  // CANCEL BOOKING
  // ---------------------------------------------------------
  const handleCancel = async (bookingId: string) => {
    try {
      await cancelBooking(bookingId);
      message.success("Booking cancelled");
      await refreshBookings();
    } catch (err) {
      message.error(getErrorMessage(err, "Failed to cancel booking"));
    }
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
      sorter: (a, b) => a.endDate.localeCompare(b.endDate),
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
      render: (status: string) => <Tag color={STATUS_COLORS[status] ?? "red"}>{status}</Tag>,
    },
    {
      title: "Action",
      key: "action",
      render: (_, booking) => {
        const finished = dayjs(booking.endDate).isBefore(dayjs(), "day");
        if (booking.status === "cancelled" || finished) {
          return null;
        }
        return (
          <Popconfirm
            title="Cancel this booking?"
            description="This frees the room for other guests."
            okText="Yes, cancel"
            cancelText="Keep it"
            okButtonProps={{ danger: true }}
            onConfirm={() => handleCancel(booking.id)}
          >
            <Button size="small" danger>
              Cancel
            </Button>
          </Popconfirm>
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
        <h2 style={{ margin: 0 }}>My Bookings</h2>
        <Button type="primary" onClick={() => setOpen(true)}>
          + New Booking
        </Button>
      </div>

      <Table<Booking>
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
        onCancel={closeModal}
        footer={[
          <Button key="cancel" onClick={closeModal}>
            Cancel
          </Button>,
          <Button
            key="create"
            type="primary"
            loading={creating}
            disabled={!roomId || !range}
            onClick={handleCreate}
          >
            Create
          </Button>,
        ]}
      >
        {errorMessage && (
          <Alert message={errorMessage} type="error" showIcon style={{ marginBottom: 16 }} />
        )}

        <div style={{ marginBottom: 16 }}>
          <label htmlFor="booking-room">Room</label>
          <Select
            id="booking-room"
            style={{ width: "100%", marginTop: 6 }}
            placeholder="Select a room"
            value={roomId}
            onChange={setRoomId}
            options={rooms
              .filter((r) => r.available || r.id === roomId)
              .map((r) => ({
                value: r.id,
                label: `Room ${r.roomNumber} — ${r.type} (€${r.price}/night)`,
              }))}
          />
        </div>

        <div style={{ marginBottom: 16 }}>
          <label>Check-in and check-out</label>
          <DatePicker.RangePicker
            style={{ width: "100%", marginTop: 6 }}
            value={range}
            disabledDate={(d) => d.isBefore(dayjs().startOf("day"))}
            onChange={(dates) =>
              setRange(dates && dates[0] && dates[1] ? [dates[0], dates[1]] : null)
            }
          />
        </div>

        {estimatedTotal !== null && (
          <p style={{ margin: 0 }}>
            {nights} night{nights > 1 ? "s" : ""} × €{selectedRoom?.price} ={" "}
            <strong>€{estimatedTotal.toFixed(2)}</strong>
          </p>
        )}
      </Modal>
    </div>
  );
}
