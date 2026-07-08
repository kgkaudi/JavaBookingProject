// frontend/src/pages/Home.tsx
import React from "react";
import { Button, Card, Row, Col, Typography } from "antd";
import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const { Title, Paragraph } = Typography;

export default function Home() {
  const { isAdmin } = useAuth();

  return (
    <div style={{ padding: 24 }}>
      <Row gutter={[24, 24]} justify="center">
        <Col xs={24} sm={20} md={16} lg={12}>
          <Card bordered={false} style={{ textAlign: "center" }}>
            <Title level={2} style={{ marginBottom: 8 }}>
              Welcome to BookingProject
            </Title>
            <Paragraph style={{ fontSize: 16, color: "rgba(0,0,0,0.65)" }}>
              Book rooms quickly, manage your reservations, and explore
              available rooms.
            </Paragraph>

            <Row justify="center" gutter={12} style={{ marginTop: 20 }}>
              <Col>
                <Link to="/rooms">
                  <Button type="primary">Browse Rooms</Button>
                </Link>
              </Col>
              <Col>
                <Link to="/bookings">
                  <Button>My Bookings</Button>
                </Link>
              </Col>
              {isAdmin && (
                <Col>
                  <Link to="/admin/users">
                    <Button>Admin</Button>
                  </Link>
                </Col>
              )}
            </Row>
          </Card>
        </Col>
      </Row>

      <Row gutter={[24, 24]} style={{ marginTop: 32 }}>
        <Col xs={24} md={12}>
          <Card title="How it works" bordered={false}>
            <Paragraph>
              Search available rooms, pick dates, and confirm your booking.
              Manage bookings from your profile.
            </Paragraph>
          </Card>
        </Col>

        <Col xs={24} md={12}>
          <Card title="Tips" bordered={false}>
            <Paragraph>
              Use the calendar to choose start and end dates. Check availability
              before creating a booking.
            </Paragraph>
          </Card>
        </Col>
      </Row>
    </div>
  );
}
