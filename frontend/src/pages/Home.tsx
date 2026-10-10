import { Button, Card, Row, Col, Typography } from "antd";
import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const { Title, Paragraph } = Typography;

export default function Home() {
  const { isAdmin, user } = useAuth();

  return (
    <div style={{ padding: 24 }}>
      <Row gutter={[24, 24]} justify="center">
        <Col xs={24} sm={20} md={16} lg={12}>
          <Card variant="borderless" style={{ textAlign: "center" }}>
            <Title level={2} style={{ marginBottom: 8 }}>
              Welcome{user?.name ? `, ${user.name}` : ""}
            </Title>
            <Paragraph style={{ fontSize: 16 }} type="secondary">
              Book rooms quickly, manage your reservations, and explore available rooms.
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
                  <Link to="/admin">
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
          <Card title="How it works" variant="borderless">
            <Paragraph>
              Search available rooms, pick dates, and confirm your booking. Manage or cancel
              bookings from the My Bookings page.
            </Paragraph>
          </Card>
        </Col>

        <Col xs={24} md={12}>
          <Card title="Tips" variant="borderless">
            <Paragraph>
              Pick your check-in and check-out day in the calendar. The check-out day is free for the
              next guest, so you can book back-to-back stays.
            </Paragraph>
          </Card>
        </Col>
      </Row>
    </div>
  );
}
