export default {
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  plugins: {
    tailwindcss: {},
    autoprefixer: {},
    daisyui: {},
  },
  daisyui: {
    themes: ["light", "dark", "cupcake"],
  },
}
