# P3 — Journal · Screen Specification

Written companion to `P3-design-system.md`. Every screen, every state, every string.
Twenty-one frames across seven pages. Build order is the order below.

---

## 0. Ground rules

**Vocabulary.** The user-facing noun is **entry**, never "note", "journal entry", or "record". The app is **Journal**. Actions: *new entry · save · edit · delete · analyse again · filter · search*.

**Data available to the UI.** From the schema — nothing else exists, so nothing else can be shown:

| Field | Type | Nullable | Origin |
|---|---|---|---|
| `id` | bigint | no | system |
| `version` | bigint | no | system — optimistic locking |
| `title` | varchar(100) | **no** | user |
| `content` | text, <20 000 | **no** | user |
| `summary` | varchar(500) | yes | AI |
| `mood` | enum(6) | yes | AI |
| `created_at` | timestamp | no | system |
| `last_updated` | timestamp | no | system |
| `analysed_at` | timestamp | yes | system |
| `tags` | set of varchar(50), lowercase | may be empty | AI |
| `todos` | ordered list of varchar(1000) | may be empty | AI |

**Derived states.** Computed in the domain, exposed as booleans by the API. The UI never compares timestamps itself.

| State | Rule | Badge |
|---|---|---|
| Analysed | `analysed_at IS NOT NULL AND analysed_at >= last_updated` | `✓ analysed` — brand |
| Not analysed | `analysed_at IS NULL` | `not analysed yet` — amber |
| Out of date | `analysed_at IS NOT NULL AND analysed_at < last_updated` | `analysis out of date` — amber |

Any edit bumps `last_updated`, so correcting a typo in the title marks the analysis out of date. Accepted over-eagerness — the alternative is content hashing.

**Moods.** Rendered as a pill: emoji + lowercase label.

| Value | Pill | Base hue |
|---|---|---|
| `HAPPY` | 😊 happy | `#0E9F6E` (brand) |
| `CALM` | 😌 calm | `#4E8C96` |
| `NEUTRAL` | 😐 neutral | none — plain pill |
| `ANXIOUS` | 😰 anxious | `#C9922E` |
| `SAD` | 😔 sad | `#6E8FA8` |
| `FRUSTRATED` | 😤 frustrated | `#D65A54` (danger) |

`mood = NULL` renders nothing at all — no placeholder, no dash.

**Tags** are stored canonical-lowercase, so they always display lowercase. Order is alphabetical everywhere, because the set has no intrinsic order.

**Todos** have no completion flag. They are extracted text, rendered as a plain list. No checkboxes anywhere.

Order is `id` ascending — the order the writer mentioned things, since the model reads the entry top to bottom. This requires an explicit `ORDER BY`; without one the order is whatever the database finds convenient and can change between reads. Tags are an unordered set, to-dos are an ordered list: same relationship shape, different collection type, each matching its own semantics.

**Two failure types, never conflated.**

| Failure | HTTP | Entry saved? | Treatment |
|---|---|---|---|
| Analysis failed | `201` / `200` | **yes** | Navigate on, amber badge, non-blocking toast |
| Request failed | `4xx` `5xx` timeout | **no** | Stay put, keep the user's text, inline error |

---

## 1. Shared furniture

### App bar
`--surface-2` · `padding 14px 20px` · `border-bottom 0.5px --border`.
Left: 30×30 brand mark (`radius 9`, book icon 18px, `#fff`) + wordmark **Journal** at `15px/500`.
Right: page-dependent actions.

On sub-pages the left slot becomes a `.btn.sm` **← Back** instead of the logo.

### Primary navigation

Two top-level pages exist, so the app bar carries navigation. It sits immediately after the wordmark with `margin-left 24`, `flex · gap 18`.

Each item is text at `14px/500`. Active: `--text-primary` with a `2px --brand` bottom border, `padding-bottom 3`. Inactive: `--text-secondary`, no border, hover `--text-primary`.

Items: **Entries** · **To-dos**

The To-dos item carries a trailing count as a plain pill when any exist: `To-dos 7`. Omitted at zero, because a zero badge is noise.

Navigation appears only on the two top-level pages. Sub-pages (new, view, edit) replace the whole left slot with **← Back**, so there is never both a Back button and a nav row.

### Toolbar (Home only)
Sits at the top of `.main`, below the `.h1`. `flex · gap 10 · align-items center`:

