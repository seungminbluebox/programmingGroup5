import { fileURLToPath, URL } from "node:url";
import { globSync } from "node:fs";

import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import vueDevTools from "vite-plugin-vue-devtools";
import vueCustomElements from "vite-plugin-vue-ce";

// https://vite.dev/config/
export default defineConfig(({ mode }) => ({
  plugins: [vue(), vueCustomElements(), vueDevTools()],
  root: fileURLToPath(new URL("./src/main/resources/", import.meta.url)),
  build: {
    minify: mode !== "development",
    sourcemap: mode !== "production",
    outDir: fileURLToPath(new URL("./target/classes/", import.meta.url)),
    emptyOutDir: true,
    rolldownOptions: {
      input: [
        ...globSync('./src/main/resources/templates/*.html'),
        ...globSync('./src/main.resources/static/*.html')
      ]
    }
  },
  publicDir: fileURLToPath(new URL("./src/main/resources/static", import.meta.url)),
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src/main/resources/", import.meta.url)),
    },
  },
  define: {
    "process.env.NODE_ENV": JSON.stringify(mode),
    __VUE_PROD_DEVTOOLS__: mode === "development",
  },
}));
