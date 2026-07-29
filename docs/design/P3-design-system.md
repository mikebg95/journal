# Warm Graphite — Design System

The design system for the Dev Master Plan portfolio. Dual theme, light and dark.
Established in P2 (Recipe Book), applied to P3 (Notes & Journal) and onward.

This document is the source of truth. Where an implementation disagrees with it, the implementation is wrong.

---

## 1. Principles

**Warm neutrals only.** Every neutral in both themes satisfies `R ≥ G > B`. Light greys run bone and paper; dark greys run graphite and olive. There is no blue anywhere in the greyscale. This single constraint is what gives the system its identity, and it is the easiest thing to lose by accident.

**Elevation is lighter, in both themes.** Three surfaces, `0` furthest back and `2` most elevated. Light mode tops out at pure white; dark mode at the palest graphite. The direction never inverts — a card is always lighter than the screen it sits on, regardless of theme.

**No shadows.** Depth comes from surface value plus a hairline border. There are no drop shadows, no glows, no gradients. The only permitted `box-shadow` in the entire system is the keyboard focus ring.

**Hairlines, not lines.** Borders are `0.5px` and sit at roughly `1.25:1` against their surface. They separate regions without drawing attention. A visible, contrasting border is out of system.

**Nothing bolder than 500.** Hierarchy is carried by size and colour. There is no bold text anywhere. This is most of why the system reads calm rather than assertive.

**One accent, theme-invariant.** A single muted emerald and a single clay red. Both are identical in light and dark — only the neutrals flip. The accent appears sparingly: brand mark, primary action, section icons, key numerals.

---

## 2. Colour

### Tokens

```css
:root{
  /* surfaces — 0 furthest back, 2 most elevated */
  --surface-0:#efede7;
  --surface-1:#f6f5f1;
  --surface-2:#ffffff;

  /* text */
  --text-primary:#1b1b19;
  --text-secondary:#6a695f;
  --text-muted:#9b998f;

  /* lines */
  --border:#e6e4dc;
  --border-strong:#d7d5cc;

  /* accent */
  --brand:#0E9F6E;
  --brand-strong:#0B7D55;
  --danger:#D65A54;
}

@media (prefers-color-scheme:dark){
  :root:not([data-theme=light]){
    --surface-0:#191918;
    --surface-1:#212120;
    --surface-2:#2a2a27;

    --text-primary:#f1efe8;
    --text-secondary:#a7a59c;
    --text-muted:#77756e;

    --border:#38382f;
    --border-strong:#4a4943;

    --brand-strong:#34C88F;
  }
}

:root[data-theme=dark]{
  --surface-0:#191918; --surface-1:#212120; --surface-2:#2a2a27;
  --text-primary:#f1efe8; --text-secondary:#a7a59c; --text-muted:#77756e;
  --border:#38382f; --border-strong:#4a4943;
  --brand-strong:#34C88F;
}
```

`--brand` and `--danger` are deliberately not themed. `--brand-strong` **is** themed — it is a text colour used on a tinted background, and a single value cannot serve both themes.

### Reference

| Token | Light | Dark |
|---|---|---|
| `--surface-0` | `#efede7` | `#191918` |
| `--surface-1` | `#f6f5f1` | `#212120` |
| `--surface-2` | `#ffffff` | `#2a2a27` |
| `--text-primary` | `#1b1b19` | `#f1efe8` |
| `--text-secondary` | `#6a695f` | `#a7a59c` |
| `--text-muted` | `#9b998f` | `#77756e` |
| `--border` | `#e6e4dc` | `#38382f` |
| `--border-strong` | `#d7d5cc` | `#4a4943` |
| `--brand` | `#0E9F6E` | `#0E9F6E` |
| `--brand-strong` | `#0B7D55` | `#34C88F` |
| `--danger` | `#D65A54` | `#D65A54` |

### Surface assignment

Fixed. Do not improvise.

