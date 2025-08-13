/** @type {import('tailwindcss').Config} */
export default {
  content: ["./frontend/src/**/*.{html,js,jsx,ts,tsx}"],
  safelist: [
    'S',
    'theme-blue',
    'theme-green',
    'theme-gray',
    'theme-lavender',
      'm-0',
      'mt-4',
  ],
  theme: {
    extend: {},
  },
  plugins: [],
}

