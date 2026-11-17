## MODIFIED Requirements

### Requirement: Ruoka day screen
The app SHALL have a Ruoka screen for one day, opening on today, with arrows to earlier days (none
after today). It SHALL list the day's items in eating order, each on one line with its name
(shortened with "…" when it does not fit) and kcal, without the amount or a logging time (both are
in the row's ⓘ bubble, see "Item row buttons"). On top it SHALL
show the day's total kcal with the treats' share apart, and the number of items without kcal
("1 760 kcal · herkkuja 230 kcal · 1 ilman kcal"). A day with nothing logged SHALL show only a short
line on how to add, without any warning, count or judgement. The screen SHALL show no budget,
target, remaining calories, streak or meal names. The top bar SHALL have a "Viikko" link that opens
Historia on Energia for 1 vk; back from there SHALL return to the Ruoka screen.

#### Scenario: A day in order
- **GIVEN** today the user logged Rahka 200 g at 7.10, Pullo 1 kpl at 10.05 and Pizza (no kcal) at 12.30
- **WHEN** the Ruoka screen opens
- **THEN** the three items are listed in that order, each on one line without amount or time, and
  the top reads the total of Rahka and Pullo and "1 ilman kcal"

#### Scenario: Missed day
- **WHEN** the user moves to a day with nothing logged
- **THEN** only the how-to-add line is shown

#### Scenario: From the day to the week
- **WHEN** the user taps Viikko on the Ruoka screen
- **THEN** Historia opens on Energia with 1 vk, and tapping a day in the Energia day list opens that
  day on Ruoka

### Requirement: Item row buttons
Every logged item row in the add sheet's day list and on the Ruoka screen SHALL be one line: the
name with its herkku sign, the kcal ("ilman kcal" when missing), an ⓘ button and one ⋮ button, in
that order. The row keeps the 56 dp minimum height of the app's list rows.

The ⓘ button (named "Tiedot: <name>") SHALL open an info bubble (Material 3 rich tooltip) anchored
to the button, with the name as its title and these lines, each only when known:
- the amount with its unit ("200 g", "1 kpl");
- the kcal ("180 kcal"), or "ilman kcal";
- the ruoka's Kalorit ("90 kcal / 100 g", "250 kcal / kpl", whole kcal), taken from the own ruoka
  whose name matches the item's (the app's name key; own ruuat only, never a Valmis ruoka); left
  out when there is none (e.g. removed) or it has no Kalorit;
- "Herkku" when the item is a treat (the item's own flag, the same as the row's sign);
- the logging time "kirjattu klo 7.10" (in the item's own time offset) only when the item has a
  real logging time; annokset added afterwards to an earlier day and imports without a time show
  no time line.
The kcal line is always there, so the bubble is never empty. The shown kcal is the item's stored
kcal, never recomputed from amount × Kalorit. The title shows the full name, wrapped, never
shortened. The bubble has no caret, sits above ⓘ (below when there is no room) inside the
window, and may cover other parts of the screen.

Tapping outside the bubble, back, tapping ⓘ again, or scrolling or dragging the list SHALL close
it, and only close it: that tap or back opens no editor, presses nothing and does not close the
sheet. The bubble also closes when its item disappears; if the item changes while open, its lines
follow. The bubble has no buttons and changes nothing; opening or closing it SHALL leave the
sheet's field focus, keyboard and highlight as they were. Long-press on ⓘ does nothing extra.
TalkBack SHALL announce the bubble's title and lines once when it opens. ⓘ appears only on annos
rows (Ruoka and the sheet's day list), not on Omat ruuat or paino rows.

The ⋮ button (named "Toiminnot: <name>") opens a menu with "Muokkaa" (opens the item's editor) and
"Poista" (in the theme's error colour) that deletes the item at once and shows "<name> poistettu ·
Kumoa" (in the sheet in its line, on Ruoka as its Kumoa bar); Kumoa SHALL bring the item back in its
place. Poista SHALL act exactly as the editor's Poista (in the sheet it clears the highlight of that
item, replaces the line and keeps the keyboard as it was). On a narrow row the name SHALL be
shortened first; kcal, ⓘ and ⋮ never shrink. Tapping elsewhere on the row SHALL still open
the editor. Järjestä rows are one line too, with only their own move buttons (no ⓘ or ⋮).

#### Scenario: Amount from the info button
- **GIVEN** today has Rahka 200 g, 180 kcal, logged at 7.10, and the own ruoka Rahka has 90 kcal / 100 g
- **WHEN** the user taps ⓘ on Rahka
- **THEN** a bubble titled Rahka shows "200 g", "180 kcal", "90 kcal / 100 g" and "kirjattu klo 7.10",
  and tapping outside it closes it with nothing changed

#### Scenario: Item without amount or kcal
- **GIVEN** Pizza was logged today at 12.30 with no amount and no kcal, and the ruoka Pizza has no Kalorit
- **WHEN** the user taps ⓘ on Pizza
- **THEN** the bubble shows only "ilman kcal" and "kirjattu klo 12.30"

#### Scenario: Added afterwards to yesterday
- **GIVEN** the user added Lasagne to yesterday this morning
- **WHEN** they tap ⓘ on Lasagne
- **THEN** the bubble has no "kirjattu" line

#### Scenario: Bubble open in the sheet
- **GIVEN** the sheet's field has focus and the keyboard is up
- **WHEN** the user taps ⓘ on Tomaatti and then back
- **THEN** the bubble closes, the sheet stays open and the keyboard and field are as before

#### Scenario: Delete from the sheet
- **WHEN** the user taps ⋮ on Tomaatti in the sheet's day list and then Poista
- **THEN** Tomaatti is gone from the day, the header total drops, the line reads "Tomaatti poistettu
  · Kumoa", and Kumoa brings it back in its place

#### Scenario: Edit from Ruoka
- **WHEN** the user taps ⋮ on Kurkku on the Ruoka screen and then Muokkaa
- **THEN** Kurkku's editor opens

#### Scenario: Menu closed without a choice
- **WHEN** the user opens the ⋮ menu on Kurkku and taps outside it
- **THEN** the menu closes and nothing changes
