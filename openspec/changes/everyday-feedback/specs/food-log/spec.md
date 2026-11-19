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
preview, and show "Lisättiin <N> annosta · Kumoa"; in the search those annokset count as added in
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
  adds them to today with "Lisättiin 3 annosta · Kumoa"

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

### Requirement: Saving an AI import
"Lisää" SHALL add the items in their order at the end of the chosen day, as logged items written to
Health Connect like any item, and SHALL show a line "Lisättiin <N> annosta · Kumoa"; Kumoa SHALL
remove the added items and restore the products as they were. An import SHALL only add: it SHALL
NOT change or delete items already logged, and SHALL NOT touch weights or anything outside the
food log. The JSON's values SHALL win over the app's:
- Each item SHALL be logged under the own product of its JSON name (compared ignoring case, extra
  spaces and ä/a, ö/o; base names and aliases do not match), created under the JSON name (cleaned like a typed name) when there is none, and SHALL be
  its category follows the JSON (below). A base product SHALL NOT rename it or make it a treat.
- An item with both amount and kcal SHALL set its product's energy from the item (kcal per 100 g
  for grams, per piece for kpl) and its usual amount to the item's amount, also when the product
  already exists. When several items of one product carry both, the last one SHALL win, unless
  the import's `products` list holds that product: then the list's energy wins and the items only
  log their own kcal.
- An item without kcal SHALL take its kcal from its product's final energy in this import (after
  all items and the `products` list); when there is no own product of that name and "Valmiit
  ruuat" is on, from the base product of that name or alias. The new own product SHALL then take
  that base product's energy and usual amount, and the item's name and category. The item SHALL
  keep its own amount; when its unit differs from the energy's unit it SHALL be logged without
  kcal. An item without amount SHALL use the product's usual amount.
- A product is always a treat or never (Herkku belongs to the product): every item SHALL set its
  product's category to its `herkku`, also for an existing product, under the same last-wins and
  `products`-list rules as the energy; the item is a treat exactly when its product ends up Herkku.
  The import stays append-only: earlier logged items of the product keep their sign, as they keep
  their kcal (switching Herkku by hand still changes them all).
- A product in the `products` list SHALL be saved as given: created when new, its energy, usual
  amount and category replaced when it exists.
Earlier logged items of a changed product SHALL keep their kcal.

#### Scenario: Undo the import
- **WHEN** the user taps Kumoa on "Lisättiin 2 annosta"
- **THEN** both items are gone and the products are as before

#### Scenario: Earlier items untouched
- **GIVEN** today already holds Rahka 200 g
- **WHEN** an import with Kaurapuuro is added to today
- **THEN** Rahka stays as it was and Kaurapuuro is the last item

#### Scenario: New product learns its energy
- **GIVEN** no product Kaurapuuro
- **WHEN** Kaurapuuro 300 g 210 kcal is added
- **THEN** a product Kaurapuuro 70 kcal / 100 g, usually 300 g, is suggested next time

#### Scenario: Item keeps a known product's values
- **GIVEN** a product Rahka 65 kcal / 100 g
- **WHEN** an item Rahka 200 g 130 kcal is added
- **THEN** the item reads 130 kcal and the product stays 65 kcal / 100 g (usually 200 g from
  then on), with no change line

#### Scenario: Filled with the import's energy
- **GIVEN** a product Rahka 65 kcal / 100 g
- **WHEN** an import holds Rahka 100 g without kcal, then Rahka 200 g 160 kcal
- **THEN** the first item reads 80 kcal and the second 160 kcal

#### Scenario: Product list wins
- **WHEN** an import holds the product Lohikeitto 380 kcal per annos and an item Lohikeitto 400 g
  320 kcal
- **THEN** the item reads 320 kcal and Lohikeitto is 380 kcal / kpl

