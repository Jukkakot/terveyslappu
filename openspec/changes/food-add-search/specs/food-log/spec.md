## ADDED Requirements

### Requirement: Adding from the search
"Lisää annos" (in the ⋮ menu of the front page and Historia, and on the Ruoka screen) SHALL open
the add search over the whole screen, adding to the day shown (today from the front page and
Historia). The add search SHALL show only what can be added: no day list, no day total, no
summary line and no eaten annos.

From the top the add search SHALL show:
- a top bar with a back arrow ("Takaisin"), the field with a search icon and the hint "Hae ruokaa
  tai kirjoita kcal", a ✕ button ("Tyhjennä") while the field has text, and a ⋮ menu holding
  Kopioi tuontiohje and Tuo ruoka-annokset. The field has focus and the keyboard is up when the
  search opens;
- the line "Lisätään: <day>" (Eilen or the date, bodyMedium, onSurfaceVariant) only when the
  search's day is not the current date;
- the clipboard card when it is offered ("Clipboard offer"), hidden while typing;
- with the field empty: the heading "Usein tähän aikaan" and up to eight ruoka rows: the ruuat
  most often logged within two hours of the current clock time in the last 30 days, then the
  most often logged ruuat. With none, the heading is left out and a quiet line "Kirjoita ruuan
  nimi tai pelkkä kcal-määrä" shows instead;
- while typing: up to ten matching ruuat (own before Valmiit ruuat, best match first, directly
  under the top bar) and as the last row "Lisää ”<text>” uutena" with a line telling what it adds
  (for a bare number "Lisää pelkkä <N> kcal"; for other text "ilman kcal" or the known ruoka's
  kcal). Clearing the field (✕ or deleting the text) SHALL bring the empty-field content back.
The list scrolls when it does not fit; scrolling it hides the keyboard.

Every ruoka row (empty field and typing alike) SHALL start with a + icon in the primary colour and
show the ruoka's name (one line, "…" when it does not fit) with the herkku sign for a Herkku ruoka,
under it the usual amount and the kcal that amount gives ("200 g · 130 kcal"; with no usual amount
the Kalorit "140 kcal / 100 g"; "200 g · ei kcal" or "ei kcal" when unknown), and end with a ⋮
button (see "Removing a product from the search"). Rows have no dividers. The "Lisää … uutena" row
starts with the same + icon and has no ⋮.

Tapping a ruoka row SHALL add that ruoka at once at the end of the day with its usual amount.
Tapping the "Lisää … uutena" row SHALL do what Enter does. Pressing Enter SHALL add exactly the
typed text: a bare number as an annos "Pikalisäys" with that many kcal, any other text as an annos
of that name (its ruoka's kcal when the name is a known ruoka, otherwise without kcal). After any
add the field SHALL be cleared, the keyboard SHALL stay as it was, the search SHALL stay open for
the next annos, and a bar "<name> lisätty · Kumoa" SHALL show at the bottom (above the keyboard
when it is up) for as long as the app's Kumoa bars last; Kumoa SHALL remove that annos. A new add
replaces the bar.

The ⋮ menu's "Muokkaa" SHALL open the editor for a new annos: the same editor and rules as an
annos's Muokkaa (name, amount and unit, Kalorit, Herkku; changes reach the ruoka as after
Tallenna), filled as a tap would add it, with Lisää and Peruuta instead of Tallenna and Poista.
Lisää SHALL add the annos as edited (with the same bar) and return to the search; Peruuta, or
closing the editor, SHALL return to the search and add nothing. The search returns with the field
as it was and the keyboard up only if it was up before.

Leaving the search (the back arrow, or system back when the keyboard is down; back with the
keyboard up first hides it) SHALL:
- when at least one annos added in this search is still logged (not undone): show the Ruoka screen
  on the search's day, wherever the search was opened, with every annos added in this search
  highlighted with a tonal background and the list scrolled so the last of them is visible. The
  highlight stays until the user leaves the Ruoka screen or opens Lisää annos again; deleting a
  highlighted annos removes its highlight;
