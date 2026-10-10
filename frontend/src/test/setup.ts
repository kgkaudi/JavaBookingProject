import "@testing-library/jest-dom/vitest";
// same React 19 patch the app loads in main.tsx, so toasts render in tests too
import "../antd-compat";
import { cleanup } from "@testing-library/react";
import { afterEach } from "vitest";

afterEach(() => {
  cleanup();
  localStorage.clear();
});

// antd relies on these browser APIs, which jsdom does not provide
Object.defineProperty(window, "matchMedia", {
  writable: true,
  value: (query: string) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: () => {},
    removeListener: () => {},
    addEventListener: () => {},
    removeEventListener: () => {},
    dispatchEvent: () => false,
  }),
});

class ResizeObserverStub {
  observe() {}
  unobserve() {}
  disconnect() {}
}
globalThis.ResizeObserver = ResizeObserverStub as unknown as typeof ResizeObserver;

// jsdom does not implement getComputedStyle(element, pseudoElement) and logs an error for it;
// antd uses that form for scrollbar measurements, so ignore the pseudo-element argument.
const realGetComputedStyle = window.getComputedStyle.bind(window);
window.getComputedStyle = (element: Element) => realGetComputedStyle(element);