1. **Search field** — `.in` at `height 36`, `flex 1`, leading 15px magnifier in `--text-muted`, placeholder `Search entries`.
2. **Filter button** — `.btn` with 15px funnel icon, label `Filter`. When filters are active: `background --tint-brand`, `border-color --brand`, label `Filter · 3`.
3. **New entry** — `.btn.primary` with 16px plus.

### Active-filter chip row
Renders only when at least one filter is set. `margin-top 12` · `flex-wrap · gap 8 · align-items center`.
Each chip is a `.pill` carrying its own removal affordance: a trailing 12px `×` in `--text-muted`. Mood chips use their mood tint; tag chips use `--tint-brand`; the search chip uses the plain pill and reads `“deploy”`.
Trailing text button **Clear all** at `12.5px --text-secondary`, no border, no background.

### Pagination
Bottom of the table, `margin-top 14` · `flex · space-between · align-items center`.
Left: `1–20 of 47` at `12.5px --text-muted`, tabular numerals.
Right: two `.btn.sm` chevron buttons. Disabled state `opacity .45`, no pointer.
Hidden entirely when the total fits one page.

### Toast
Fixed bottom-left, 24px from both edges. `--surface-2` · `radius 14` · `0.5px --border` · `padding 14px 16px` · `max-width 380` · `border-left 2px` in the semantic colour. Title `13.5px/500`, body `12.5px --text-secondary`. Trailing 14px `×`. Auto-dismiss after 6s. **Never blocks input.**

### Modal
Overlay `rgba(0,0,0,0.5)` · centred · `padding 28`. Panel `--surface-2` · `radius 16` · `padding 24` · `max-width 340` · centred text.
Stack: 48×48 tile (`radius 12`, tint, 22px icon) → `margin-bottom 14` → title `16px/500` → `mb 6` → body `13.5px --text-secondary` → `mb 20` → button row `gap 10`, both `flex:1`.
Dismissible by Escape and by the Cancel button. **Not** by clicking the overlay — same reasoning as the editor.

---

## 2. Home

### Frame 1 — Home, populated

The default landing screen, and the most important frame in the set: **every column except the title is AI-generated output**, so the table is the demonstration of the feature.

**App bar.** Logo left. Nothing right — the New entry action lives in the toolbar so it sits next to search and filter.

**Body.** `.main` padding `22px 20px 26px`.
- `.h1` **Your journal** at `21px/500`, `margin-bottom 16`.
- Toolbar (§1).
- Table, wrapped in a `.card` with `padding:0; overflow:hidden`.

**Table grid.** `2.2fr 1.4fr 0.9fr 1.5fr 0.5fr auto`

| Column | Align | Content |
|---|---|---|
| Title | left | `14.5px/500 --text-primary`, single line, ellipsis at overflow |
| Summary | left | `13px --text-secondary`, truncated to ~7 words + `…` |
| Mood | centre | mood pill, or empty |
| Tags | left | first two tags alphabetically as `--tint-brand` pills, then a `+3` plain pill |
| Todos | centre | plain pill, 13px checklist icon + count. Omitted when zero. |
| Updated | right | `12.5px --text-muted`, tabular, `14 Mar` (year only if not current) |
| — | right | 18px chevron in `--text-muted`, 20px column |

Header row: `12px/500 --text-muted`, `padding 12px 16px`. Labels: **Title · Summary · Mood · Tags · To-dos · Updated**.
Body rows: `padding 14px 16px`, `border-bottom 0.5px --border`, **last row none**.

**Sort.** `last_updated` descending, fixed. No sortable headers.

**Summary truncation** happens server-side or in the mapper, not with CSS — it must be a genuine word-boundary cut at ~7 words so the ellipsis lands cleanly.

**When `summary IS NULL`** the cell shows `not analysed yet` as an amber pill, not blank. This is the one place the status badge appears on Home, and it keeps the column from looking broken.

**Row interaction.** Whole row is the click target → Frame 11. Hover `background --surface-1`. Cursor pointer. Keyboard: rows are focusable, Enter activates.

**Tag pills are also click targets** — clicking one adds it to the tag filter and reloads. That's the primary discovery path for filtering, so tag pills get their own hover (`border-color --brand`) and a tooltip `Filter by "work"`.

---

### Frame 2 — Home, filtered

Identical to Frame 1 plus:
- Filter button in its active treatment: `--tint-brand` fill, `--brand` border, label `Filter · 3`.
- Chip row visible beneath the toolbar showing every active constraint: `😊 happy ×` · `work ×` · `deployment ×` · `“deploy” ×` · **Clear all**.
- `.h1` unchanged. The result count lives in the pagination line, which now reads `1–4 of 4`.