- otherwise: return to the screen it was opened from, as it was.
Back from that Ruoka screen follows Ruoka's own back rule. The search cannot be opened from
Järjestä, and Ruoka's own clipboard card is not shown while the search is open.

#### Scenario: Only addable things
- **GIVEN** today has Rahka and Lumipallo logged
- **WHEN** the user taps Lisää annos on Ruoka
- **THEN** the search opens over the whole screen with the keyboard up, "Usein tähän aikaan" rows
  each starting with +, and neither Rahka nor Lumipallo as an eaten annos or the day's total

#### Scenario: Usual breakfast in one tap
- **GIVEN** on most mornings around 7 the user logged Rahka 200 g
- **WHEN** the user opens Lisää annos at 7.20
- **THEN** among the first rows is "Rahka" with "200 g · 130 kcal", and one tap logs Rahka 200 g,
  clears nothing else and shows "Rahka lisätty · Kumoa"

#### Scenario: A meal of several annokset
- **WHEN** the user taps Rahka, types "kahvi" and presses Enter, then taps back
- **THEN** both are logged in that order, the Ruoka screen shows the day with Rahka and kahvi
  highlighted and kahvi visible

#### Scenario: Undo in the search
- **WHEN** the user taps Rahka and then Kumoa on its bar
- **THEN** Rahka is not logged, and back with nothing else added returns where the search was opened

#### Scenario: Nothing added
- **WHEN** the user opens Lisää annos from the front page and taps back without adding
- **THEN** the front page is shown as before

#### Scenario: From the front page
- **WHEN** the user opens Lisää annos from the front page's ⋮ menu, adds Pulla and taps back
- **THEN** the Ruoka screen shows today with Pulla highlighted

#### Scenario: Set the amount before adding
- **GIVEN** a ruoka Jauhelihakastike 140 kcal / 100 g, usually 250 g
- **WHEN** the user types "jauh", taps ⋮ → Muokkaa on its row, changes the amount to 350 g and taps
  Lisää
- **THEN** Jauhelihakastike 350 g, 490 kcal is logged, the search is shown again with the bar, and
  the next Jauhelihakastike starts at 350 g

#### Scenario: Edit and cancel
- **WHEN** the user opens the new-annos editor and then taps Peruuta
- **THEN** nothing is added and the search is as before

#### Scenario: What Enter does
- **WHEN** the user types "kana"
- **THEN** matching ruuat are listed under the field, the last row reads "Lisää ”kana” uutena",
  and tapping it adds what Enter would

#### Scenario: Only a number
- **WHEN** the user types 350 and presses Enter
- **THEN** an annos "Pikalisäys" with 350 kcal is logged, the field is cleared and the bar shows

#### Scenario: A whole day as one number
- **GIVEN** the user estimated yesterday at 2 200 kcal with an AI chat
- **WHEN** they move to yesterday on Ruoka, tap Lisää annos, type 2200 and press Enter
- **THEN** the search reads "Lisätään: Eilen", yesterday gets an annos "Pikalisäys" 2 200 kcal,
  and back shows yesterday on Ruoka with it highlighted

#### Scenario: Only a name
- **WHEN** the user types "työpaikan pizza", which is no ruoka yet, and presses Enter
- **THEN** an annos "työpaikan pizza" without kcal is logged

#### Scenario: Forgiving search
- **WHEN** the user types "jaulihak"
- **THEN** Jauhelihakastike is suggested (letters ä/a and ö/o equal, parts of compound words and
  one wrong letter from four typed letters match)

#### Scenario: Back with the keyboard up
- **WHEN** the keyboard is up and the user presses system back
- **THEN** the keyboard hides and the search stays open

