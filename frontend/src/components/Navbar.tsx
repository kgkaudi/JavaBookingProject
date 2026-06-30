import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Navbar() {
  const { token, logout } = useAuth();

  return (
    <div className="navbar bg-base-100/80 backdrop-blur-md border-b border-base-300">
      <div className="flex-1">
        <Link to="/" className="text-xl font-bold text-primary">
          BookingProject
        </Link>
      </div>

      <div className="flex-none gap-4">
        <ul className="menu menu-horizontal px-1">
          <li><Link to="/rooms">Rooms</Link></li>
          <li><Link to="/bookings">Bookings</Link></li>
          <li><Link to="/about">About</Link></li>
        </ul>

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