Shows the table with fewer rows so the filtering is legible as an effect.

---

### Frame 3 — Home, empty (first run)

App bar with logo only — **no toolbar**. Searching and filtering nothing is meaningless, so the entire toolbar is suppressed. This is the one screen where New entry moves into the empty state itself.

Centred column, `padding 44px 20px`:
- 60×60 tile, `radius 16`, `rgba(14,159,110,0.10)`, 28px open-book icon in `--brand`, `margin 0 auto 18px`
- **Start your journal** — `17px/500`
- **Write your first entry and it'll show up here.** — `14px --text-secondary`, `margin-bottom 20`
- `.btn.primary` **+ New entry**

An invitation, not a report of absence.

---

### Frame 4 — Home, no matches

Distinct from Frame 3 and this distinction matters — a user who can't tell "no results" from "no entries" is stuck.

Toolbar **stays visible** with its active treatment, and the chip row stays. Only the table is replaced:

- 54×54 tile, `radius 14`, `--surface-1`, 26px magnifier in `--text-secondary`
- **No entries match these filters** — `15.5px/500`
- **Try removing a filter or searching for something else.** — `14px --text-secondary`
- `.btn` **Clear all filters** — secondary, not primary. The primary action on this screen is still New entry, up in the toolbar.

---

### Frame 5 — Filter modal

Wider than the standard modal: `max-width 440`, `text-align left`, `padding 24`.

**Header.** **Filter entries** at `16px/500`, with a trailing 16px `×` in `--text-muted`, top-right.

**Section — Mood.** Label `12.5px/500 --text-secondary`, `margin-bottom 10`.
Six chips in a `flex-wrap · gap 8` grid, each a `.pill` at `padding 5px 11px` with emoji + label.
- Unselected: `--surface-1` bg, `--border`, `--text-secondary`
- Selected: mood tint bg, mood-coloured border, mood-coloured text, plus a 13px leading check

Helper line beneath, `12px --text-muted`: **Matches entries with any of the moods you pick.**

`border-top 0.5px --border`, `margin 18px 0`.

**Section — Tags.** Label **Tags**.
- Selected tags render first as removable `--tint-brand` pills, `flex-wrap · gap 8`, `margin-bottom 10`
- `.in` at `height 36`, placeholder `Add a tag`
- On focus/typing, a suggestion list drops below: `--surface-2` · `radius 10` · `0.5px --border` · max 8 rows, each `padding 8px 12px` at `13.5px`, matched substring in `--text-primary/500` and the rest `--text-secondary`. Hover/active row `--surface-1`. Keyboard ↑↓ and Enter.
- No match → single row `12.5px --text-muted`: `No tag matches "xyz"`

Helper line: **Matches entries that have all the tags you pick.**

The two helper lines are load-bearing — they explain why mood is OR and tags is AND without the user needing to guess. The asymmetry follows from cardinality: an entry has one mood but many tags.

**Footer.** `border-top 0.5px --border`, `padding-top 18`, `flex · space-between`.
- Left: text button **Clear all** — `13px --text-secondary`, no border
- Right: `.btn` **Cancel** + `.btn.primary` **Show 4 entries** with a live count

**Applies on confirm, not live.** The modal covers the table, so live filtering would update something invisible. The count on the button delivers that feedback instead, and a real Cancel becomes possible.

Search is deliberately **not** in this modal — it lives in the toolbar (§1). Search is high-frequency and low-commitment; filtering is occasional and deliberate.

---

## 3. New entry

Full page, not a modal. Click-outside-to-dismiss is disabled entirely — losing a journal entry to a stray click is the same failure as losing it to a backend outage, and a confirm dialog is weak mitigation for something that shouldn't be reachable.

### Frame 6 — New entry, empty

**App bar.** Left `.btn.sm` **← Back**. Right: nothing.

**Body.** `.main` padding `22px 20px 26px`.
- `.h1` **New entry**, `margin-bottom 18`
- Field **Title** — `.lab` `12.5px/500 --text-secondary`, `mb 6`; `.in` `height 40`, `maxlength 100`, placeholder `Give it a title`
- Field **Entry** — `.lab`; textarea `min-height 260`, `padding 12px 13px`, `line-height 1.6`, placeholder `Write whatever's on your mind.`
- Footer row, `margin-top 22`, `flex · space-between`:
  - Left: `.btn` **Cancel**
  - Right: `.btn.primary` **Save and analyse** with a 16px check icon

