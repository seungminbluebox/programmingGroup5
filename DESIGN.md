---
name: AZIT Design System

colors:

  # Primary — indigo
  primary: "#4f46e5"           # blue/59 · logo, CTA, active nav, progress fill
  primary-alt: "#5b4fe8"       # blue/61 · gradient pair, hover variant
  primary-mid: "#667eea"       # blue/66 · gradient accent, lighter indigo

  # Text
  text-primary: "#111827"      # azure/11 · headings, body
  text-dark: "#1a1a2e"         # blue/14 · deepest text, hero sections
  text-muted: "#6b7280"        # grey/46 · secondary labels, timestamps, metadata
  text-light: "#9ca3af"        # azure/65 · placeholder, disabled

  # Surface & Border
  surface: "#ffffff"
  border: "#e5e7eb"            # grey/91 · card borders, dividers, input strokes
  border-indigo: "#c7d2fe"     # blue/89 · indigo-tinted stroke (active inputs, focus rings)
  border-green: "#bbf7d0"      # spring green/85 · green-tinted stroke

  # Overlay
  overlay-black-5: "rgba(0,0,0,0.05)"   # subtle shadow fill

  # Pastel chip backgrounds (tags, status badges, role chips)
  chip-green: "#dcfce7"        # grey/93
  chip-red: "#fee2e2"          # grey/94
  chip-purple: "#f3e8ff"       # grey/95
  chip-orange: "#fff7ed"       # grey/96
  chip-indigo: "#eef2ff"       # grey/97
  chip-violet: "#f5f3ff"       # grey/98
  chip-yellow: "#fef9c3"       # yellow/88

  # Accent — solid (icons, dots, data viz)
  accent-violet-dark: "#764ba2"   # violet/46 · dark purple
  accent-violet: "#7c3aed"        # violet/58 · vivid violet
  accent-violet-light: "#a78bfa"  # violet/76 · soft violet
  accent-violet-pale: "#c4b5fd"   # blue/85 · very light violet
  accent-green-dark: "#16a34a"    # spring green/36 · dark green (text on chip-green)
  accent-green: "#22c55e"         # spring green/45 · mid green
  accent-green-bright: "#43e97b"  # spring green/59 · vivid green (charts, highlights)
  accent-green-light: "#6ee7b7"   # spring green/67 · soft green
  accent-red: "#ef4444"           # red/60 · error, destructive
  accent-red-light: "#fca5a5"     # red/82 · soft red
  accent-orange: "#f59e0b"        # orange/50 · warning, in-progress
  accent-orange-dark: "#854d0e"   # orange/29 · dark orange (text on chip-orange)
  accent-rose: "#ec4899"          # rose/60 · pink accent
  accent-rose-light: "#f472b6"    # rose/70 · soft pink
  accent-blue: "#3b82f6"          # azure/60 · azure blue
  accent-blue-mid: "#60a5fa"      # azure/68 · lighter blue
  accent-blue-light: "#93c5fd"    # azure/78 · pale blue
  accent-blue-deep: "#0369a1"     # azure/32 · deep azure
  accent-cyan: "#11998e"          # cyan/33 · teal/cyan
  accent-yellow: "#fee140"        # yellow/62 · bright yellow
  accent-magenta: "#f093fb"       # magenta/78 · magenta

typography:
  font-family: "Pretendard Variable, Pretendard, Inter, Noto Sans KR"  # 한글/영문 단일 패밀리

  # Font sizes (px) — 소수점 값은 artifact로 판단하여 제거
  font-size:
    xs:   9px
    sm:   10px
    sm+:  11px
    base: 12px
    md:   13px
    md+:  14px
    lg:   15px
    lg+:  16px
    xl:   17px
    xl+:  18px
    2xl:  19px
    3xl:  20px
    4xl:  22px
    5xl:  24px
    6xl:  26px
    7xl:  28px
    8xl:  30px
    9xl:  36px
    10xl: 48px

  # Font weights
  font-weight:
    regular:   400
    medium:    500
    semibold:  600
    bold:      700
    extrabold: 800
    black:     900

  # Letter spacing (px)
  letter-spacing:
    tight-xl:  -2px
    tight-lg:  -1px
    tight-md:  -0.5px
    tight-sm:  -0.3px
    normal:    0
    wide-sm:   0.2px
    wide-md:   0.3px
    wide-xl:   2px

  # Line heights (px) — font-size × ratio로 계산된 값
  line-height:
    xs:  14.4px   # ~120% of 12px
    sm:  15.6px   # ~120% of 13px
    md:  19.2px   # ~120% of 16px
    lg:  22.4px   # ~140% of 16px
    xl:  31.2px   # ~130% of 24px
    2xl: 33px
    3xl: 40px

