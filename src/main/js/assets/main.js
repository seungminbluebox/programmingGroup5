import { defineCustomElement } from "vue";

function pascalCaseToKebabCase(pascalCase) {
  return pascalCase
    .split("/")
    .at(-1)
    .replace(/\.ce\.vue$/, "")
    .replace(/([a-z0-9])([A-Z])/g, "$1-$2")
    .replace(/([A-Z])([A-Z][a-z])/g, "$1-$2")
    .toLowerCase();
}

for(const [path, module] of Object.entries(
  import.meta.glob("@/components/**/*.ce.vue", { eager: true }),
)) {
  const { default: VComponent } = module;
  const name = pascalCaseToKebabCase(VComponent.name || path);
  const VCustomElement = defineCustomElement(VComponent);

  customElements.define(name, VCustomElement);
}