**No AI fields on this page.** Summary, mood, tags and to-dos do not exist yet and there is nothing to show. The page has exactly two inputs.

**Button label discloses the cost.** `Save and analyse` tells the user this does more than save and will take a moment — which is what a confirmation dialog would have said, without the dialog. There is no save confirmation.

**Character counter.** Hidden until `content` passes 18 000. Then bottom-right of the textarea, `12.5px --text-muted`, `18,204 / 20,000`, tabular. Past 19 500 it turns amber. At 20 000 input stops.

---

### Frame 7 — New entry, validation error

Title empty on submit. Matches the P2 pattern exactly.
- `.in` border becomes `--danger`
- Message below at `margin-top 6`: 14px alert-circle + **Enter a title**, both `--danger` at `12.5px`, `gap 5`
- Focus moves to the field
- Button returns to its resting state

Content empty behaves identically with **Write something first**. Both rules are client-side *and* server-side; the message is imperative and specific, never "This field is required."

---

### Frame 8 — New entry, saving

The whole reason the spinner lives here rather than on the next page. ADR-0002 makes the AI call part of the save request, so until it returns there is no entry — navigating first would mean rendering a detail page for something that doesn't exist.

- 2px `--brand` indeterminate bar along the **top edge of the frame**, sliding left to right
- Primary button disabled: `opacity .55`, `cursor not-allowed`, label **Analysing…** with a 15px spinner replacing the check
- Cancel disabled
- Both inputs `readonly`, unchanged in appearance — the user's text stays visible and legible throughout, which is the point
- Below the button row, `12.5px --text-muted`: **This usually takes a few seconds.**

No overlay, no scrim, no blocking spinner. The user can read what they wrote while they wait.

**On success →** Frame 11 or 12 depending on whether analysis succeeded.

---

### Frame 9 — New entry, request failed

The other failure. Nothing was saved, so the user must not leave.

- Everything returns to the editable state with the text intact
- A `--danger` inline banner above the footer row: `--surface-2` · `radius 10` · `0.5px --danger` · `padding 12px 14px` · `border-left 2px --danger`. 15px alert icon + **Couldn't save your entry** at `13.5px/500`, then `12.5px --text-secondary`: **Check your connection and try again. Your text is still here.**
- Button re-enabled, label unchanged

That last sentence is the whole job of this frame.

---

### Frame 10 — Discard confirmation

Triggered by **Cancel** or **← Back** when either field has content. If both are empty, navigate away with no dialog — there is nothing to lose.

Standard modal. Amber tile (`rgba(201,146,46,.12)`, 22px alert-triangle).
- Title **Discard this entry?**
- Body **Your writing won't be saved.**
- Buttons: **Keep writing** (secondary) · **Discard** (`.btn.solid-danger`)

The safe option is on the left and reads as the obvious choice. The destructive verb matches the title.

---

## 4. View entry

### Frame 11 — View entry, analysed

The payoff screen. Everything below the title is machine-generated.

**App bar.** Left `.btn.sm` **← Back**. Right, `gap 8`: `.btn.sm` **Edit** (pencil) · `.btn.sm.danger` **Delete** (trash).

**Body.**
- `.h1` title
- Meta row, `margin 8px 0 20px`, `flex · gap 10 · align-items center · flex-wrap`:
  - `✓ analysed` pill — `--tint-brand`, themed brand text, 13px check
  - mood pill
  - `12.5px --text-muted`: **Created 12 Mar 2026**
  - **· Updated 14 Mar 2026** — appended only when `last_updated > created_at`. They are equal at creation, so printing both immediately would look like a bug.
- **Summary block** — `.card` with `border-left 2px --brand`, `13.5px --text-secondary`, `line-height 1.6`, `margin-bottom 20`. No heading; the brand edge identifies it as generated.
- **Entry content** — `.card`, `padding 16`, `14px/1.6 --text-primary`, `white-space: pre-wrap`. Every line break, blank line and leading space preserved. This is the CSS half of the data contract's promise about prose.
- Two-column `.grid2`, `gap 16`:
  - **Tags** card — section heading `14px/500` with a 16px tag icon in `--brand`. Body: `flex-wrap · gap 8` of `--tint-brand` pills, alphabetical, all of them (no overflow pill here — there is room).
  - **To-dos** card — section heading with a 16px checklist icon. Body: `flex-column · gap 11`. Each row `flex · gap 11 · align-items flex-start`: a 6px `--brand` dot at `margin-top 7`, then `13.5px/1.5` text that **wraps** — `todos.value` allows 1 000 characters, so truncation is not an option.