| Element | Surface |
|---|---|
| Page body | `--surface-0` |
| Screen body | `--surface-1` |
| Caption strip | `--surface-1` |
| App header bar | `--surface-2` |
| Card, panel | `--surface-2` |
| Modal | `--surface-2` |
| Input | `--surface-2`, border `--border-strong` |
| Pill, badge | `--surface-1` |

A pill sits one surface step **below** its parent card. It is recessed, not raised. This inversion is a defining detail of the system.

### Tints and overlays

Alpha, never a baked hex — so a tint composites correctly on any surface in either theme.

```css
--tint-brand:rgba(14,159,110,0.12);
--tint-brand-soft:rgba(14,159,110,0.10);
--tint-danger:rgba(214,90,84,0.12);
--tint-warn:rgba(201,146,46,0.12);
--scrim:rgba(0,0,0,0.5);
```

Resolved for reference: `--tint-brand` is `#E2F3EE` on white, `#273830` on `#2a2a27`.

### Semantic extension

For states that are neither success nor failure — a degraded result, a stale value, a recoverable conflict.

```css
--warn:#C9922E;          /* light text-on-tint: #8A6216 */
--warn-strong:#8A6216;   /* dark  text-on-tint: #E0B25A */
```

Warn is not danger. A system that degraded correctly has not failed, and rendering it in red teaches the user to distrust red.

---

## 3. Typography

```css
font-family:-apple-system,BlinkMacSystemFont,"Segoe UI",Roboto,Helvetica,Arial,sans-serif;
line-height:1.5;
```

The system font stack. No webfont, no display face. Personality comes from palette and spacing.

| Role | Size | Weight | Colour |
|---|---|---|---|
| Sheet title | 15 | 400 | `--text-muted` |
| Caption strip | 12 | 400 | `--text-muted` |
| Table column header | 12 | 500 | `--text-muted` |
| Helper text, counter | 12 | 400 | `--text-muted` |
| Field label | 12.5 | 500 | `--text-secondary` |
| Pill, number chip | 12.5 | 500 | `--text-secondary` |
| Inline field error | 12.5 | 400 | `--danger` |
| Small button | 13 | 500 | inherit |
| Card body, list item | 13.5 | 400 | `--text-primary` |
| Modal body | 13.5 | 400 | `--text-secondary` |
| Body, input | 14 | 400 | `--text-primary` |
| Button | 14 | 500 | inherit |
| Table row label | 14.5 | 500 | `--text-primary` |
| Wordmark | 15 | 500 | `--text-primary` |
| Terminal-screen title | 15.5 | 500 | `--text-primary` |
| Modal title | 16 | 500 | `--text-primary` |
| Empty-state title | 17 | 500 | `--text-primary` |
| Page heading | 21 | 500 | `--text-primary` |
| Display numeral | 34 | 500 | `--brand` |

500 is the ceiling. Sentence case throughout. Fractional sizes are intentional.

**Tabular numerals** are required on counts, quantities, dates, versions, and paginated ranges:

```css
font-variant-numeric:tabular-nums;
```

**Prose that a user wrote** renders at `line-height:1.6` with `white-space:pre-wrap`. Line breaks, blank lines and indentation are content, not formatting.

---

## 4. Spacing

Even values are preferred but odd values are used freely where optical balance calls for it. These are the established measurements.

| Measurement | Value |
|---|---|
| Content max-width | 760 |
| Body padding | `32px 16px` |
| Frame bottom margin | 24 |
| Caption padding | `9px 14px` |
| Header bar padding | `14px 20px` |
| Screen body padding | `22px 20px 26px` |
| Card padding | 16 |
| Two-column gap | 16 |
| Table header padding | `12px 16px` |
| Table row padding | `14px 16px` |
| Label → field | 6 |
| Field → error message | 6 |
| Section heading → content | 14 |
| List item gap | 11–14 |
| Button icon gap | 7 |
| Button group gap | 8–10 |
| Overlay padding | 28 |
| Modal padding | 24 |
| Modal max-width | 340 |
| Empty-state padding | `44px 20px` |
| Terminal-screen padding | `40px 18px` |

