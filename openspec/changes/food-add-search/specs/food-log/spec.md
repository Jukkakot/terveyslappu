## ADDED Requirements

### Requirement: Adding from the search
"Lisää annos" (in the ⋮ menu of the front page and Historia, and on the Ruoka screen) SHALL open
the add search over the whole screen, adding to the day shown (today from the front page and
Historia). It SHALL show only what can be added: no day list, no day total, no summary line and
no eaten annos. Tapping a ruoka row SHALL add that ruoka at once at the end of the day with its
usual amount; the field is then cleared and the search stays open for the next annos.

Details:
- Top bar: a back arrow ("Takaisin"), the field with a search icon and the hint "Hae ruokaa tai
  kirjoita kcal", a ✕ button ("Tyhjennä") while the field has text, and a ⋮ menu holding Kopioi
  tuontiohje and Tuo ruoka-annokset. The field has focus and the keyboard is up when the search
  opens, from every entry, once the permission is confirmed granted ("Search without permission").
- The line "Lisätään: <day>" (the day's title as elsewhere: Eilen, else the date like "Su 15.11.";
  bodyMedium, onSurfaceVariant) shows only when the search's day is not the current date, from the
  start (also when opened from Ruoka on a past day).
- The clipboard card, when offered ("Clipboard offer"), hidden while typing.
- Empty field: the heading "Usein tähän aikaan" and up to eight ruoka rows: the ruuat most often
  logged within two hours of the current clock time in the last 30 days, then the most often
  logged ruuat. With none, the heading is left out and a quiet line "Kirjoita ruuan nimi tai
  pelkkä kcal-määrä" shows, but only when the day and history were read successfully (see
  "Search messages and loading"). The ranking is computed when the search opens and stays fixed
  while it is open: adds do not reshuffle the rows; only a removed ruoka drops out, and Kumoa of
  its removal brings it back in its place.
