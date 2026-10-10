import { StrictMode, useEffect, useState } from "react";
import { BrowserRouter } from "react-router-dom";
import App from "./App.tsx";
import ErrorBoundary from "./components/ErrorBoundary";

import { ConfigProvider, theme, App as AntApp } from "antd";
import enUS from "antd/locale/en_US";
import { IntlProvider } from "react-intl";
import { ProConfigProvider } from "@ant-design/pro-provider";
import "antd/dist/reset.css";

const DARK_MODE_KEY = "darkMode";

export default function Root() {
  // remember the choice; first visit follows the OS setting
  const [darkMode, setDarkMode] = useState<boolean>(() => {
    const saved = localStorage.getItem(DARK_MODE_KEY);
    return saved !== null
      ? saved === "true"
      : window.matchMedia?.("(prefers-color-scheme: dark)").matches ?? false;
  });

  useEffect(() => {
    localStorage.setItem(DARK_MODE_KEY, String(darkMode));
  }, [darkMode]);

  return (
    <StrictMode>
      <IntlProvider locale="en">
        <ConfigProvider
          locale={enUS}
          theme={{
            algorithm: darkMode ? theme.darkAlgorithm : theme.defaultAlgorithm,
            token: {
              colorPrimary: "#1677ff",
              borderRadius: 6,
            },
          }}
        >
          <ProConfigProvider dark={darkMode}>
            <AntApp>
              <ErrorBoundary>
                <BrowserRouter>
                  <App setDarkMode={setDarkMode} darkMode={darkMode} />
                </BrowserRouter>
              </ErrorBoundary>
            </AntApp>
          </ProConfigProvider>
        </ConfigProvider>
      </IntlProvider>
    </StrictMode>
  );
}