The screen body padding is asymmetric — `22` top, `20` sides, `26` bottom.

---

## 5. Radii

| Element | Radius |
|---|---|
| Frame, modal, 60px tile | 16 |
| Card, 54px tile | 14 |
| Button, input | 10 |
| Brand mark (30px) | 9 |
| Number chip (24px) | 8 |
| Icon tile (48px) | 12 |
| Pill | 20 — fully rounded |

---

## 6. Borders and elevation

```css
border:0.5px solid var(--border);          /* structural: frames, cards, dividers */
border:0.5px solid var(--border-strong);   /* interactive: inputs, secondary buttons */
```

There is no elevation scale, because there are no shadows. The complete depth system:

```css
.layer{background:var(--surface-N);border:0.5px solid var(--border)}
```

Modals do not float. They sit on a scrim at `--surface-2`, the same surface as a card.

---

## 7. Components

### Frame
```css
border:0.5px solid var(--border);border-radius:16px;overflow:hidden;
margin-bottom:24px;background:var(--surface-2)
```

### Caption strip
`font-size:12px` · `color:var(--text-muted)` · `padding:9px 14px` · `background:var(--surface-1)` · `border-bottom:0.5px solid var(--border)`

### Header bar
`display:flex` · `align-items:center` · `justify-content:space-between` · `padding:14px 20px` · `background:var(--surface-2)` · `border-bottom:0.5px solid var(--border)`

### Brand mark
`30×30` · `border-radius:9px` · `background:var(--brand)` · `color:#fff` · centred icon at `18px`. Wordmark beside it at `15px/500`, `gap:10px`.

### Buttons

```css
.btn{display:inline-flex;align-items:center;gap:7px;padding:9px 15px;
     border-radius:10px;font-size:14px;font-weight:500;
     border:0.5px solid var(--border-strong);
     color:var(--text-primary);background:var(--surface-2)}
.btn.primary{background:var(--brand);color:#fff;border-color:var(--brand)}
.btn.danger{color:var(--danger);border-color:var(--danger);background:var(--surface-2)}
.btn.solid-danger{background:var(--danger);color:#fff;border-color:var(--danger)}
.btn.sm{padding:6px 11px;font-size:13px}
```

Resolved heights: default **40px**, small **33px**. Every variant carries a border, primary included. Leading icons at 15–16px.

Disabled: `opacity:.55` · `cursor:not-allowed` · no hover.

### Card
`background:var(--surface-2)` · `border:0.5px solid var(--border)` · `border-radius:14px` · `padding:16px`

A card used as a table wrapper takes `padding:0;overflow:hidden`.

### Section heading
`font-size:14px` · `font-weight:500` · `display:flex` · `align-items:center` · `gap:8px` · `margin-bottom:14px`. Leading icon `16px` in `--brand`.

### Field
```css
.lab{font-size:12.5px;font-weight:500;color:var(--text-secondary);margin-bottom:6px}
.in{height:40px;border:0.5px solid var(--border-strong);border-radius:10px;
    background:var(--surface-2);padding:0 13px;font-size:14px}
```
Compact variant `height:36px`. Multi-line: `align-items:flex-start`, `padding-top:11px`, `line-height:1.6`.
Placeholder `color:var(--text-muted)`.

**Invalid:** `border-color:var(--danger)`, and a message below at `margin-top:6px` — 14px alert-circle plus text, both `--danger` at `12.5px`, `gap:5px`. Copy is imperative and names the fix.