**Empty sub-states.** When `tags` is empty, the card renders its heading plus a single `13.5px --text-muted` line: **No tags.** Same for to-dos: **No to-dos.** The card is never an empty box. When both are empty *and* the entry is analysed, the `.grid2` collapses to a single full-width row containing one line: **The analysis didn't find any tags or to-dos.**

---

### Frame 12 — View entry, not analysed

Reached straight after a save where the AI failed. The entry is safe; the screen must say so first.

- Meta row: amber `not analysed yet` pill. **No mood pill** — mood is null.
- App bar gains a third action, first in the right group: `.btn.sm` **Analyse again** with a 15px refresh icon.
- **No summary block.** The element is absent, not an empty card.
- Content card renders normally — the user's writing is fully intact and prominent.
- `.grid2` is replaced by a single full-width `.card`, `padding 20`, centred, `--surface-2`:
  - 15px amber alert icon
  - `13.5px/500` **This entry hasn't been analysed yet**
  - `12.5px --text-secondary` **Summary, mood, tags and to-dos will appear here once it has.**
- **Toast**, bottom-left, amber left edge:
  > **Entry saved**
  > The analysis didn't finish. You can analyse it again anytime.

The toast leads with the reassurance. It is not a modal, because nothing is broken — the system did exactly what ADR-0005 designed it to do. Modal-ing a working degradation path is the UI equivalent of logging it at ERROR.

---

### Frame 13 — View entry, out of date

Content was edited after the last analysis. The summary describes text that no longer exists.

Identical to Frame 11 except:
- Meta row pill is amber and reads **analysis out of date**
- App bar carries **Analyse again**
- Summary block keeps its `--brand` left edge but the card gains `12.5px --text-muted` beneath it: **Based on an earlier version of this entry.**

The old analysis is still shown. It is stale, not wrong, and deleting it would lose information for no reason.

---

### Frame 14 — View entry, analysing

Re-analysis in progress, triggered from Frames 12 or 13.

- 2px `--brand` indeterminate bar on the frame's top edge
- **Analyse again** disabled, label **Analysing…**, 15px spinner
- Edit and Delete disabled
- Existing content untouched and readable. If a stale analysis is present it stays on screen until replaced — never blanked out in anticipation.

**On success:** bar disappears, the pill flips to `✓ analysed`, and the summary / mood / tags / to-dos regions cross-fade over 200ms. A brief `--brand` check appears in the meta row for ~1s, then fades. Restrained — one small confirmation, not a celebration.

**On failure:** toast with amber edge — **Analysis didn't finish** / **Nothing was changed. You can try again.** Any previous analysis remains exactly as it was. This is the case your spec was explicit about and it is the correct instinct: a failed re-analysis must never destroy a good previous result.

**Stale race.** If the entry's version moved while the analysis was running, the result is discarded silently — no dialog. The summary would describe text that no longer exists, and there is nobody watching to ask. `Analyse again` stays available.

---

## 5. Edit entry

### Frame 15 — Edit entry

A full page, visually distinct from View at a glance: every value sits in an input, and the app bar carries Save.

**App bar.** Left `.btn.sm` **← Back**. Right, `gap 8`: `.btn` **Cancel** · `.btn.primary` **Save changes**.

**Body.**
- `.h1` **Edit entry**
- Version chip, meta row: plain pill, `12.5px --text-muted`, tabular, **v3**. Present only here, because this is the only screen where the version is about to be submitted. It is the visible trace of optimistic locking.
- **Title** — `.in`, `maxlength 100`
- **Entry** — textarea `min-height 220`, counter rules as Frame 6
- `border-top 0.5px --border`, `margin 22px 0 18px`
- Sub-heading `12.5px/500 --text-secondary`: **Analysis** — with `12px --text-muted` beside it: **Generated by AI. You can change any of it.**
- **Summary** — textarea `height 80`, `maxlength 500`
- **Mood** — the same six-chip selector as the filter modal, single-select, plus a seventh plain chip **none** for clearing it back to null
- **Tags** — selected tags as removable `--tint-brand` pills, then the autocomplete `.in` from Frame 5. Free text is allowed and gets canonicalised on save, so `  Work ` silently becomes `work`.
- **To-dos** — each existing todo on a row: `.in` `flex 1` + a 16px trash in `--text-muted`. Below, an `.in` `height 36` placeholder **Add a to-do** with a `.btn.sm.primary` `+`. Matches the P2 Steps card pattern exactly.

