import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Navbar() {
  const { token, user, isAdmin, logout } = useAuth();

  return (
    <div className="navbar bg-base-100/80 backdrop-blur-md border-b border-base-300 px-4">
      {/* Left side: Brand */}
      <div className="flex-1">
        <Link to="/" className="text-xl font-bold text-primary">
          BookingProject
        </Link>
      </div>

      {/* Right side: Navigation */}
      <div className="flex-none gap-4">
        <ul className="menu menu-horizontal px-1">
          {/* Always visible */}
          <li><Link to="/rooms">Rooms</Link></li>

          {/* Logged-in users */}
          {token && (
            <>
              <li><Link to="/bookings">Bookings</Link></li>
              <li><Link to="/profile">Profile</Link></li>
            </>
          )}

          {/* Admin-only links */}
          {isAdmin && (
            <>
              <li><Link to="/admin">Admin Panel</Link></li>
              <li><Link to="/admin/users">Users</Link></li>
              <li><Link to="/admin/rooms">Rooms</Link></li>
              <li><Link to="/admin/bookings">Bookings</Link></li>
            </>
          )}
        </ul>

        {/* Auth buttons */}
        {token ? (
          <button
            onClick={logout}
            className="btn btn-outline btn-error btn-sm"
          >
            Logout
          </button>
        ) : (
          <Link to="/login" className="btn btn-primary btn-sm">
            Login
          </Link>
        )}
      </div>
    </div>
  );
}