### Pill
```css
.pill{display:inline-flex;align-items:center;gap:5px;padding:3px 9px;
      border-radius:20px;background:var(--surface-1);
      border:0.5px solid var(--border);font-size:12.5px;color:var(--text-secondary)}
```
Optional leading icon at 13px. A tinted variant swaps `background` for a tint and `color` for the matching strong token. Never lighter than its parent card.

### Number chip
`24×24` · `border-radius:8px` · `background:var(--tint-brand)` · `color:var(--brand-strong)` · `12.5px/500` · `flex-shrink:0`

### Table
Header: `display:grid` · `padding:12px 16px` · `12px/500` · `--text-muted` · `border-bottom:0.5px solid var(--border)` · trailing 20px spacer column for the chevron.
Row: same grid · `padding:14px 16px` · `border-bottom:0.5px solid var(--border)` · label `14.5px/500`.
**The last row has no bottom border.**
Trailing chevron `18px` in `--text-muted`.

### Modal
```css
.overlay{background:var(--scrim);display:flex;align-items:center;
         justify-content:center;padding:28px;min-height:270px}
.modal{background:var(--surface-2);border-radius:16px;padding:24px;
       max-width:340px;width:100%;text-align:center}
```
Vertical stack: `48×48` tile (`border-radius:12px`, tint background, 22px icon, `margin:0 auto 14px`) → title `16px/500` (`margin-bottom:6px`) → body `13.5px --text-secondary` (`margin-bottom:20px`) → button row `display:flex;gap:10px`, each button `flex:1;justify-content:center`.

Closes on Escape and on the cancel action. Does not close on overlay click where unsaved work is at stake.

### Empty state
Centred, `padding:44px 20px`. `60×60` tile (`border-radius:16px`, `--tint-brand-soft`, 28px `--brand` icon, `margin:0 auto 18px`) → title `17px/500` (`margin-bottom:6px`) → line `14px --text-secondary` (`margin-bottom:20px`) → primary button.

An empty state is an invitation. It states what will appear and offers the action that produces it.

### Terminal screens
Centred, `padding:40px 18px`.
Error: `54×54` tile, `border-radius:14px`, `background:var(--surface-1)`, 26px icon in `--text-secondary`, `margin-bottom:16px`; title `15.5px/500`; primary button.
Not-found: `34px/500` numeral in `--brand`; title `15.5px/500` at `margin:6px 0 16px`; primary button.

### Toast
Fixed, 24px from the bottom-left. `background:var(--surface-2)` · `border-radius:14px` · `border:0.5px solid var(--border)` · `border-left:2px solid` in the semantic colour · `padding:14px 16px` · `max-width:380px`. Title `13.5px/500`, body `12.5px --text-secondary`, trailing 14px dismiss. Auto-dismisses. Never blocks input.

A toast reports something that already succeeded or something recoverable. Anything requiring a decision is a modal.

### Progress
A `2px` `--brand` indeterminate bar on the top edge of the frame. No overlay, no blocking spinner, no scrim. Content stays visible and readable while work is in flight.

### Icons
Lucide. `viewBox="0 0 24 24"` · `fill="none"` · `stroke="currentColor"` · `stroke-width="2"` · round caps and joins.
Always `style="vertical-align:-0.15em;flex-shrink:0"`.
Sizes: 12 · 13 · 14 · 15 · 16 · 18 · 22 · 26 · 28.

Colour: `--text-muted` decorative, `--text-secondary` interactive, `--brand` inside brand tiles, `--danger` destructive. Accent-coloured icons belong in tiles, not loose on a surface.

---

## 8. Layout

```
body                 --surface-0 · padding 32/16
└ wrap 760
  ├ sheet title      15 · --text-muted · mb 28
  └ frame            --surface-2 · r16 · 0.5px · mb 24
    ├ caption        --surface-1
    └ screen         --surface-1
      ├ header bar   --surface-2
      └ body         padding 22/20/26
        └ card       --surface-2 · r14 · p16
          └ pill     --surface-1
```

