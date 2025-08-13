/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./src/main/resources/static/**/*.{html,js}"],
  safelist: [
    'S',
    'theme-blue',
    'theme-green',
    'theme-gray',
    'theme-lavender',
    '-right-2',
    'left-16',
    'top-1',
      'mr-1',
      'm-0',
    'mt-4',
      'text-ellipsis',
      'break-normal',
      '!whitespace-nowrap',
      'search-dropdown',
      'z-50',
      'w-64'
  ],
  theme: {
    extend: {},
  },
  plugins: [],
}

