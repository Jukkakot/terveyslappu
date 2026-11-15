# Proposal

## Why

The Lisää annos sheet still leaves the user wondering what to do (user, 2026-11-15, v1.33
screenshot): the import offer is under the field with the keyboard up, the field's label "Ruoka
tai kcal" is odd, the list has no heading, and the two bottom links look equal. Importing copied
meals is the usual case (about 90 %), so it must be the first thing and take one tap. Products
cannot be removed from the sheet, and the Kumoa message stays 10 s and covers buttons. Chosen
from mockups A–E (canvas "Lisää annos -luonnokset"): A with B's "Lisää kaikki" card.

## What Changes

- Sheet header "Lisää annos · <day>" with a ⋮ menu (Kopioi tuontiohje moves there).
- Clipboard card at the top when food JSON is on the clipboard: the items with amount and kcal,
  the total, "Lisää kaikki" (adds at once, no preview), "Muokkaa" (opens the preview) and ✕
  (Ohita). The keyboard does not rise while the card is shown. Same card on the Ruoka screen.
- Field: search icon and "Hae ruokaa tai kirjoita kcal".
- List headings "Usein tähän aikaan" (empty field) and "Tuotteet" (typing); while typing a last
  row "Lisää ”<text>” uutena" shows what Enter does.
- A red delete button on own-product rows removes the product (logged items stay), with Kumoa.
- Kumoa messages everywhere in the app: about 4 s, swipeable, never covering the sheet's buttons.
- "Tuo ruoka-annokset" stays as a quiet text button at the bottom.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `food-log`: "Adding an item", "Clipboard offer", "Import from the home-screen sheet"; added
  "Removing a product from the sheet".
- `app-appearance`: added "Undo messages".

## Impact

- App screens: the Lisää annos sheet (in the app and from the widget and shortcut), the Ruoka
  screen's offer, snackbars app-wide. Health Connect: "Lisää kaikki" writes like the preview's
  Lisää. No scale, widget-visual or build/CI change. Ohje text updated.
