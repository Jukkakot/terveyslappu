## MODIFIED Requirements

### Requirement: Ruoka day screen
The app SHALL have a Ruoka screen for one day, opening on today, with arrows to earlier days (none
after today). It SHALL list the day's items in eating order, each with its name, amount and kcal,
and, for an item logged on the day itself, its logging time in small muted text. On top it SHALL
show the day's total kcal with the treats' share apart, and the number of items without kcal
("1 760 kcal · herkkuja 230 kcal · 1 ilman kcal"). A day with nothing logged SHALL show only a short
line on how to add, without any warning, count or judgement. The screen SHALL show no budget,
target, remaining calories, streak or meal names. The top bar SHALL have a "Viikko" link that opens
Historia on Energia for 1 vk; back from there SHALL return to the Ruoka screen.

#### Scenario: A day in order
- **GIVEN** today the user logged Rahka 200 g at 7.10, Pullo 1 kpl at 10.05 and Pizza (no kcal) at 12.30
- **WHEN** the Ruoka screen opens
- **THEN** the three items are listed in that order with their times, and the top reads the total
  of Rahka and Pullo and "1 ilman kcal"

#### Scenario: Missed day
- **WHEN** the user moves to a day with nothing logged
- **THEN** only the how-to-add line is shown

#### Scenario: From the day to the week
- **WHEN** the user taps Viikko on the Ruoka screen
- **THEN** Historia opens on Energia with 1 vk, and tapping a day in the Energia day list opens that
  day on Ruoka

### Requirement: Adding an item
"Lisää annos" (on the front page and on the Ruoka screen) SHALL open the add sheet, adding to the
day shown (today from the front page). The sheet SHALL take the full height and show the day it
adds to, with adding kept apart at the bottom.

From the top the sheet SHALL show: a header "<day> · <total> kcal" (Tänään, Eilen or the date;
"<day>" alone while the day is unread or empty) with a ⋮ menu holding Kopioi tuontiohje and Tuo
ruoka-annokset; under it the Ruoka screen's summary line without the total ("herkkuja 230 kcal · 1 ilman
kcal", parts with zero left out, the line left out when both are zero); the clipboard card when it
is offered ("Clipboard offer"); then the day's items in eating order as rows of the Ruoka screen
(logging time, name, amount, treat sign, kcal), scrolling, with "Ei vielä annoksia" when the day
has none. Tapping an item SHALL open its editor ("Editing, order and delete"). The item added last
in this sheet SHALL be highlighted with a tonal background, and the list SHALL scroll to show it.

At the bottom the sheet SHALL show the add panel on a surface of its own tone (surfaceContainerHigh,
with a 1 dp outlineVariant line along its top edge), so it reads apart from the day list: a message
line only when "Sheet messages and loading" has one, then with
the field empty the heading "Lisää – usein tähän aikaan" and up to eight chips, one per product:
the products most often logged within two hours of the current clock time in the last 30 days,
then the most often logged products, in at most three wrapped rows (chips that do not fit are left
out). A chip SHALL read "+ <name> <kcal> kcal" with the kcal of its usual amount, in a slightly smaller type than the other labels (labelMedium), or "+ <name>" when the
energy is unknown; it SHALL show no treat sign. Last, the field with a search icon and the hint
"Hae ruokaa tai kirjoita kcal".

Tapping a chip SHALL add that product at once at the end of the day with its usual amount, show it
in the day list, highlighted and scrolled into view; no confirmation line is shown (the row's edit
and delete buttons replace Muuta and Kumoa). A long press on a chip SHALL open the editor for a new item
of that product (below); the chip's accessibility actions SHALL offer the same.

While typing, the list "Tuotteet" SHALL cover the day list and the chips, directly above the field:
up to five matching products, own products before base products, each row with the product's name
and under it its usual amount and the kcal that amount gives ("200 g · 130 kcal"; with no usual
amount the energy, "140 kcal / 100 g"; "200 g · ei kcal" or "ei kcal" when the energy is unknown),
the treat sign for a Herkku product, and an edit button; and as the last row "Lisää ”<text>”
uutena" with a line telling what it adds (for a bare number "Pikalisäys <N> kcal"; for other text
"ilman kcal" or the known product's kcal); tapping it SHALL do what Enter does. Tapping a product
row SHALL add it as a chip does. Clearing the field SHALL bring the day list and the chips back.

