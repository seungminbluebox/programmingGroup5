import { fileURLToPath, URL } from "node:url";

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
    outDir: fileURLToPath(new URL("./target/classes/static/assets", import.meta.url)),
    rolldownOptions: {
      input: [
        fileURLToPath(new URL("./src/main/js/assets/main.js", import.meta.url)),
        fileURLToPath(new URL("./src/main/js/assets/main.css", import.meta.url)),
      ],
      output: {
        entryFileNames: `[name].js`,
        chunkFileNames: `[name].js`,
        assetFileNames: `[name].[ext]`,
      },
    },
  },
  publicDir: fileURLToPath(new URL("./src/main/resources/static", import.meta.url)),
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src/main/js", import.meta.url)),
    },
  },
  define: {
    "process.env.NODE_ENV": JSON.stringify(mode),
    __VUE_PROD_DEVTOOLS__: mode === "development",
  },
}));
