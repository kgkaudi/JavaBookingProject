import { Outlet, useNavigate, Link } from "react-router-dom";
import { ProLayout } from "@ant-design/pro-components";
import {
  LogoutOutlined,
  HomeOutlined,
  BookOutlined,
  UserOutlined,
  SettingOutlined,
  MoonOutlined,
  SunOutlined,
} from "@ant-design/icons";
import { Avatar, Dropdown, message } from "antd";

export default function ProLayoutShell({ setDarkMode, darkMode }) {
  const navigate = useNavigate();

  const avatarMenu = {
    items: [
      {
        key: "profile",
        icon: <UserOutlined />,
        label: "Profile",
        onClick: () => navigate("/profile"),
      },
      {
        key: "settings",
        icon: <SettingOutlined />,
        label: "Settings",
        onClick: () => navigate("/settings"),
      },
      {
        key: "logout",
        icon: <LogoutOutlined />,
        label: "Logout",
        onClick: () => {
          localStorage.removeItem("token");
          message.info("Logged out");
          navigate("/login");
        },
      },
    ],
  };

  return (
    <ProLayout
      title="BookingProject"
      logo="https://ant.design/assets/logo.svg"
      layout="mix"
      fixedHeader
      navTheme={darkMode ? "dark" : "light"}
      contentStyle={{ minHeight: "calc(100vh - 64px)", padding: 24 }}
      route={{
        path: "/",
        routes: [
          { path: "/", name: "Home", icon: <HomeOutlined /> },
          { path: "/rooms", name: "Rooms", icon: <BookOutlined /> },
          { path: "/bookings", name: "Bookings", icon: <BookOutlined /> },
          { path: "/profile", name: "Profile", icon: <UserOutlined /> },
        ],
      }}
      menuItemRender={(item, dom) => <Link to={item.path || "/"}>{dom}</Link>}
      breadcrumbRender={(routers = []) => {
        return routers.map((r) => {
          // Static mapping
          const nameMap: Record<string, string> = {
            "/": "Home",
            "/rooms": "Rooms",
            "/bookings": "Bookings",
            "/profile": "Profile",
          };

          // Dynamic room detail: /rooms/:id
          if (r.path?.startsWith("/rooms/")) {
            const id = r.path.split("/")[2];
            return {
              ...r,
              breadcrumbName: `Room ${id}`,
            };
          }

          return {
            ...r,
            breadcrumbName: nameMap[r.path] || r.breadcrumbName || r.name,
          };
        });
      }}
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
        src: "https://api.dicebear.com/7.x/identicon/svg?seed=user",
        size: "small",
        render: (_, avatar) => (
          <Dropdown menu={avatarMenu}>
            <div style={{ cursor: "pointer" }}>{avatar}</div>
          </Dropdown>
        ),
      }}
    >
      <Outlet />
    </ProLayout>
  );
}