**No Analyse again on this page.** Deliberately. With unsaved edits present, re-analysing is ambiguous — it would either analyse text the user can see they've changed, or silently save first and contradict the Save button. The action lives on View entry, where there is no unsaved state and the reprocess endpoint's by-id semantics match.

**Save with no changes** → navigate back to View with no request. Nothing to write, no version to bump.

**On save → Frame 11/12/13** for that entry, reflecting its new state.

---

## 6. Dialogs and terminal screens

### Frame 16 — Delete confirmation

From View entry. Standard modal, `--tint-danger` tile with a 22px trash icon.
- Title **Delete this entry?**
- Body **This can't be undone.**
- Buttons: **Cancel** (secondary) · **Delete** (`.btn.solid-danger`)

Nothing about tag lifecycle. A user at a delete prompt does not care what happens to the tag table, and explaining it would be solving an engineering documentation problem at the user's expense.

**On confirm:** delete, navigate to Home, and the row is gone. Optional toast **Entry deleted**. No undo — the copy already promised there wouldn't be one.

---

### Frame 17 — Version conflict

The only place optimistic locking becomes visible. Fires when **Save changes** on Edit returns `409`.

Standard modal, amber tile (`rgba(201,146,46,.12)`, 22px alert-triangle) — **not danger**, because nothing was destroyed and the user did nothing wrong.
- Title **This entry changed since you opened it**
- Body **Someone saved a newer version. Reloading discards your edits.**
- Buttons: **Reload** (`.btn.primary`) · **Keep my version** (secondary)

Two options only. A third ("merge") is a genuinely different feature and belongs nowhere near P3.

**Reachable in practice, with one user:** two tabs on Edit; a re-analysis racing an edit; a double-submit; or the browser Back button restoring a cached form with a stale version. Worth noting that the widest conflict window in the app is not the 2–10s AI call — it's the user sitting on the Edit page with the tab open.

**Keep my version** re-submits with the current server version, which always succeeds. **Reload** re-fetches and discards local edits.

---

### Frame 18 — Something went wrong

Generic error boundary. `padding 40px 18px` centred.
- 54×54 tile, `radius 14`, `--surface-1`, 26px alert-triangle in `--text-secondary`
- **Something went wrong** — `15.5px/500`
- `.btn.primary` **Back to home** with a 16px home icon

No stack trace, no apology, no error code in the primary view.

---

### Frame 19 — Page not found

- `34px/500 --brand` **404**
- **Page not found** — `15.5px/500`, `margin 6px 0 16px`
- `.btn.primary` **Back to home**

Reachable by a deleted entry's URL, so it needs to exist.

---

## 6a. To-dos

A second read model over the same data: every to-do from every entry, in one list. Read-only in P3.

The feature that justifies it is not task management — it's that **every commitment the user made to themselves in prose is collected in one place**, with a route back to the context that produced it. A to-do without its source entry is frequently meaningless ("call him back"), so the link is not a convenience, it's what makes the page work at all.

Read-only is deliberate. Ticking, removing, or reordering would put user state into a list that re-analysis regenerates, which turns a safe replace into a merge. That decision is deferred, not forgotten.

### Frame 20 — To-dos, populated

**App bar.** Logo, nav with **To-dos** active. Right: nothing — this page has no create action, because to-dos originate from analysis.

**Body.** `.main` padding `22px 20px 26px`.
- `.h1` **Your to-dos**, `margin-bottom 6`
- Subtitle `13.5px --text-secondary`, `margin-bottom 16`: **Everything you said you'd do, gathered from your entries.**
- Sort control, right-aligned above the table: `.btn.sm` with a 15px sort icon, label **Newest first**. Toggles to **Oldest first**. Two options only.
- Table in a `.card` with `padding:0; overflow:hidden`

**Table grid.** `3fr 1.6fr 0.9fr auto`

| Column | Align | Content |
|---|---|---|
| To-do | left | `13.5px/1.5 --text-primary`, **wraps** — `todos.value` allows 1 000 characters, so truncation is not acceptable |
| From | left | Entry title at `13px --text-secondary`, hover `--text-primary` + underline |
| Mood | centre | The parent entry's mood pill, or empty |
| Date | right | Parent entry's `last_updated`, `12.5px --text-muted`, tabular |