# 소수점 대형 값(59.52, 178.33 등)은 html.to.design 자동 측정 artifact — 전부 제거
spacing:
  xxxs: 2px
  xxs:  4px
  xs:   8px
  s:    16px
  s+:   24px
  # 추가 정수 단계 (컴포넌트 내부 패딩 등에서 관찰됨)
  1:  1px
  3:  3px
  5:  5px
  6:  6px
  7:  7px
  9:  9px
  10: 10px
  12: 12px
  14: 14px
  15: 15px
  18: 18px
  20: 20px
  22: 22px
  28: 28px
  34: 34px

radius:
  sm:   8px
  md:   14px
  lg:   24px
  full: 9999px

# 분수 값(1.08, 1.33, 1.67 등)은 SVG 스케일링 artifact — 제거
stroke:
  thin: 1px
  base: 1.5px
  thick: 2px

dimensions:
  width:
    icon-xs: 13px
    icon-sm: 14px
    icon-md: 15px
    icon-lg: 17px
    avatar:  67px
    sidebar: 160px
    panel:   480px
  height:
    header:  130px
    canvas:  880px
    page:    1200px
---

# AZIT Design System

## Overview

AZIT is a Korean side-project team matching platform — a space where developers, designers, and PMs find each other to build together. The design centers on **calm productivity**: surfaces that stay quiet so that project and people data can speak clearly.

> **Note on token provenance**: This design system was extracted from a Figma file that was originally built using `html.to.design`. As a result, the raw variable export contained a significant number of auto-generated artifacts — fractional stroke weights from SVG scaling, large decimal spacing values from layout measurements, and near-duplicate color tokens from multiple import passes. All such values have been removed or consolidated. Only tokens with clear semantic intent have been retained.

---

## Colors

### Primary Palette

The primary color family is a **deep indigo** that reads as trustworthy and focused — not the cold corporate blue of legacy SaaS, and not the trendy purple of consumer apps. It sits precisely at the intersection.

- **Primary** (`#4f46e5`): The single most important color. Used for the logo, primary CTAs, active navigation states, and progress bar fills. Every occurrence implies interactivity or active state — it is never used decoratively.
- **Primary Alt** (`#5b4fe8`): A slightly warmer indigo, used as the gradient end-stop alongside primary (`#4f46e5 → #5b4fe8`). Also used as a hover variant for primary elements.
- **Primary Mid** (`#667eea`): A lighter indigo for gradient accents and secondary visual hierarchy within indigo contexts.

### Text

- **Text Primary** (`#111827`): Near-black with no blue tint. Used for all body text and headings.
- **Text Dark** (`#1a1a2e`): A deep navy-black. Reserved for the darkest elements — hero text, strong emphasis.
- **Text Muted** (`#6b7280`): Medium gray for secondary information: timestamps, metadata, helper labels.
- **Text Light** (`#9ca3af`): Lightest text, used for placeholders and disabled states only.

### Surface & Border

- **Surface** (`#ffffff`): All card and panel backgrounds.
- **Border** (`#e5e7eb`): The universal separator — card borders, table dividers, input outlines.
- **Border Indigo** (`#c7d2fe`): Indigo-tinted stroke for active input states and focus rings.
- **Border Green** (`#bbf7d0`): Green-tinted stroke for success/active states in green contexts.

### Pastel Chip Backgrounds

Used exclusively for tags, role chips, and status badges. Always paired with a matching darker solid accent for text:

| Token | Value | Text color to pair |
|---|---|---|
| `chip-green` | `#dcfce7` | `accent-green-dark` `#16a34a` |
| `chip-red` | `#fee2e2` | `accent-red` `#ef4444` |
| `chip-purple` | `#f3e8ff` | `accent-violet` `#7c3aed` |
| `chip-orange` | `#fff7ed` | `accent-orange-dark` `#854d0e` |
| `chip-indigo` | `#eef2ff` | `primary` `#4f46e5` |
| `chip-violet` | `#f5f3ff` | `accent-violet` `#7c3aed` |
| `chip-yellow` | `#fef9c3` | `accent-orange` `#f59e0b` |

