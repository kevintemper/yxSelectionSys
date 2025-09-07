// vite.config.ts
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // 默认接口（如果页面其他代码还在请求 /api，会走 gray 这边）
      '/api': {
        target: 'http://localhost:9101', // gray 作为默认
        changeOrigin: true,
        rewrite: (p) => p.replace(/^\/api/, '/api'),
      },

      // 显式灰度
      '/gray-api': {
        target: 'http://localhost:9101',
        changeOrigin: true,
        rewrite: (p) => p.replace(/^\/gray-api/, ''),
      },

      // 显式生产
      '/prd-api': {
        target: 'http://localhost:9103',
        changeOrigin: true,
        rewrite: (p) => p.replace(/^\/prd-api/, ''),
      },
    },
  },
});