#### Scenario: Item updates a known product
- **GIVEN** a product Rahka 65 kcal / 100 g, usually 250 g
- **WHEN** an item Rahka 200 g 160 kcal is added
- **THEN** the item reads 160 kcal and Rahka is 80 kcal / 100 g, usually 200 g, from then on;
  earlier Rahka items keep their kcal

#### Scenario: Base product does not take over
- **GIVEN** no own product Ranskalaiset, and a base product Ranskalaiset perunat 290 kcal / 100 g,
  Herkku, with the alias Ranskalaiset
- **WHEN** an item Ranskalaiset 200 g 300 kcal without herkku is added
- **THEN** the item is Ranskalaiset 200 g 300 kcal, not a treat, and an own product Ranskalaiset
  150 kcal / 100 g is suggested next time

#### Scenario: Base product fills a missing kcal
- **GIVEN** no own product Uuniranskalaiset, the base product Uuniranskalaiset 130 kcal / 100 g,
  and Valmiit ruuat on
- **WHEN** an item Uuniranskalaiset 200 g without kcal is added
- **THEN** the item reads 260 kcal and an own product Uuniranskalaiset 130 kcal / 100 g is created

#### Scenario: Category from the JSON
- **GIVEN** an own product Lakritsi in category Herkku
- **WHEN** an item Lakritsi 50 g 180 kcal without herkku is added
- **THEN** the preview shows "Lakritsi: ei herkku" among its lines (with the energy change), and
  after Lisää Lakritsi and its items are not treats

#### Scenario: Product list updates
- **GIVEN** a product Lohikeitto 300 kcal / kpl
- **WHEN** an import with product Lohikeitto 380 kcal per annos is added
- **THEN** Lohikeitto is 380 kcal / kpl from then on; earlier logged items keep their kcal

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
  "Lisättiin 4 annosta · Kumoa"

### Requirement: Adding from the search
"Lisää annos" (in the ⋮ menu of the front page and Historia, and on the Ruoka screen) SHALL open
the add search over the whole screen, adding to the day shown (today from the front page and
Historia). It SHALL show only what can be added and this search's own adds (the marks and the
added strip below): no day list, no day total and no eaten annos. Tapping a ruoka row SHALL add that ruoka at once at the end of the day with its
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
- After any add the keyboard stays as it was and the bar "<name> <amount> lisätty · Kumoa" shows
  ("Search messages and loading"); Kumoa removes that annos.
