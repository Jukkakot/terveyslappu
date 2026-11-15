# Proposal

## Why

Rows, buttons, delete and Kumoa differ between screens (survey 2026-11-15): delete is a grey
trash on weights, a red trash in the sheet, a grey "Poista" in editors and a confirm dialog for own
charts; some deletes have no Kumoa at all; primary buttons are filled on one screen and outlined on
the next; rows are 48, 52 or 56 dp. The user chose all recommendations (2026-11-15).

## What Changes

- One delete rule: every delete happens at once, without a confirmation, and offers Kumoa (the 4 s
  bar, or the sheet line inside the sheet). The own chart's confirm dialog goes away; chart delete,
  product delete under Asetukset → Ruokakirjaus, a removed row in the AI preview and ✕ on the
  clipboard card gain Kumoa.
- Every delete control is red (error colour): row trash buttons and editor "Poista" buttons.
- Weight rows no longer delete on long-press; the trash button stays.
- Button hierarchy: a screen's main action is a filled button, a secondary one outlined, the rest
  text buttons. Dialogs keep Material text buttons: Poista (red) on the left, Peruuta then
  Tallenna on the right. Ruoka's "Lisää annos" and the Uusi kaavio tab's button become filled.
- List rows: at least 56 dp high with 16 dp from the screen edge to the content, on every list
  (Historia weights, Energia, Askeleet, Ruoka, Ruokakirjaus, AI preview; sheet rows line up to the
  same edge).
- Settings' "copied" messages use the same timed, swipeable bar as the others.

## Decisions taken without asking

- Row tap opens the editor wherever a row can be edited; a pencil is shown only where tap does
  something else (the sheet's suggestion rows), as today.
- Kumoa of a removed AI-preview row returns it to the same place in the draft; Kumoa of ✕ on the
  clipboard card brings the offer back (it counts as not skipped).
- Home edit mode's "Lisää kortti" stays outlined: Valmis is that mode's main action.

## Capabilities

### New Capabilities
(none)

### Modified Capabilities
- `app-appearance`: new requirements Delete and Kumoa, Buttons, List rows.
- `weight-entry`: Delete own entry (no long-press).
- `weight-chart`: Chart editor (delete without confirm, with Kumoa).
- `food-log`: Ruokakirjaus in Asetukset (Kumoa), Preview and fine-tuning (Kumoa for a removed row),
  new requirement Undoing Ohita.

## Impact

App screens only: Historia list, Energia/Askeleet lists, Ruoka, Lisää annos sheet and item editor,
Ruokakirjaus, AI preview, clipboard card, chart editor, Asetukset messages. No data, Health Connect,
widget or build change. Implement after `fixed-tabs-single-data` is merged (same files).