#### Scenario: Long name on one line
- **WHEN** a ruoka "Keitetty täysjyvämakaroni" is listed on a narrow phone
- **THEN** the row shows the name on one line shortened with "…", the amount and kcal under it,
  and the ⋮ button in full

### Requirement: Search messages and loading
A failed write SHALL show the bar "Tallennus epäonnistui" (without Kumoa) and keep the search open.
The field SHALL be usable at once. The day SHALL be read in the background when the search opens;
an add made before the day is read SHALL be placed at the end of the day once it has been read. A
failed read SHALL show the bar "Päivän annoksia ei voitu lukea" once, typing and adding SHALL
still work, and the day SHALL be read again when the search resumes and before the next write.

#### Scenario: Add before the day is read
- **WHEN** the user types a name and presses Enter before the day has loaded
- **THEN** the annos is added at the end of the day once it has loaded

#### Scenario: Failed write
- **WHEN** Health Connect refuses a write
- **THEN** the bar "Tallennus epäonnistui" is shown and the search stays open

#### Scenario: Day cannot be read
- **WHEN** reading the day fails
- **THEN** a bar says so, typing still works, and an added annos is placed at the end after the
  day has been read again

### Requirement: Search without permission
The add search SHALL check the nutrition permission before showing its rows and focusing the field:
while checking it SHALL show only the top bar, without the keyboard. Without the permission it
SHALL show the explanation and a Salli button requesting it, and show the rows, focus the field
and raise the keyboard once granted.

#### Scenario: Permission missing
- **GIVEN** the nutrition permission is not granted
- **WHEN** the user taps the widget
- **THEN** the search explains why the permission is needed and offers Salli; once granted the
  rows show and the keyboard comes up

### Requirement: Search with permission denied or Health Connect missing
When the permission has been denied the add search SHALL show the same text and a button that
opens Health Connect's app permissions, and check again when the user returns. When Health Connect
is not available it SHALL show the existing text; the back arrow closes the search.

#### Scenario: Permission denied
- **GIVEN** the permission was denied
- **WHEN** the user taps the widget
- **THEN** the search offers a button to Health Connect's permissions and checks again on return

### Requirement: Search over midnight
The add search SHALL add to the day it was opened on. After midnight the line "Lisätään: Eilen"
SHALL show, and leaving after an add SHALL show that day on Ruoka. A new open SHALL use the new
today.

#### Scenario: Open past midnight
- **GIVEN** the search was opened at 23.55
- **WHEN** the user adds an annos at 00.05
- **THEN** the annos goes to the earlier day and the search reads "Lisätään: Eilen"

### Requirement: Import from the home-screen search
"Tuo ruoka-annokset" and the card's "Muokkaa" in the search opened from the widget or the shortcut
SHALL close that search and open the app on the import preview over the front page, as when the
estimate is shared to the app. Any search or editor open in the app SHALL close. After Lisää or
Peruuta the user SHALL stay in the app. The card's "Lisää kaikki" SHALL save in the search itself
and keep it open.

#### Scenario: Import from the search
- **WHEN** the user taps Tuo ruoka-annokset in the search opened from the widget
- **THEN** the app opens on the import preview, as when the estimate is shared to the app

#### Scenario: After the import
- **WHEN** the user taps Lisää or Peruuta on the preview
- **THEN** the app stays open

#### Scenario: Offer in the search
- **GIVEN** the user copied food JSON with 4 items
- **WHEN** they open the search from the widget and tap Lisää kaikki
- **THEN** the 4 annokset are added to today, the search stays open and shows
  "4 annosta lisätty · Kumoa"

### Requirement: Removing a product from the search
Each ruoka row in the add search SHALL end with a ⋮ button ("Toiminnot: <name>") whose menu offers
"Muokkaa" (the editor for a new annos, see "Adding from the search") and, for an own ruoka only, "Poista"
(in the theme's error colour). Poista SHALL remove the ruoka (as removing it under Omat ruuat in
Asetukset does: logged annokset keep their values), take it off the rows and show the bar
"<name> poistettu · Kumoa"; Kumoa SHALL bring the ruoka back as it was. Valmiit ruuat SHALL have
no Poista.

