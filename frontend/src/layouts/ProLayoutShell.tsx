import { Outlet, useNavigate, Link } from "react-router-dom";
import { ProLayout } from "@ant-design/pro-components";
import {
  LogoutOutlined,
  HomeOutlined,
  BookOutlined,
  UserOutlined,
  MoonOutlined,
  SunOutlined,
  TeamOutlined,
  DashboardOutlined,
} from "@ant-design/icons";
import { Avatar, Dropdown, message } from "antd";
import { useAuth } from "../context/AuthContext";

const BREADCRUMB_NAMES: Record<string, string> = {
  "/": "Home",
  "/rooms": "Rooms",
  "/bookings": "Bookings",
  "/profile": "Profile",
  "/admin": "Admin Panel",
  "/admin/users": "Users",
  "/admin/rooms": "Rooms",
  "/admin/bookings": "Bookings",
};

interface ProLayoutShellProps {
  setDarkMode: (value: boolean) => void;
  darkMode: boolean;
}

export default function ProLayoutShell({ setDarkMode, darkMode }: ProLayoutShellProps) {
  const navigate = useNavigate();
  const { user, isAdmin, logout } = useAuth();

  const initial = user?.name?.charAt(0)?.toUpperCase() || "?";

  const avatarMenu = {
    items: [
      {
        key: "profile",
        icon: <UserOutlined />,
        label: "Profile",
        onClick: () => navigate("/profile"),
      },
      {
        key: "logout",
        icon: <LogoutOutlined />,
        label: "Logout",
        onClick: async () => {
          await logout(); // also invalidates the token on the server
          message.info("Logged out");
          navigate("/login");
        },
      },
    ],
  };

  // ---------------------------------------------------------
  // ROUTES CONFIGURATION
  // ---------------------------------------------------------
  const baseRoutes = [
    { path: "/", name: "Home", icon: <HomeOutlined /> },
    { path: "/rooms", name: "Rooms", icon: <BookOutlined /> },
    { path: "/bookings", name: "Bookings", icon: <BookOutlined /> },
    { path: "/profile", name: "Profile", icon: <UserOutlined /> },
  ];

  const adminRoutes = [
    {
      path: "/admin",
      name: "Admin Panel",
      icon: <DashboardOutlined />,
      routes: [
        { path: "/admin/users", name: "Users", icon: <TeamOutlined /> },
        { path: "/admin/rooms", name: "Rooms", icon: <BookOutlined /> },
        { path: "/admin/bookings", name: "Bookings", icon: <BookOutlined /> },
      ],
    },
  ];

  return (
    <ProLayout
      title="BookingProject"
      logo="/booking.svg"
      layout="mix"
      fixedHeader
      navTheme={darkMode ? "realDark" : "light"}
      contentStyle={{ minHeight: "calc(100vh - 64px)", padding: 24 }}
      route={{
        path: "/",
        routes: isAdmin ? [...baseRoutes, ...adminRoutes] : baseRoutes,
      }}
      menuItemRender={(item, dom) => <Link to={item.path || "/"}>{dom}</Link>}
      breadcrumbRender={(routers = []) =>
        routers.map((r) => {
          const path = r.path ?? "";

          if (path.startsWith("/rooms/")) {
            return { ...r, breadcrumbName: `Room ${path.split("/")[2]}` };
          }

          return { ...r, breadcrumbName: BREADCRUMB_NAMES[path] ?? r.breadcrumbName };
        })
      }
      actionsRender={() => [
        darkMode ? (
          <SunOutlined
            key="light"
            style={{ fontSize: 18, cursor: "pointer" }}
            onClick={() => setDarkMode(false)}
          />
        ) : (
          <MoonOutlined
            key="dark"
            style={{ fontSize: 18, cursor: "pointer" }}
            onClick={() => setDarkMode(true)}
          />
        ),
      ]}
      avatarProps={{
        size: "small",
        render: () => (
          <Dropdown menu={avatarMenu}>
            <Avatar
              style={{
                backgroundColor: "#1890ff",
                fontWeight: 600,
                fontSize: 16,
                cursor: "pointer",
              }}
            >
              {initial}
            </Avatar>
          </Dropdown>
        ),
      }}
    >
      <Outlet />
    </ProLayout>
  );
}
