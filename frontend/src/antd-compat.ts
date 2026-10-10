// antd v5 does not support React 19 out of the box: static helpers such as
// message.success() / message.error() silently render nothing.
// This official patch fixes that. It must be imported before anything else renders.
import "@ant-design/v5-patch-for-react-19";