Two-column content uses `grid-template-columns:1fr 1fr` with `gap:16px`, collapsing to one column below 640px.

---

## 9. Interaction

```css
.btn:hover{background:var(--surface-1);border-color:var(--border-strong)}
.btn.primary:hover{background:#0D9166}
.btn.solid-danger:hover{background:#C9524C}
.btn.danger:hover{background:var(--tint-danger)}
.btn:active{filter:brightness(.96)}

.row:hover{background:var(--surface-1)}
.frame:hover{border-color:var(--border-strong)}
.in:focus-within{border-color:var(--brand)}

:focus-visible{outline:none;
  box-shadow:0 0 0 2px var(--surface-2),0 0 0 4px var(--brand)}

*{transition:background-color .15s ease,border-color .15s ease,color .15s ease}

@media (prefers-reduced-motion:reduce){
  *,*::before,*::after{transition-duration:.01ms!important;animation-duration:.01ms!important}
}
```

No transforms on hover. No scale, no lift, no translate — there is no shadow to lift out of. Transitions are limited to colour.

Confirmation of a completed action is one small, brief mark that fades. Never a celebration.

---

## 10. Contrast

Measured WCAG ratios for every text pairing in the system.

| Pairing | Light | Dark | Status |
|---|---|---|---|
| `--text-primary` on `--surface-2` | 17.25 | 12.51 | AAA |
| `--text-secondary` on `--surface-1` | 5.07 | 6.53 | AA |
| `--brand-strong` on `--tint-brand` | 4.48 | 5.79 | AA |
| `--danger` on `--surface-1` | 3.85 | 4.18 | AA at ≥14px/500 |
| `#fff` on `--brand` | 3.39 | 3.39 | AA-large only |
| `#fff` on `--danger` | 3.85 | 3.85 | AA-large only |
| `--text-muted` on `--surface-1` | 2.62 | 3.49 | decorative only |
| `--border` on `--surface-2` | 1.27 | 1.22 | non-informational |

Three rules follow, and they are binding:

**Button labels are never smaller than 14px/500.** White on brand and white on danger clear AA-large at that size and weight. A 13px label on a filled button is out of system.

**`--text-muted` is decorative.** Column headers, captions, placeholders, and counters — text that repeats information available elsewhere or labels an obvious field. Never use it for content the user must read to proceed.

**Borders carry no information.** At ~1.25:1 they are invisible to many users. Never signal state with a border alone; pair it with colour, text, or an icon.

Where a stricter standard is required, these fills reach AA for normal text without disturbing the palette elsewhere:

```css
--brand-solid:#0B8C60;   /* #fff → 4.26:1 */
--danger-solid:#C4483F;  /* #fff → 4.83:1 */
```

---

## 11. Accessibility

- Keyboard focus is always visible, via the ring in §9. Never `outline:none` without a replacement.
- Reduced motion is respected.
- Every icon-only control carries an `aria-label` naming its effect, not its glyph.
- Emoji never carry meaning alone. A text label accompanies them.
- Modals trap focus, restore it on close, and respond to Escape.
- Asynchronous regions use `aria-busy`; the indeterminate bar is `role="progressbar"`.
- Toasts are `role="status"` with `aria-live="polite"`. Never `role="alert"` — they are not interruptions.
- Interactive elements are never nested inside other interactive elements. A row containing its own controls manages `tabindex` rather than wrapping everything in an anchor.
- Colour is never the only signal. Status is colour plus text.

---

## 12. Domain components

### Mood

A pill: emoji, then a lowercase label. Tint background at `0.12` alpha over the card; text in the themed strong value.

