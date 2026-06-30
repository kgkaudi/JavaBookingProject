import { StrictMode, useState } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import App from "./App.tsx";

import { ConfigProvider, theme, App as AntApp } from "antd";
import enUS from "antd/locale/en_US";
import "antd/dist/reset.css";

function Root() {
  const [darkMode, setDarkMode] = useState(false);

  return (
    <StrictMode>
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
        <AntApp>
          <BrowserRouter>
            <App setDarkMode={setDarkMode} darkMode={darkMode} />
          </BrowserRouter>
        </AntApp>
      </ConfigProvider>
    </StrictMode>
  );
}

createRoot(document.getElementById("root")!).render(<Root />);