- Typing: up to ten matching ruuat (own before Valmiit ruuat, best match first, directly under
  the top bar) and as the last row "Lisää ”<text>” uutena" with a line telling what it adds (for a
  bare number "Lisää pelkkä <N> kcal"; for other text "ilman kcal" or the known ruoka's kcal).
  Clearing the field (✕ or deleting the text) brings the empty-field content back. The list
  scrolls when it does not fit; scrolling it hides the keyboard.
- Every ruoka row starts with a + icon in the primary colour (TalkBack click label "Lisää <name>")
  and shows the ruoka's name (one line, "…" when it does not fit) with the herkku sign for a
  Herkku ruoka, under it the usual amount and its kcal ("200 g · 130 kcal"; with no usual amount
  the Kalorit "140 kcal / 100 g"; "200 g · ei kcal" or "ei kcal" when unknown), and ends with a ⋮
  button ("Removing a product from the search"). Rows have no dividers. The "Lisää … uutena" row
  starts with the same + icon and has no ⋮.
- Tapping "Lisää … uutena" does what Enter does. Enter adds exactly the typed text: a bare number
  as an annos "Pikalisäys" with that many kcal, any other text as an annos of that name (its
  ruoka's kcal when the name is a known ruoka, otherwise without kcal).
- After any add the keyboard stays as it was and the bar "<name> lisätty · Kumoa" shows ("Search
  messages and loading"); Kumoa removes that annos.
- The ⋮ menu's "Muokkaa" opens the editor for a new annos: the same editor and rules as an annos's
  Muokkaa (name, amount and unit, Kalorit, Herkku; changes reach the ruoka as after Tallenna),
  filled as a tap would add it, with Lisää and Peruuta instead of Tallenna and Poista. Lisää adds
  the annos as edited (with the same bar) and returns to the search; Peruuta, or closing the
  editor, returns to the search and adds nothing. The search returns with the field as it was and
  the keyboard up only if it was up before.
- The search cannot be opened from Järjestä, and Ruoka's own clipboard card is not shown while the
  search is open.

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

#### Scenario: Rows stay put
- **GIVEN** the empty field lists Rahka first and Kahvi second
- **WHEN** the user taps Kahvi
- **THEN** the rows keep their order, and a ruoka removed with Poista drops out while Kumoa
  brings it back in its place

#### Scenario: Past day from Ruoka
- **WHEN** the user opens Lisää annos on Ruoka showing yesterday
- **THEN** the line "Lisätään: Eilen" shows from the start

#### Scenario: Tap target label
- **WHEN** TalkBack reads a ruoka row
- **THEN** its click action is named "Lisää Rahka"

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

### Requirement: Leaving the search
Leaving the search (the back arrow, or system back when the keyboard is down; back with the
keyboard up first hides it) SHALL, when at least one annos added in this search is counted as added
(below), show the Ruoka screen on the search's day with those annokset highlighted ("Highlight on
Ruoka") and the list scrolled so the last of them is visible; otherwise it SHALL return to the
screen it was opened from, as it was.

An annos counts as added in this search from the moment its add is queued (also before the day has
been read or while it is being written). It stops counting when its write fails, when Kumoa removes
it and when it is deleted. Back with only failed adds therefore adds nothing.

Ruoka keeps its own back rule and origin. When the search was opened from Ruoka, leaving stays on
Ruoka and Ruoka's origin is unchanged. When it was opened from Historia, back from that Ruoka
returns to Historia on the tab that was open; when from the front page, back returns to the front
page.

Only the back arrow and system back apply this rule. Home and the recents button only send the
search to the background. When the process was killed or the screen was recreated so that the
search session is lost, the next open is a new search and nothing is highlighted.

A bar showing when the user leaves the search SHALL be dropped, so its Kumoa is gone; the annos
can still be deleted on Ruoka.

#### Scenario: A meal of several annokset
- **WHEN** the user taps Rahka, types "kahvi" and presses Enter, then taps back
- **THEN** both are logged in that order, the Ruoka screen shows the day with Rahka and kahvi
  highlighted and kahvi visible

#### Scenario: Undo in the search
- **WHEN** the user taps Rahka and then Kumoa on its bar
- **THEN** Rahka is not logged, and back with nothing else added returns where the search was opened

#### Scenario: Failed add only
- **WHEN** the user's only add fails with "Tallennus epäonnistui" and the user taps back
- **THEN** nothing is highlighted and the user returns where the search was opened

#### Scenario: Nothing added
- **WHEN** the user opens Lisää annos from the front page and taps back without adding
- **THEN** the front page is shown as before

#### Scenario: From the front page
- **WHEN** the user opens Lisää annos from the front page's ⋮ menu, adds Pulla and taps back
- **THEN** the Ruoka screen shows today with Pulla highlighted, and back from Ruoka returns to the
  front page

#### Scenario: From Historia
- **GIVEN** Historia is on its Syöty tab
- **WHEN** the user opens Lisää annos from its menu, adds Pulla, taps back and then back again on Ruoka
- **THEN** Ruoka showed Pulla highlighted and the second back shows Historia on the Syöty tab

#### Scenario: From Ruoka
- **GIVEN** the user opened Ruoka from the front page and Lisää annos from Ruoka
- **WHEN** the user adds Pulla and taps back, and then back again
- **THEN** Ruoka is shown with Pulla highlighted and the second back returns to the front page

#### Scenario: Home button
- **WHEN** the user adds Pulla in the search and presses the home button
- **THEN** the search is only sent to the background and no Ruoka screen is opened

#### Scenario: Session lost
- **GIVEN** the search was in the background and the system ended the app's process
- **WHEN** the user opens Lisää annos again
- **THEN** it is a new search and nothing is highlighted on Ruoka

#### Scenario: Bar dropped on leaving
- **WHEN** the user taps Pulla and leaves the search while "Pulla lisätty · Kumoa" shows
- **THEN** the bar is gone and Pulla can be deleted on Ruoka

### Requirement: Highlight on Ruoka
The annokset added in a search SHALL be shown on Ruoka with a tonal background (the highlight).
The highlight SHALL be cleared by changing the day, by leaving Ruoka (to the front page, Historia
including Viikko, or Asetukset) and by opening Lisää annos again. It SHALL stay while an annos
editor or Järjestä is open and when the app goes to the background and returns. Editing a
highlighted annos keeps its highlight; deleting it drops it, and Kumoa of the delete brings the
annos back without the highlight.

#### Scenario: Cleared by leaving
- **GIVEN** Ruoka shows Pulla highlighted
- **WHEN** the user opens Historia and returns to Ruoka
- **THEN** Pulla is no longer highlighted

#### Scenario: Cleared by changing the day
- **WHEN** the user moves from the highlighted day to the previous day and back
- **THEN** nothing is highlighted

#### Scenario: Kept while editing
- **WHEN** the user opens a highlighted annos's editor, saves, and the app goes to the background and returns
- **THEN** the annos is still highlighted

#### Scenario: Delete and Kumoa
- **WHEN** the user deletes a highlighted annos and taps Kumoa
- **THEN** it is back in its place without the highlight

### Requirement: Search messages and loading
Every message of the add search SHALL be shown as the app's bar at the search's bottom (above the
keyboard when it is up): "<name> lisätty · Kumoa", "<N> annosta lisätty · Kumoa",
"<name> poistettu · Kumoa", "Ohitettu · Kumoa", "Päivitetty <N> annosta · Kumoa",
"Päivitettiin <X>/<N> annosta · Yritä uudelleen", the clipboard-changed notice, the energy read
failure, "Tallennus epäonnistui" and "Päivän annoksia ei voitu lukea". The newest replaces the
previous one. A bar with Kumoa or Yritä uudelleen lasts as the app's Kumoa bars (about 4 seconds,
swipe away); a bar without an action lasts the app's normal short message length.

A failed write SHALL show "Tallennus epäonnistui" and keep the search open. The field SHALL be
usable at once. The day SHALL be read in the background when the search opens; an add made before
the day is read SHALL be placed at the end of the day once it has been read. A failed read SHALL
show "Päivän annoksia ei voitu lukea" once, typing and adding SHALL still work, and the day SHALL
be read again when the search resumes and before the next write.

While the day or history is loading, with the field empty, nothing SHALL show below the field
except a quiet line "Ladataan…". After a failed read the bar shows and no rows are shown. The line
"Kirjoita ruuan nimi tai pelkkä kcal-määrä" shows only when the read succeeded and there are no
suggestion rows.

#### Scenario: Add before the day is read
- **WHEN** the user types a name and presses Enter before the day has loaded
- **THEN** the annos is added at the end of the day once it has loaded, and counts as added from the tap

#### Scenario: Failed write
- **WHEN** Health Connect refuses a write
- **THEN** the bar "Tallennus epäonnistui" is shown, the search stays open and the annos no longer counts as added

#### Scenario: Day cannot be read
- **WHEN** reading the day fails
- **THEN** the bar "Päivän annoksia ei voitu lukea" is shown with no rows, typing still works, and an
  added annos is placed at the end after the day has been read again

#### Scenario: Loading
- **WHEN** the search opens and the day is still being read
- **THEN** only the line "Ladataan…" shows below the empty field

#### Scenario: Newest bar wins
- **WHEN** the user adds Pulla and then removes a ruoka with Poista
- **THEN** only "… poistettu · Kumoa" shows

### Requirement: Search without permission
The add search SHALL check the nutrition permission before showing its rows and focusing the field:
while checking it SHALL show only the top bar, without the keyboard. Without the permission it
SHALL show the explanation and a Salli button requesting it, and show the rows, focus the field
and raise the keyboard once granted. From every entry (the app, the widget and the shortcut) the
field gets focus and the keyboard comes up once the permission is confirmed granted.

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
The add search SHALL add to the day it was opened on. The date SHALL be re-checked when the search
resumes and on each add (there is no live ticker); when the day is then no longer the current
date, the line "Lisätään: Eilen" SHALL show, and leaving after an add SHALL show that day on
Ruoka. The "Usein tähän aikaan" ranking stays as computed when the search opened. A new open SHALL
use the new today.

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
A tap on the widget or the shortcut SHALL open the add search (as in "Adding from the search") over
the whole screen, adding to today, in the app's theme and without first showing the app's front
page, logging its NFR-8 timings (food.sheet.timing) as before. It SHALL work as in the app:
suggestions, search, Enter, the bars with Kumoa, and it SHALL stay open for the next annos.
Leaving it SHALL:
- when at least one annos added in it counts as added ("Leaving the search"): open the app on the
  Ruoka screen of that day. The app's Ruoka SHALL read the day only after the search's pending
  writes have finished, and the highlight SHALL match annokset by the added item's own client id
  and apply as they appear. Back from there follows the app's own back rules;
- otherwise: close it and show what was behind it, normally the home screen.

A tap on the widget or the shortcut while a search is open SHALL leave it as it was when its day is
the same date; when the date has changed it SHALL start a fresh search for today. Adds are already
saved, so nothing is lost.

#### Scenario: Breakfast from the home screen
- **GIVEN** on most mornings around 7 the user logged Rahka 200 g
- **WHEN** they tap the Lisää annos widget at 7.20, tap Rahka and tap back
- **THEN** Rahka 200 g is added to today and the app shows Ruoka with Rahka highlighted

#### Scenario: Back right after an add
- **WHEN** the user taps a suggestion in the search opened from the widget and taps back at once
- **THEN** the app shows Ruoka with that annos present and highlighted

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
- **WHEN** the user taps the widget again while the search is open on the same date
- **THEN** the search stays as it was

#### Scenario: Opened again after midnight
- **WHEN** the user taps the widget while a search from yesterday is open
- **THEN** a fresh search for today opens

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
(in the add search as its bar, replacing the previous one; the app bar on Ruoka and in Omat
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
