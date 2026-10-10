import { Component } from "react";
import type { ErrorInfo, ReactNode } from "react";
import { Button, Result } from "antd";

interface Props {
  children: ReactNode;
}

interface State {
  hasError: boolean;
}

/** Shows a friendly screen instead of a blank page when a component crashes. */
export default class ErrorBoundary extends Component<Props, State> {
  state: State = { hasError: false };

  static getDerivedStateFromError(): State {
    return { hasError: true };
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error("Unhandled UI error:", error, info.componentStack);
  }

  render() {
    if (this.state.hasError) {
      return (
        <Result
          status="error"
          title="Something went wrong"
          subTitle="An unexpected error occurred. Reloading the page usually fixes it."
          extra={
            <Button type="primary" onClick={() => window.location.assign("/")}>
              Back to home
            </Button>
          }
        />
      );
    }
    return this.props.children;
  }
}
