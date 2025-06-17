/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./src/main/resources/static/**/*.{html,js}"],
  safelist: [
    'S',
    'theme-blue',
    'theme-green',
    'theme-gray',
    'theme-lavender',
  ],
  theme: {
    extend: {},
  },
  plugins: [],
}

