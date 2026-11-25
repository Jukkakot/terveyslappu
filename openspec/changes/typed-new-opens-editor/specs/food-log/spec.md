## MODIFIED Requirements

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
  bare number "Lisää pelkkä <N> kcal"; for a known ruoka its kcal, for a new name "täytä tiedot").
  Clearing the field (✕ or deleting the text) brings the empty-field content back. The list
  scrolls when it does not fit; scrolling it hides the keyboard.
- Every ruoka row starts with a + icon in the primary colour (TalkBack click label "Lisää <name>")
  and shows the ruoka's name (one line, "…" when it does not fit) with the herkku sign for a
  Herkku ruoka, under it the usual amount and its kcal ("200 g · 130 kcal"; with no usual amount
  the Kalorit "140 kcal / 100 g"; "200 g · ei kcal" or "ei kcal" when unknown), and ends with a ⋮
  button ("Removing a product from the search"). Rows have no dividers. The "Lisää … uutena" row
  starts with the same + icon and has no ⋮.
- Tapping "Lisää … uutena" does what Enter does. Enter adds exactly the typed text: a bare number
  as an annos "Pikalisäys" with that many kcal, a known ruoka's name (own, or Valmiit ruuat by name
  or alias) as an annos of that ruoka. A new name does not add at once: it opens the editor for a
  new annos (as ⋮ → Muokkaa below) with the name filled and no kcal, since the details come next;
  Lisää adds it as edited, Peruuta returns to the search with the typed text kept and adds nothing.
- After any add the keyboard stays as it was and the bar "<name> <amount> lisätty · Kumoa" shows
  ("Search messages and loading"); Kumoa removes that annos.
- Added mark: a ruoka row whose ruoka has annokset counted as added in this search ("Leaving the
  search") shows a mark at its end, just before its ⋮: "✓" for one annos, "×2", "×3" … for more,
  in the primary colour. The mark is not a control (a tap on it is a tap on the row, which adds
  again); TalkBack reads it as part of the row ("lisätty", "lisätty 2 kertaa"). An annos counts
  for the ruoka it was logged as, after the typed name was resolved to a ruoka (a row tap,
  Muokkaa → Lisää, Enter on a known name, the clipboard's Lisää kaikki), and names are compared
  as ruoka names are (case and surrounding spaces do not matter). A Valmiit ruuat row whose alias
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

#### Scenario: Only a name
- **GIVEN** no ruoka is named "työpaikan pizza"
- **WHEN** the user types "työpaikan pizza" and presses Enter
- **THEN** the editor for a new annos opens with the name "työpaikan pizza", Lisää and Peruuta, and
  nothing is logged until Lisää

#### Scenario: Known name adds at once
- **GIVEN** an own ruoka Rahka 130 kcal
- **WHEN** the user types "rahka" and presses Enter
- **THEN** Rahka is logged at once with the bar "Rahka 200 g lisätty · Kumoa"