Header labels: **To-do · From · Mood · Date**

Because to-do text wraps, row height varies. That is correct here and wrong on Entries — this page is a reading list, not a scannable index, so a ragged right edge costs nothing.

**Row padding** `14px 16px`, `border-bottom 0.5px --border`, last row none.

**Interaction.** The **From** cell is the only click target, and it navigates to that entry's View page. The row itself is inert — there is no row-level action, so making the whole row clickable would imply one. Chevron omitted for the same reason.

**Grouping.** None. A flat list sorted by date, so a single entry's to-dos appear adjacent naturally, ordered by `id` within that entry. Grouping by entry would duplicate the From column into a header and add a collapse state for no gain.

**Ordering.** `ORDER BY entry.last_updated DESC, todo.id ASC`. The secondary key is what guarantees a stable order within an entry.

**Pagination.** Same component as Entries, 20 per page.

---

### Frame 21 — To-dos, empty

Reached two ways, and they need different copy.

**No entries at all** — centred, `padding 44px 20px`:
- 60×60 tile, `radius 16`, `--tint-brand-soft`, 28px checklist icon in `--brand`
- **No to-dos yet** — `17px/500`
- **Write an entry and anything you said you'd do will show up here.** — `14px --text-secondary`
- `.btn.primary` **+ New entry**

**Entries exist but none produced a to-do** — same tile and title, different line and no button:
- **None of your entries mention anything you need to do.**

The second is not a failure and must not read as one. It is a true and unremarkable statement about what the user wrote.

---

## 7. Navigation map

```
Entries ⇄ To-dos          (top-level nav, both directions)
   │           │
   │           └── "From" cell ─────────────→ View
   │
Home ──── New entry ─┬─ saving ─┬─ ok ──────→ View (analysed)
 │  ▲                │          └─ AI fail ─→ View (not analysed) + toast
 │  │                └─ req fail → stays, inline error
 │  │
 │  └── Cancel/Back (dirty) → Discard? ─┬─ Keep writing → stays
 │                                       └─ Discard ─────→ Home
 │
 ├──── Filter modal ─┬─ Show N entries → Home (filtered)
 │                    └─ Cancel ─────────→ Home (unchanged)
 │
 └──── row click ──── View ─┬─ Edit ──→ Edit ─┬─ Save ok ──→ View
                             │                 ├─ 409 ──────→ Conflict ─┬─ Reload
                             │                 │                         └─ Keep mine → View
                             │                 └─ Cancel ───→ View
                             ├─ Analyse again → analysing ─┬─ ok → View (analysed)
                             │                              └─ fail → toast, unchanged
                             ├─ Delete → Delete? ─┬─ Cancel → View
                             │                     └─ Delete → Home
                             └─ Back ─────────────────────→ Home
```

---

## 8. State matrix

| Screen | `analysed_at` | Summary | Mood pill | Tags/To-dos | Bar action | Notice |
|---|---|---|---|---|---|---|
| View, analysed | `>= last_updated` | shown | shown if set | shown | Edit · Delete | — |
| View, not analysed | `NULL` | **absent** | absent | placeholder card | **Analyse again** · Edit · Delete | toast |
| View, out of date | `< last_updated` | shown + caveat | shown | shown | **Analyse again** · Edit · Delete | inline caveat |
| View, analysing | any | previous kept | kept | kept | all disabled | top bar |
| Home row | `NULL` | amber pill in cell | empty cell | as available | — | — |

---

## 9. Complete copy inventory

