import { Button, Result } from "antd";
import { Link } from "react-router-dom";

export default function NotFound() {
  return (
    <Result
      status="404"
      title="404"
      subTitle="Sorry, the page you are looking for does not exist."
      extra={
        <Link to="/">
          <Button type="primary">Back to home</Button>
        </Link>
      }
    />
  );
}