The editor for a new item (a match row's edit button or a chip's long press) SHALL be the same
editor and rules as Muuta (name, amount and unit, energy, Herkku; changes reach the product as
after Tallenna), filled as a tap would add it, with Lisää and Peruuta instead of Tallenna and
Poista: Lisää SHALL add the item as edited, show it highlighted in the day list and return to the
sheet; Peruuta SHALL return to the sheet and add nothing (as does closing the editor). Leaving the
item editor by Tallenna, Peruuta or Poista SHALL return to the sheet with the field as it was, and
the line SHALL then show the edited item (none after Poista). Pressing enter SHALL add exactly the
typed text: a bare number as an item "Pikalisäys" with that many kcal, any other text as an item of
that name (its product's kcal when the name is a known product, otherwise without kcal), then
clear the field and keep the keyboard up for the next item.

Details:
- Header: "<day>" is `dayTitle` capitalised (Tänään, Eilen, date) and names the sheet's day
  relative to the current date (after midnight a sheet opened yesterday reads "Eilen · …"). It
  shows "· <total> kcal" whenever the day has at least one item, also "· 0 kcal".
- Layout: the header and summary line stay pinned; the add panel never scrolls and sits directly
  above the keyboard when it is up; only the day list shrinks. When the day list would be shorter
  than two rows, the chips drop to fewer rows (down to one). A drag down on the handle, or on the
  day list at its top, closes the sheet; the panel is not a drag area for closing.
- Clipboard card: the first entry of the scrolling day list (it scrolls away), hidden while typing.
- While typing, the Tuotteet rows replace the day list (header and summary stay), bottom-aligned
  just above the line and the field, best match nearest the field and "Lisää … uutena" closest to
  it; they scroll when they do not fit.
- Highlight: only the newest add in this sheet (for Lisää kaikki the last of its items); it stays
  until the next add or the sheet closes; Kumoa or Poista of it clears it; editing other items does
  not move it; the list scrolls it into view also with the keyboard up.
- The line: editing an item from the day list that was not the line's item leaves the line as it
  was; Poista of any item shows "<name> poistettu · Kumoa", and Kumoa restores it in its place.
- Chips: filled in rank order, stopping at the first chip that does not fit (no backfill); a long
  name is ellipsized at about 60 % of the panel width; the kcal carries its unit ("145 kcal"). With no chips the
  heading and chip area are left out. The accessibility action of a chip is "Muokkaa ennen
  lisäämistä".
- Keyboard: a chip tap keeps the keyboard as it was; scrolling the day list hides it; after an
  editor the keyboard comes back only if it was up before; back first closes the keyboard.
- From the Ruoka screen the sheet is the same; Ruoka's own clipboard card is not shown while the
  sheet is open, and the sheet cannot be opened from Järjestä.

#### Scenario: The day first
- **GIVEN** today the user logged Rahka 250 g at 7.10 and Jäätelö 130 g (Herkku, 260 kcal) at 19.05
- **WHEN** they open the sheet from the widget
- **THEN** the header reads "Tänään · 405 kcal", the line under it "herkkuja 260 kcal", both items are
  listed with their times, the keyboard is down, and the chips sit above the field at the bottom

#### Scenario: Usual breakfast in one tap
- **GIVEN** on most mornings around 7 the user logged Rahka 200 g
- **WHEN** the user taps Lisää annos at 7.20
- **THEN** among the first chips is "+ Rahka 130 kcal", and one tap logs Rahka 200 g with its kcal,
  shows it highlighted at the end of the day list and raises the header total by 130

#### Scenario: Set the amount before adding
- **GIVEN** a product Jauhelihakastike 140 kcal / 100 g, usually 250 g
- **WHEN** the user long-presses its chip (or types "jauh" and taps the edit button on its row),
  changes the amount to 350 g and taps Lisää
- **THEN** Jauhelihakastike 350 g, 490 kcal is added and highlighted in the day list, and
  the next Jauhelihakastike starts at 350 g

#### Scenario: Edit and cancel
- **WHEN** the user opens the new-item editor and then taps Peruuta
- **THEN** nothing is added and the sheet is as before

#### Scenario: Edit a typed match
- **WHEN** the user types "kaurapuu" and taps the edit button on Kaurapuuro
- **THEN** the editor opens with Kaurapuuro's typical portion, and Lisää adds it as edited

#### Scenario: What enter does
- **WHEN** the user types "kana"
- **THEN** the Tuotteet rows replace the day list and the chips above the field, the last row reads
  "Lisää ”kana” uutena", and tapping it adds what Enter would

#### Scenario: Only a number
- **WHEN** the user types 350 and presses enter
- **THEN** an item "Pikalisäys" with 350 kcal is added, the field is cleared and the day list shows it

#### Scenario: A whole day as one number
- **GIVEN** the user estimated yesterday at 2 200 kcal with an AI chat
- **WHEN** they move to yesterday on Ruoka, tap Lisää annos, type 2200 and press enter
- **THEN** yesterday has an item "Pikalisäys" 2 200 kcal next to any other items, counted in
  Energia like any item, and the sheet's header reads "Eilen · …"

#### Scenario: Only a name
- **WHEN** the user types "työpaikan pizza", which is no product yet, and presses enter
- **THEN** an item "työpaikan pizza" without kcal is added and the line under the header counts
  "1 ilman kcal"

#### Scenario: Forgiving search
- **WHEN** the user types "jaulihak"
- **THEN** Jauhelihakastike is suggested (letters ä/a and ö/o equal, parts of compound words and
  one wrong letter from four typed letters match)

#### Scenario: Edit an eaten item in the sheet
- **WHEN** the user taps Banaani in the sheet's day list, changes it to 2 kpl and taps Tallenna
- **THEN** the sheet shows Banaani 2 kpl with the new kcal and the new total

#### Scenario: Back to the field after editing
- **WHEN** the user taps Muuta, changes the amount and taps Tallenna
- **THEN** the sheet is shown again and the line shows the edited item

#### Scenario: Back to the field after cancelling or deleting
- **WHEN** the user taps Muuta and then Peruuta, or Poista
- **THEN** the sheet is shown again; after Poista no item line is shown

### Requirement: Treats apart
A product in category Herkku SHALL carry a small herkku sign on its items, and the day total, the
chart and the statistics SHALL show the treats' share apart from base food, so the user sees
whether the excess comes from ordinary meals or from treats. Herkku SHALL NOT be red or styled as
a warning. There SHALL be no other categories.

#### Scenario: Day with a treat
- **GIVEN** today 1 530 kcal of Ruoka items and Suklaapala 230 kcal (Herkku)
- **THEN** the day reads 1 760 kcal · herkkuja 230 kcal, and its bar has a 230 kcal treat part

### Requirement: Adding from the home screen
A tap on the widget or the shortcut SHALL open the add field (as in "Adding an item") as a sheet
over whatever is on screen, without the app's main screen behind it, adding to today, in the app's
theme. The sheet SHALL work as on Ruoka: suggestions, search, enter, the highlighted new row, and it SHALL stay open for the next item. Closing the sheet (back, a tap outside or swiping
it down) SHALL show what was behind it, normally the home screen.

#### Scenario: Breakfast from the home screen
- **GIVEN** on most mornings around 7 the user logged Rahka 200 g
- **WHEN** they tap the Lisää annos widget at 7.20 and tap Rahka
- **THEN** Rahka 200 g is added to today, the sheet stays open with the Rahka row highlighted, and closing it shows the home screen again

#### Scenario: Several items in a row
- **WHEN** the user types "kahvi", presses enter, then taps Pulla
- **THEN** both are added to today in that order, and the app's Ruoka screen shows them when opened

#### Scenario: Closing at once
- **WHEN** the user taps a suggestion and closes the sheet immediately
- **THEN** the item is still in Health Connect

#### Scenario: Opened again while open
- **WHEN** the user taps the widget again while the sheet is open
- **THEN** the sheet stays as it was

### Requirement: Sheet messages and loading
A failed write SHALL show "Tallennus epäonnistui" as a line at the top of the add panel and keep the sheet open. A delete SHALL show "<name> poistettu · Kumoa" in the same place;
closing the sheet drops it. With no message the line is left out. The field SHALL be usable at once, but the keyboard SHALL stay down
until the user taps the field. Until the day has been read the day list's place SHALL show one
quiet "Ladataan…" (onSurfaceVariant, bodyMedium), with no empty text, the chip area left out and
the header "<day>" alone; a failed read SHALL show its line there instead, with no chips, and the
day SHALL be read again when the sheet resumes and before the next write.

#### Scenario: Add before the day is read
- **WHEN** the user types a name and presses enter before the day has loaded
- **THEN** the item is added at the end of the day once it has loaded

#### Scenario: Failed write
- **WHEN** Health Connect refuses a write
- **THEN** the line "Tallennus epäonnistui" is shown and the sheet stays open

#### Scenario: Day cannot be read
- **WHEN** reading the day fails
- **THEN** a short line in the day list's place says so, typing still works, and an added item is
  placed at the end after the day has been read again
