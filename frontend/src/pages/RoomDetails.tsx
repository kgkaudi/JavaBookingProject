import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { Button, Card, Descriptions, Spin, Tag, message } from "antd";
import { getRoom } from "../api/rooms";
import { getErrorMessage } from "../api/errors";
import type { Room } from "../types";

export default function RoomDetails() {
  const { id } = useParams();
  const [room, setRoom] = useState<Room | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) return;

    let active = true;

    getRoom(id)
      .then((data) => {
        if (active) setRoom(data);
      })
      .catch((err) => message.error(getErrorMessage(err, "Failed to load room details")))
      .finally(() => {
        if (active) setLoading(false);
      });

    return () => {
      active = false;
    };
  }, [id]);

  if (loading) {
    return (
      <div style={{ padding: 24 }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!room) {
    return (
      <div style={{ padding: 24 }}>
        <Card>
          <h2>Room not found</h2>
          <Link to="/rooms">Back to rooms</Link>
        </Card>
      </div>
    );
  }

  return (
    <div style={{ padding: 24 }}>
      <Card
        title={`Room ${room.roomNumber}`}
        extra={
          room.available && (
            <Link to={`/bookings?roomId=${room.id}`}>
              <Button type="primary">Book this room</Button>
            </Link>
          )
        }
      >
        <Descriptions bordered column={1}>
          <Descriptions.Item label="Room Number">{room.roomNumber}</Descriptions.Item>
          <Descriptions.Item label="Type">{room.type}</Descriptions.Item>
          <Descriptions.Item label="Capacity">{room.capacity}</Descriptions.Item>
          <Descriptions.Item label="Price per night">€{room.price}</Descriptions.Item>
          <Descriptions.Item label="Status">
            {room.available ? (
              <Tag color="green">Available</Tag>
            ) : (
              <Tag color="red">Unavailable</Tag>
            )}
          </Descriptions.Item>
        </Descriptions>
      </Card>
    </div>
  );
}
