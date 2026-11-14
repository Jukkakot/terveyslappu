# Proposal

## Why

The front page needs scrolling and repeats parts that belong in the details (BMI, normal range,
the automatic weigh-in card, a separate weigh-in button). The user wants an overview that fits one
screen: weight on top, food (and later steps) under it, each card opening its details, with the
card's own quick action inside it (user 2026-11-14).

## What Changes

- Weight card on top: the latest weight, its change, the small chart with its period menu, and a
  filled "Punnitse" button inside the card. The separate bottom "Punnitse vaa'alla" goes.
- The BMI and normal-weight row leaves the front page; it stays in Historia (BMI tab, point
  details). The "BMI vaatii pituuden" link leaves with it.
- The automatic weigh-in card leaves the front page (the widget and Asetukset show its state).
- Food card full width under the weight card; the row is built so that the steps card can later
  take its right half. A tap on the card's number or title opens Ruoka on today; a tap on its bars
  opens Historia on Energia. The outlined "+ Annos" stays inside; the "Tänään" button goes.
- The Ruoka day screen gets a link to the week view (Historia on Energia); Energia's bars and list
  already open a day.
- Small charts get lower, so the front page with its notices fits a phone screen without scrolling.
- The clipboard food offer is shown also on the front page.
- Source-neutral wording for the food JSON import: "Liitä tekoälyltä" → "Tuo ruoka-annokset",
  the preview title "Tuo tekoälyltä" → "Tuo ruoka-annokset", "Kopioi ohje tekoälylle" →
  "Kopioi tuontiohje", the offer line "Leikepöydällä ruoka-annoksia: <N> · Näytä · Ohita". No
  JSON export (user 2026-11-14: no need).

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `home-screen`: the front page's cards, actions, taps and what is left out.
- `food-log`: the Ruoka screen's week link, the clipboard offer on the front page, the import's
  wording.

## Impact

App screens only: `ui/HomeScreen.kt`, `chart/HomeSummary`/`HomeFood` (BMI data no longer needed
there), `ui/FoodScreen.kt`, the clipboard offer's call sites in `MainActivity`/`FoodViewModel`,
`strings.xml`, `HelpScreen` text, home screen tests. No Health Connect, scale, widget or CI change.
