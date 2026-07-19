/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        ink: '#172033',
        line: '#d8dee8',
        surface: '#f6f8fb',
      },
      boxShadow: {
        panel: '0 12px 35px rgba(23, 32, 51, 0.08)',
      },
    },
  },
  plugins: [],
};
