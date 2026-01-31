<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
</script>

<template>
  <a-config-provider
    :theme="{
      token: {
        colorPrimary: '#f28c28',
        colorLink: '#f28c28',
        colorInfo: '#f28c28',
      },
    }"
  >
    <BasicLayout />
  </a-config-provider>
</template>

<style>
/* 全局样式优化 */
* {
  box-sizing: border-box;
}

body {
  margin: 0;
  padding: 0;
  font-family:
    -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen', 'Ubuntu', 'Cantarell',
    'Fira Sans', 'Droid Sans', 'Helvetica Neue', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  overflow-x: hidden;
}

html {
  overflow-x: hidden;
}

:root {
  --brand: #f28c28;
  --brand-700: #d4751f;
  --ink: #1b1b1b;
  --muted: #6b7280;
  --paper: #ffffff;
  --paper-2: rgba(255, 255, 255, 0.72);
  --stroke: rgba(17, 24, 39, 0.10);
  --shadow-m: 0 18px 55px rgba(17, 24, 39, 0.14);
  --shadow-s: 0 6px 18px rgba(17, 24, 39, 0.12);
}
</style>