---

## Typography

One font family handles everything:

- **Pretendard Variable** (or static **Pretendard**) covers both Korean and Latin characters with excellent weight coverage across 100–900. It is optically optimized for mixed Korean/English UI text and eliminates the need for separate font stacks per language.

Use `Pretendard Variable` when variable font support is available (modern browsers, most native environments). Fall back to static `Pretendard` otherwise.

```css
font-family: "Pretendard Variable", Pretendard, Inter, "Noto Sans KR", sans-serif;
```

### Type Scale

The scale is continuous from 9px to 48px. Key levels in practice:

| Token | Size | Weight | Usage |
|---|---|---|---|
| `xs` | 9px | 500 | Fine print, tiny labels |
| `sm+` | 11px | 500 | Column headers, section markers |
| `base` | 12px | 400 | Captions, timestamps |
| `md+` | 14px | 400/500 | Body text, list items |
| `lg+` | 16px | 600 | Card headings |
| `xl+` | 18px | 600 | Section headings |
| `5xl` | 24px | 700 | Page titles |
| `9xl` | 36px | 700 | Large stat numbers |
| `10xl` | 48px | 800 | Hero numbers |

### Rules

- Do not use bold (700+) for body text. Semibold (600) is the maximum for anything below heading level.
- Tight letter-spacing (`-0.3` to `-1px`) is applied to large display sizes (24px+) to compensate for optical looseness. Normal or slightly wide spacing for small labels.

---

## Spacing

The semantic scale uses a named system (`xxxs` → `s+`) for component-level spacing, with raw integer values available for fine-tuning layout internals.

| Token | Value | Common use |
|---|---|---|
| `xxxs` | 2px | Icon-to-label gap, inline badge padding |
| `xxs` | 4px | Tight internal padding |
| `xs` | 8px | Default item gap, small padding |
| `s` | 16px | Card internal padding, section rhythm |
| `s+` | 24px | Section-to-section gap, page margins |

---

## Border Radius

Three radius values cover the full range of components:

- **`sm` (8px)**: Buttons, input fields, small chips. The default interactive element radius.
- **`md` (14px)**: Cards, dropdowns, modal containers. Feels friendly without being bubbly.
- **`lg` (24px)**: Large surface panels, hero cards, featured sections.
- **`full` (9999px)**: Pills, avatar rings, toggle tracks.

---

## Stroke

Only three stroke weights are used intentionally:

- **`thin` (1px)**: Borders on cards, inputs, dividers.
- **`base` (1.5px)**: Icon strokes.
- **`thick` (2px)**: Emphasized borders, active input outlines.

> All fractional stroke values in the original export (e.g. 1.08px, 1.33px, 1.67px) are SVG scaling artifacts from the html.to.design import and carry no semantic meaning.

---

## Components

### Tags & Role Chips

Always use the pastel chip background with its matching text color. Fully rounded (`radius: full`). Two sizes:
- **SM**: 12px text, `xxs`/`xs` padding — inline within list rows
- **MD**: 13px text, `xs`/`s` padding — filter selectors, profile badges

Never use a solid accent color as a chip background — always use the pastel variant.

### Buttons

- **Primary**: `primary` fill, white text, `radius: sm`. One per view maximum.
- **Outlined**: `border` stroke, `text-primary` label, same radius.
- **Ghost**: No background, no border, `primary` text — low-emphasis inline actions.

### Cards

- Background: `surface`
- Border: `1px solid border`
- Radius: `md` (14px)
- Internal padding: `s` (16px)
- No heavy shadow — background contrast between `surface` and the page background creates the elevation.

### Progress Bars

- Track: `border` (`#e5e7eb`)
- Fill: `primary` (`#4f46e5`)
- Height: 6px (standard), 4px (compact list variant)
- Radius: `full`

---

## Do's and Don'ts

**Do:**
- Use `primary` exclusively for interactive or active-state elements
- Always pair pastel chip backgrounds with their matching darker text color
- Use `Pretendard Variable, Pretendard, Inter, "Noto Sans KR", sans-serif` as the single font stack for all text
- Apply tight letter-spacing (`-0.3` to `-1px`) on text 24px and above

**Don't:**
- Don't use accent colors (violet, green, red) for interactive elements — those belong to `primary`
- Don't use font-weight 700+ for body or label text
- Don't use fractional stroke weights — round to 1, 1.5, or 2px
- Don't create new spacing values outside the defined scale