| Value | Pill | Base | Light text | Dark text |
|---|---|---|---|---|
| `HAPPY` | 😊 happy | `#0E9F6E` | `#0A7350` | `#34C88F` |
| `CALM` | 😌 calm | `#4E8C96` | `#3A6E77` | `#7FBFC9` |
| `NEUTRAL` | 😐 neutral | — | `--text-secondary` | `--text-secondary` |
| `ANXIOUS` | 😰 anxious | `#C9922E` | `#8A6216` | `#E0B25A` |
| `SAD` | 😔 sad | `#6E8FA8` | `#4E6C85` | `#9DB9CE` |
| `FRUSTRATED` | 😤 frustrated | `#D65A54` | `#B33F3A` | `#E88A85` |

Worst case 4.6:1 light, 5.0:1 dark. `HAPPY` reuses `--brand`; `FRUSTRATED` reuses `--danger`; `NEUTRAL` is the plain pill. Three new hues total.

A null mood renders nothing. No placeholder, no dash.

### Tag

A pill with `--tint-brand` and themed brand text, always lowercase, always sorted alphabetically. Where a set is truncated, the overflow indicator is a plain pill reading `+3`.

An active filter tag is a filled pill: `background:var(--brand)`, `color:#fff`.

A removable tag carries a trailing 12px `×` in `--text-muted` with an `aria-label` naming what it removes.

### Status

| State | Pill | Colour |
|---|---|---|
| Complete | `✓ analysed` | `--tint-brand` / `--brand-strong` |
| Not run | `not analysed yet` | `--tint-warn` / `--warn-strong` |
| Stale | `analysis out of date` | `--tint-warn` / `--warn-strong` |

Never `--danger`. A degraded state is warned, not errored.

### Generated content

A card with `border-left:2px solid var(--brand)` and body text at `13.5px --text-secondary`. The left edge marks the content as machine-produced; no heading is needed. Where the content is stale, a `12px --text-muted` caveat sits beneath it.

### User prose

`white-space:pre-wrap` · `font-size:14px` · `line-height:1.6`. Never normalised, never collapsed, never truncated in a detail view.

### Counter

`12px --text-muted`, right-aligned, tabular. Hidden until the value reaches 90% of the limit, then `--warn` at 97%. A counter visible from the first character is discouraging.

### Autocomplete

Below the field: `--surface-2` · `border-radius:10px` · `0.5px --border` · maximum 8 rows at `padding:8px 12px` and `13.5px`. The matched substring renders `--text-primary/500`, the remainder `--text-secondary`. Active row `--surface-1`. Arrow keys navigate, Enter selects. An empty result is a single `12.5px --text-muted` row naming the query.

---

## 13. Voice

Words are design material. They exist to make the interface easier to use.

- Sentence case everywhere. No title case. No exclamation marks.
- Controls are verb plus object — `Save changes`, `New entry`, `Back to home`. The verb survives the whole flow: `Delete` produces `Entry deleted`.
- Name what the person controls, never how the system is built. `Not analysed yet`, never a status enum.
- Empty states invite and say what will appear.
- Errors state what happened and what to do. They do not apologise and they are never vague.
- Where work was preserved, say so first. Reassurance precedes explanation.
- One job per element. A label labels; an example demonstrates; helper text explains a rule that isn't obvious.
- Short lines. `This can't be undone.` is the register.

---

## 14. Compliance checklist

- [ ] Every neutral satisfies `R ≥ G > B`, in both themes
- [ ] No `box-shadow` other than the focus ring
- [ ] All borders `0.5px`
- [ ] No `font-weight` above 500
- [ ] System font stack, no webfont
- [ ] Radii from §5 only
- [ ] Pills fully rounded and never lighter than their parent
- [ ] `--brand-strong` themed
- [ ] Filled-button labels at 14px/500 or larger
- [ ] `--text-muted` used decoratively only
- [ ] Numerals tabular where they represent quantity, date, or version
- [ ] Last table row has no bottom border
- [ ] User prose renders `pre-wrap` at `line-height:1.6`
- [ ] Status conveyed by colour and text together
- [ ] Focus visible, reduced motion respected, Escape closes modals
- [ ] Both themes verified, including scrim and tints
