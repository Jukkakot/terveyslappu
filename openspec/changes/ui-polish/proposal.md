# Proposal

## Why

The usability review of 2026-11-19 (roadmap backlog, "Polish", U7 and U9) and a pass over the
screenshot tour found small faults that make the app look careless or hide things: units broken
mid-word on the front page, two roundings of the same change on Paino, clipped text at large text
sizes, a half-hidden "Uusi kaavio" tab, wrapping theme chips, an unlabelled Järjestä icon, the food
card's hidden split taps, and typing the same amount again in the editor. None needs a new
feature; each is a small, uncontroversial fix. `everyday-feedback` (v1.64) is merged, so its
weigh-in dialog and notification change texts join the shown-change rule.

## What Changes

- **Units stay whole (front page):** a number and its unit never break apart and "askelta/pv" or
  "kcal/pv" never split; on a small card the average goes on its own line under the period label
  when it does not fit beside it.
- **One rounding for weight changes:** Paino's "Viikossa" shows one decimal with its unit
  ("−0,7 kg/vk"), like Muutos. Every weight change (front page card, Muutos, Viikossa, Historia rows
  and group rows, the weigh-in dialog's change line and the automatic-save notification) is the
  difference of the weights as shown, with one shared rounding function, so "82,3 → 81,8 kg" never
  sits next to "−0,6 kg" (found in the screenshot tour).
- **Front page up to 200 % text:** rows grow with the text without a cap; above 130 % a small card
  takes the full width (also in edit mode) so its title, number and unit fit. The screenshot tour adds 200 % shots.
- **"Uusi kaavio" as a "+" tab:** the last Historia tab shows a "+" icon (TalkBack "Uusi kaavio"),
  so it no longer clips and reads as "add". Its page is unchanged.
- **Theme and colours as a radio list:** Ulkoasu's Teema and Värit become lists of radio rows
  instead of wrapping chips (a segmented button was tried before and broke "Järjestelmä"
  mid-word; see design).
- **Järjestä named:** Ruoka's unlabelled sort icon becomes a ⋮ menu with "Järjestä annokset".
- **Food card is one tap target:** the whole food card opens Ruoka on today, like the steps card
  opens Askeleet (a first tap without the permission still asks for it); Syöty's week stays one
  tap away (Ruoka → Viikko or Historia). Ohje's food text follows.
- **Recent amounts in the editor:** the annos editor shows up to three earlier amounts of the same
  ruoka as chips ("200 g", "150 g"); a tap fills the amount.

Touches app screens and two texts (the weigh-in dialog's and the notification's change line), the
front page cards, Historia Paino statistics, lists and tab row, Ruoka top bar and item editor,
Asetukset → Ulkoasu, Ohje's food text and the screenshot tour. No Health Connect, scale, widget,
data file or build/CI change.

## Capabilities

### New Capabilities

### Modified Capabilities
- `home-screen`: "Simple home screen" and "Food row on the home screen" (one tap target, Ruoka),
  "Card sizes" (up to 200 % text); new "Units on the cards", "Cards at large text".
- `weight-chart`: "Period statistics" (Viikossa one decimal, kg/vk), "Custom chart tabs" ("+" tab);
  new "Changes from shown weights".
- `app-appearance`: "Theme choice" (radio rows).
- `food-log`: "Editing, order and delete" (Järjestä annokset in a ⋮ menu); new "Recent amounts in
  the editor".
- `scale-weigh-in`: "Change from the previous weight" (as shown).
- `background-weigh-in`: "Weigh-in notification" (change as shown).

## Impact

`ui/HomeScreen.kt` (food card taps, average layout, `rowUnit` without the cap), `ui/HomeEdit.kt`
(small cards full width above 130 %), `res/values/strings.xml` (non-breaking unit strings, kg/vk,
menu item), `ui/Format.kt` (`formatRate` one decimal), a pure shown-change helper used by
`chart/HomeSummary`, `chart/PeriodStats`, `chart/ListGroups` and the Historia row change,
`ui/HistoryScreen.kt` (tab row, Viikossa), `ui/SettingsScreen.kt` (`ThemeChoice`,
`ColourChoiceChips`), `ui/FoodScreen.kt` (top bar), `ui/FoodSheet.kt` (`FoodEditor` chips) and a
pure recent-amounts rule in `food/`, `scale/WeighIn.kt` and `ui/WeighInViewModel.kt` (the dialog's
change), `auto/AutoText.kt` (notification change), `help_food_body`, the screenshot tour and its
tests.