#### Scenario: Remove a one-off
- **WHEN** the user types "lumi" and taps ⋮ → Poista on Lumipallo
- **THEN** Lumipallo is gone from the rows, earlier Lumipallo annokset stay on their days, and
  Kumoa brings it back

#### Scenario: Valmis ruoka
- **WHEN** the user types "kaurapuu" and Kaurapuuro is a Valmis ruoka not yet used
- **THEN** its ⋮ menu offers Muokkaa but no Poista

## MODIFIED Requirements

### Requirement: Clipboard offer
When the front page, the Ruoka screen or the add search (in the app or opened from the widget or
shortcut) opens or the app resumes on one of them, and the clipboard holds usable food JSON that
has not been imported or skipped, the app SHALL offer it. In the add search (under the top bar,
above the rows) and on the Ruoka screen (at the top of the day) the offer SHALL be a card: a title
"Leikepöydällä" with the kcal total the preview would show, then each item on its own line with
name, amount, treat sign and kcal (at most five lines, then "+<N> muuta"), then a filled primary
button "Lisää kaikki", an outlined "Muokkaa" and a ✕ button (Ohita). "Lisää kaikki" SHALL save the
content as the preview's Lisää would, to the day the search or Ruoka shows, without opening the
preview, and show "<N> annosta lisätty · Kumoa"; in the search those annokset count as added in
this search. "Muokkaa" SHALL open the preview. When the JSON holds only products the card SHALL
list the products with their energy and its button SHALL read "Tallenna ruuat". On the front page
the same card SHALL be shown above the cards, adding to today. The offer SHALL be shown again on
every open and resume while the same content is on the clipboard, until the user saves that
content (Lisää kaikki or the preview's Lisää) or taps Ohita or ✕; after either, that content
SHALL NOT be offered again, on any screen, also when it is copied again; Kumoa of the import SHALL
NOT bring it back. The example JSON that Kopioi tuontiohje puts on the clipboard SHALL NOT be
offered. Content counts as the same when its JSON is the same, whatever text surrounds it. Content
imported by sharing it to the app or by Tuo ruoka-annokset SHALL count as imported too. Peruuta on
the preview SHALL leave the content offered. Clipboard text without food JSON SHALL show nothing.
In the search opened from the widget or shortcut the clipboard SHALL be read when its window gains
focus. The app SHALL read the clipboard's text only when the clipboard has changed since the app
last read it (Android may then show its own "pasted" notice once per new copy) and when the offer
is used, and SHALL NOT read it anywhere else or in the background. The offer SHALL NOT be shown on
Ruoka without the food permission, before the day is read or in Järjestä, nor in the front page's
edit mode.

#### Scenario: Copied in the chat
- **GIVEN** the user copied the chat's answer with food JSON of 3 items
- **WHEN** they open Lisää annos
- **THEN** the card lists the 3 items and the total under the top bar, and one tap on Lisää kaikki
  adds them to today with "3 annosta lisätty · Kumoa"

#### Scenario: Fix before adding
- **WHEN** the user taps Muokkaa on the card
- **THEN** the preview opens with the same items

#### Scenario: Offered on the front page
- **GIVEN** the user copied food JSON with 4 items
- **WHEN** they open the app on the front page
- **THEN** the card with the 4 items is shown above the cards, and Lisää kaikki adds them to today

#### Scenario: Still offered after looking elsewhere
- **GIVEN** the user copied food JSON and the front page already offered it
- **WHEN** they open Lisää annos from the widget
- **THEN** the search offers the same content

#### Scenario: Not offered after import
- **GIVEN** the user added the offered content with Lisää kaikki
- **WHEN** they open Lisää annos without copying anything new
- **THEN** no offer is shown

#### Scenario: Cancelled preview
- **WHEN** the user opens the preview from the offer and taps Peruuta
- **THEN** the offer is shown again on the next open

#### Scenario: Offered once
- **GIVEN** the user tapped ✕ on the card
- **WHEN** they open Lisää annos without copying anything new
- **THEN** no offer is shown

#### Scenario: Import undone
- **GIVEN** the user saved the offered content and tapped Kumoa
- **WHEN** they open the Ruoka screen without copying anything new
- **THEN** no offer is shown

#### Scenario: Import instructions copied
- **WHEN** the user taps Kopioi tuontiohje and opens the Ruoka screen
- **THEN** no offer is shown

#### Scenario: Other text
- **GIVEN** the clipboard holds a web address
- **WHEN** the Ruoka screen opens
- **THEN** no offer is shown

### Requirement: Widget and shortcut for adding
The app SHALL offer a 1x1 home-screen widget "Lisää annos" in the same round style and colours as
the Punnitse widget, with a cutlery icon, and a launcher shortcut "Lisää annos" after Punnitse on
the app icon. The widget SHALL never change with what was logged and SHALL show no state mark.
There SHALL be no Quick Settings tile for food.

#### Scenario: From the widget
- **WHEN** the user taps the Lisää annos widget
- **THEN** the add search opens over the whole screen with the keyboard up

#### Scenario: From the shortcut
- **WHEN** the user long-presses the app icon and taps Lisää annos
- **THEN** the same search opens

### Requirement: Adding from the home screen
A tap on the widget or the shortcut SHALL open the add search (as in "Adding from the search") over the
whole screen, adding to today, in the app's theme, quickly and without first showing the app's
front page. It SHALL work as in the app: suggestions, search, Enter, the bar with Kumoa, and it
SHALL stay open for the next annos. Leaving it SHALL:
- when at least one annos added in it is still logged: open the app on the Ruoka screen of that
  day with those annokset highlighted, as "Adding from the search" says; back from there follows the
  app's own back rules;
- otherwise: close it and show what was behind it, normally the home screen.

#### Scenario: Breakfast from the home screen
- **GIVEN** on most mornings around 7 the user logged Rahka 200 g
- **WHEN** they tap the Lisää annos widget at 7.20, tap Rahka and tap back
- **THEN** Rahka 200 g is added to today and the app shows Ruoka with Rahka highlighted

#### Scenario: Several items in a row
- **WHEN** the user types "kahvi", presses Enter, then taps Pulla, then taps back
- **THEN** both are added to today in that order and Ruoka shows both highlighted

#### Scenario: Nothing added
- **WHEN** the user opens the search from the widget and taps back without adding
- **THEN** the home screen is shown again and the app does not open

#### Scenario: Closing at once
- **WHEN** the user taps a suggestion and leaves the search immediately
- **THEN** the annos is still in Health Connect

#### Scenario: Opened again while open
- **WHEN** the user taps the widget again while the search is open
- **THEN** the search stays as it was

### Requirement: Food only inside the app
Food and kcal SHALL be shown only inside the app, its add search opened from the widget or
shortcut included: no widget face, tile, shortcut label or notification SHALL show what was eaten,
how many items or how many kcal. A widget or shortcut that only opens the add search is allowed.

#### Scenario: Nothing outside
- **WHEN** the user logs food
- **THEN** no notification appears and no widget changes

#### Scenario: Dialog is inside
- **WHEN** the user adds an annos in the search opened from the widget
- **THEN** the search shows its bar, and the widget itself shows nothing about it

### Requirement: Item row buttons
Every logged item row on the Ruoka screen SHALL be one line: the name with its herkku sign, the
kcal ("ilman kcal" when missing), an ⓘ button and one ⋮ button, in that order. The row keeps the
56 dp minimum height of the app's list rows.

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
it, and only close it: that tap or back opens no editor and presses nothing. The bubble also
closes when its item disappears; if the item changes while open, its lines follow. The bubble has
no buttons and changes nothing; opening or closing it SHALL leave the highlight as it was.
Long-press on ⓘ does nothing extra. TalkBack SHALL announce the bubble's title and lines once when
it opens. ⓘ appears only on annos rows on Ruoka, not on ruoka rows or paino rows.

The ⋮ button (named "Toiminnot: <name>") opens a menu with "Muokkaa" (opens the item's editor) and
"Poista" (in the theme's error colour) that deletes the item at once and shows "<name> poistettu ·
Kumoa" as Ruoka's Kumoa bar; Kumoa SHALL bring the item back in its place. Poista SHALL act exactly
as the editor's Poista. On a narrow row the name SHALL be shortened first; kcal, ⓘ and ⋮ never
shrink. Tapping elsewhere on the row SHALL still open the editor. Järjestä rows are one line too,
with only their own move buttons (no ⓘ or ⋮).

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
- **GIVEN** an ⓘ bubble is open on Ruoka (the add search no longer lists annokset)
- **WHEN** the user presses back
- **THEN** the bubble closes and Ruoka stays as it was

#### Scenario: Delete from the sheet
- **GIVEN** the user came back from the add search with Tomaatti highlighted on Ruoka
- **WHEN** they tap ⋮ on Tomaatti and then Poista
- **THEN** Tomaatti is gone, the bar reads "Tomaatti poistettu · Kumoa", and Kumoa brings it back
  in its place without the highlight

#### Scenario: Edit from Ruoka
- **WHEN** the user taps ⋮ on Kurkku on the Ruoka screen and then Muokkaa
- **THEN** Kurkku's editor opens

#### Scenario: Menu closed without a choice
- **WHEN** the user opens the ⋮ menu on Kurkku and taps outside it
- **THEN** the menu closes and nothing changes

### Requirement: Undoing a correction
After Päivitä kaikki a bar "Päivitetty <N> annosta · Kumoa" SHALL show where the edit was made
(in the add search as its bar, replacing the "lisätty" bar; the app bar on Ruoka and in Omat
ruuat) for as long as such bars last. Kumoa SHALL undo the whole save: the ruoka's old energy, the
edited item as it was, and every recalculated item whose kcal is still amount × new energy (an
item changed again since stays). When the bar is gone Kumoa is gone. When writing the recalculated
items fails, the bar SHALL read "Päivitettiin <X>/<N> annosta · Yritä uudelleen"; a retry SHALL
recalculate from the items as they are now.

#### Scenario: Undo
- **WHEN** the user taps Kumoa on "Päivitetty 3 annosta"
- **THEN** Lakritsi is 10 kcal / kpl again and the three items read 50 kcal

## REMOVED Requirements

### Requirement: Adding an item
**Reason**: The add sheet is replaced by the full-screen add search.
**Migration**: replaced by "Adding from the search".

### Requirement: Sheet messages and loading
**Reason**: The add sheet is replaced by the full-screen add search.
**Migration**: replaced by "Search messages and loading".

### Requirement: Sheet without permission
**Reason**: The add sheet is replaced by the full-screen add search.
**Migration**: replaced by "Search without permission".

### Requirement: Sheet with permission denied or Health Connect missing
**Reason**: The add sheet is replaced by the full-screen add search.
**Migration**: replaced by "Search with permission denied or Health Connect missing".

### Requirement: Sheet over midnight
**Reason**: The add sheet is replaced by the full-screen add search.
**Migration**: replaced by "Search over midnight".

### Requirement: Import from the home-screen sheet
**Reason**: The add sheet is replaced by the full-screen add search.
**Migration**: replaced by "Import from the home-screen search".

### Requirement: Removing a product from the sheet
**Reason**: The add sheet is replaced by the full-screen add search.
**Migration**: replaced by "Removing a product from the search".
