import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { Card, Descriptions, Spin, message, Tag } from "antd";
import api from "../api/axios";

export default function RoomDetails() {
  const { id } = useParams();
  const [room, setRoom] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadRoom = async () => {
      try {
        const res = await api.get(`/api/rooms/${id}`);
        setRoom(res.data);
      } catch {
        message.error("Failed to load room details");
      } finally {
        setLoading(false);
      }
    };

    loadRoom();
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
        </Card>
      </div>
    );
  }

  return (
    <div style={{ padding: 24 }}>
      <Card title={`Room ${room.roomNumber}`}>
        <Descriptions bordered column={1}>
          <Descriptions.Item label="Room Number">
            {room.roomNumber}
          </Descriptions.Item>

          <Descriptions.Item label="Type">
            {room.type}
          </Descriptions.Item>

          <Descriptions.Item label="Capacity">
            {room.capacity}
          </Descriptions.Item>

          <Descriptions.Item label="Price per night">
            €{room.price}
          </Descriptions.Item>

          <Descriptions.Item label="Status">
            {room.available ? (
              <Tag color="green">Available</Tag>
            ) : (
              <Tag color="red">Booked</Tag>
            )}
          </Descriptions.Item>
        </Descriptions>
      </Card>
    </div>
  );
}
