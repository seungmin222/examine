import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import svgr from '@svgr/rollup';
import path from 'path';

export default defineConfig({
  plugins: [react(), svgr()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 5173,       // 원하는 포트 고정
    strictPort: true, // 이미 사용 중이면 에러 내고 종료
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
});
