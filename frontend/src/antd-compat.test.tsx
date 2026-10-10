import "./antd-compat";
import { screen } from "@testing-library/react";
import { message } from "antd";
import { expect, it } from "vitest";

// Regression test: without the React 19 patch, toasts are never shown to the user.
it("static antd toasts are actually rendered on React 19", async () => {
  message.success("saved!");
  expect(await screen.findByText("saved!")).toBeInTheDocument();
});