- Added mark: a ruoka row whose ruoka has annokset counted as added in this search ("Leaving the
  search") shows a mark at its end, just before its ⋮: "✓" for one annos, "×2", "×3" … for more,
  in the primary colour. The mark is not a control (a tap on it is a tap on the row, which adds
  again); TalkBack reads it as part of the row ("lisätty", "lisätty 2 kertaa"). An annos counts
  for the ruoka it was logged as, after the typed name was resolved to a ruoka (a row tap,
  Muokkaa → Lisää, Enter on a known name, the clipboard's Lisää kaikki), and names are compared
  as the search compares them (case and ä/a, ö/o do not matter). A Valmiit ruuat row whose alias
  became an own ruoka under the typed name is marked too. Annokset of ruuat not listed in the
  rows count only in the strip. Kumoa, a failed write or a delete lowers the count; at zero the
  mark goes. The "Lisää … uutena" row has no mark.
- Added strip: while at least one annos counts as added in this search, a strip at the search's
  bottom (above the keyboard when it is up, the bar above the strip) reads "1 annos lisätty ·
  130 kcal" or "2 annosta lisätty · 465 kcal" (every annos counted as added in this whole search,
  also the clipboard's and Pikalisäys, with their known kcal), with
  "· 1 ilman kcal" added when some have no kcal and "· ei kcal" when none has any, and ends with
  a filled "Valmis" button that leaves the search as back does ("Leaving the search"). Numbers
  and their units do not break apart. The strip goes when the count drops to zero.
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
  clears nothing else and shows "Rahka 200 g lisätty · Kumoa"

#### Scenario: Adds stay visible
- **GIVEN** the search is open with Rahka (200 g, 130 kcal) and Kahvi (1 kpl, 5 kcal) among the rows
- **WHEN** the user taps Rahka, Kahvi and Kahvi again and the bars have faded
- **THEN** the Rahka row shows ✓, the Kahvi row shows ×2, and the strip reads "3 annosta lisätty ·
  140 kcal" with Valmis

#### Scenario: Undo lowers the count
- **GIVEN** the Kahvi row shows ×2
- **WHEN** the user taps Kumoa on the latest Kahvi bar
- **THEN** the Kahvi row shows ✓ and the strip counts one annos fewer

#### Scenario: Without kcal
- **WHEN** the user adds Rahka 130 kcal and "työpaikan pizza" without kcal
- **THEN** the strip reads "2 annosta lisätty · 130 kcal · 1 ilman kcal"

#### Scenario: Nothing added yet
- **WHEN** the search opens
- **THEN** no row has a mark and no strip shows

#### Scenario: Valmis
- **GIVEN** the user added Rahka from the front page's Lisää annos
- **WHEN** the user taps Valmis on the strip
- **THEN** the Ruoka screen shows today with Rahka highlighted, as back would

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
Leaving the search (the back arrow, the added strip's Valmis, or system back when the keyboard is
down; back with the keyboard up first hides it) SHALL, when at least one annos added in this search is counted as added
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

Only the back arrow, Valmis and system back apply this rule (also in the search opened from the
home-screen widget or shortcut, "Adding from the home screen"). Home and the recents button only
send the search to the background. When the process was killed or the screen was recreated so that
the search session is lost, the next open is a new search and nothing is highlighted.

A bar showing when the user leaves the search SHALL be dropped, so its Kumoa is gone; the annos
can still be deleted on Ruoka.

Each leaving SHALL be logged as `food.search.end added=<count> via=back|done ms=` (ms since the
search opened; never names or kcal).

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
- **WHEN** the user taps Pulla and leaves the search while "Pulla 1 kpl lisätty · Kumoa" shows
- **THEN** the bar is gone and Pulla can be deleted on Ruoka

#### Scenario: Leaving logged
- **WHEN** the user adds two annokset and taps Valmis
- **THEN** the log has `food.search.end added=2 via=done ms=…`

### Requirement: Search messages and loading
Every message of the add search SHALL be shown as the app's bar at the search's bottom (above the
keyboard when it is up): "<name> <amount> lisätty · Kumoa", "Lisättiin <N> annosta · Kumoa"
(the clipboard's Lisää kaikki: what that tap added, unlike the strip's count of the whole search),
"<name> poistettu · Kumoa", "Ohitettu · Kumoa", "Päivitetty <N> annosta · Kumoa",
"Päivitettiin <X>/<N> annosta · Yritä uudelleen", the clipboard-changed notice, the energy read
failure, "Tallennus epäonnistui" and "Päivän annoksia ei voitu lukea". The newest replaces the
previous one. A bar with Kumoa or Yritä uudelleen lasts as the app's Kumoa bars (about 4 seconds,
swipe away); a bar without an action lasts the app's normal short message length.

The add bar SHALL name what was added with its amount as the ruoka rows write it: "Rahka 200 g
lisätty", "Pulla 1 kpl lisätty"; a Pikalisäys with its kcal ("Pikalisäys 350 kcal lisätty"); an
annos without an amount with its name only ("työpaikan pizza lisätty"). A long name wraps to a
second line rather than cutting the amount off.

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


#### Scenario: Bar names the amount
- **WHEN** the user taps Rahka, whose usual amount is 200 g
- **THEN** the bar reads "Rahka 200 g lisätty · Kumoa"

#### Scenario: Bar for a bare number
- **WHEN** the user types 350 and presses Enter
- **THEN** the bar reads "Pikalisäys 350 kcal lisätty · Kumoa"