| Location | String |
|---|---|
| Wordmark | Journal |
| Navigation | Entries · To-dos |
| Home heading | Your journal |
| To-dos heading | Your to-dos |
| To-dos subtitle | Everything you said you'd do, gathered from your entries. |
| To-dos table headers | To-do · From · Mood · Date |
| To-dos sort | Newest first · Oldest first |
| To-dos empty, no entries | No to-dos yet / Write an entry and anything you said you'd do will show up here. / + New entry |
| To-dos empty, none found | No to-dos yet / None of your entries mention anything you need to do. |
| Search placeholder | Search entries |
| Filter button | Filter · Filter · 3 |
| New entry button | + New entry |
| Table headers | Title · Summary · Mood · Tags · To-dos · Updated |
| Clear filters (chip row) | Clear all |
| Empty, first run | Start your journal / Write your first entry and it'll show up here. |
| Empty, no matches | No entries match these filters / Try removing a filter or searching for something else. / Clear all filters |
| Filter modal | Filter entries / Mood / Matches entries with any of the moods you pick. / Tags / Matches entries that have all the tags you pick. / Add a tag / No tag matches "xyz" / Clear all / Cancel / Show 4 entries |
| New entry | New entry / Title / Give it a title / Entry / Write whatever's on your mind. / Cancel / Save and analyse / This usually takes a few seconds. |
| Saving | Analysing… |
| Title invalid | Enter a title |
| Content invalid | Write something first |
| Save failed | Couldn't save your entry / Check your connection and try again. Your text is still here. |
| Discard | Discard this entry? / Your writing won't be saved. / Keep writing / Discard |
| View meta | ✓ analysed / not analysed yet / analysis out of date / Created 12 Mar 2026 / Updated 14 Mar 2026 |
| Stale caveat | Based on an earlier version of this entry. |
| Not-analysed card | This entry hasn't been analysed yet / Summary, mood, tags and to-dos will appear here once it has. |
| Degradation toast | Entry saved / The analysis didn't finish. You can analyse it again anytime. |
| Re-analyse fail toast | Analysis didn't finish / Nothing was changed. You can try again. |
| Empty tags / todos | No tags. / No to-dos. / The analysis didn't find any tags or to-dos. |
| View actions | ← Back / Edit / Delete / Analyse again |
| Edit | Edit entry / Analysis / Generated by AI. You can change any of it. / Summary / Mood / none / Tags / To-dos / Add a to-do / Cancel / Save changes |
| Delete | Delete this entry? / This can't be undone. / Cancel / Delete |
| Conflict | This entry changed since you opened it / Someone saved a newer version. Reloading discards your edits. / Reload / Keep my version |
| Error | Something went wrong / Back to home |
| 404 | 404 / Page not found / Back to home |
| Counter | 18,204 / 20,000 |
| Pagination | 1–20 of 47 |

Sentence case throughout. No exclamation marks. No apologies. Verbs survive the flow — **Delete** produces **Entry deleted**.

---

## 10. Accessibility

- Table rows are `<a>` or `role="link"`, focusable, Enter activates. Nested tag pills are separate focusable controls — so the row needs `tabindex` management, not a wrapping anchor around interactive children.
- Every icon-only control gets an `aria-label`: the chip `×` reads `Remove filter: happy`.
- Mood pills never rely on emoji alone — the text label carries the meaning for screen readers and for the 12.5px size where emoji are ambiguous.
- The indeterminate bar is `role="progressbar"` with `aria-busy` on the region; the status change is announced via `aria-live="polite"`.
- Toasts are `role="status"`, `aria-live="polite"`. Never `alert` — they are not interruptions.
- Modals trap focus, restore it on close, and close on Escape.
- Focus ring: `0 0 0 2px --surface-2, 0 0 0 4px --brand`.
- Content that must never be reflowed: the entry body, at `pre-wrap`.

---

## 11. Deliberately absent

Recording these so they read as decisions rather than gaps:

- **Sortable columns** — default is `last_updated` desc; sorting a journal is a feature nobody uses.
- **Bulk select / bulk delete** — no requirement, and it multiplies the destructive surface.
- **Undo after delete** — the copy promises permanence; a soft-delete column would contradict the schema.
- **To-do completion** — no `done` column, so no checkboxes anywhere. Re-analysis regenerates the to-do list, so a completion flag would have to survive a regenerate, which turns a safe replace into an unreliable text-match merge. Revisit once the read-only aggregate page has been used enough to know whether ticking is actually wanted.
- **Manual to-do reordering** — order is `id` ascending, which is the order the writer mentioned things. A `position` column plus multi-row updates buys nothing for three items, and a collection change bumps the entry's version, so dragging would be able to produce a `409`.
- **Editing or deleting to-dos from the aggregate page** — one to-do belongs to one entry, so mutating it there would bump that entry's version on a page that shows no version and has no conflict dialog. Edits happen on the entry.
- **Grouping the aggregate page by entry** — sorting by date already clusters an entry's to-dos together.
- **Tag management screen** — tags are AI-generated; there is no create/rename/delete requirement.
- **Search result snippets** — Home hides content on purpose; snippets would contradict that and make row heights ragged.
- **Streaming the summary** — belongs with the frontend stage, and needs the response to stop being one finished object.
- **Login, accounts, sharing, export** — P4 and beyond.
