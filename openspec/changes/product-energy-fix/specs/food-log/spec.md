## ADDED Requirements

### Requirement: Correcting a product's energy
A product's energy SHALL count as corrected when it is saved, from an item's editor (Tallenna or
a new item's Lisää) or from Ruokakirjaus, with the same unit (kpl or 100 g) and a different kcal.
A unit change, a cleared energy, or a save that changes only the amount, Herkku or the name SHALL
NOT count. On a correction, every logged item of that product the app can read from Health
Connect, on any day, whose kcal came from the old energy SHALL be recalculated as amount × new
energy, except the item being edited, which always takes the editor's values. Renaming in the
item editor moves the item to the other product, whose energy alone applies. AI and data-file
imports SHALL never recalculate (they stay append-only).

#### Scenario: Heavier liquorice
- **GIVEN** Lakritsi is 10 kcal / kpl, logged 5 kpl (50 kcal) on three days and once with a typed
  total of 80 kcal and no amount
- **WHEN** the user changes Lakritsi to 12 kcal / kpl in Ruokakirjaus and taps Päivitä kaikki
- **THEN** the three items read 60 kcal, the 80 kcal item is unchanged, those days' totals rise
  by 10 kcal each, and "Päivitetty 3 kirjausta · Kumoa" shows

#### Scenario: Amount only
- **WHEN** the user opens a Lakritsi item, changes 5 kpl to 6 kpl and taps Tallenna
- **THEN** no dialog is shown and no other item changes

#### Scenario: Unit change
- **WHEN** the user changes Lakritsi from 10 kcal / kpl to 10 kcal / 100 g
- **THEN** no dialog is shown and earlier items keep their kcal

### Requirement: Which items follow
An item SHALL count as coming from the product's energy when it has an amount in the energy's unit
and its kcal differs from amount × old energy by less than 0,5 kcal. Items without an amount, in
the other unit, without kcal (they are filled as in "Kcal filled in later") or with any other kcal
(the user's own total, an AI import's value) SHALL NOT change and SHALL be counted as kept.

#### Scenario: Own values stay
- **GIVEN** Rahka 65 kcal / 100 g, an item 200 g at 130 kcal and AI-imported items 200 g at 150
  and at 131 kcal
- **WHEN** the user changes Rahka to 70 kcal / 100 g and taps Päivitä kaikki
- **THEN** the first item reads 140 kcal and the imported ones stay 150 and 131 kcal

#### Scenario: Decimal energy
- **GIVEN** Keksi 12,5 kcal / kpl and an item 3 kpl at 37,5 kcal
- **WHEN** the user changes Keksi to 15 kcal / kpl and taps Päivitä kaikki
- **THEN** the item reads 45 kcal

### Requirement: Confirming a correction
When a correction would change at least one other item, a dialog SHALL ask before anything is
written: title "Päivitetäänkö aiemmat kirjaukset?", the line "<name>: <N> kirjausta <D> päivältä,
yhteensä <old> → <new> kcal" (the sums over those N items, rounded after summing), a line "<M>
itse annettua kirjausta pysyy ennallaan" when M > 0, and the buttons "Päivitä kaikki", "Vain
jatkossa" and Peruuta, with singular forms for 1. The dialog SHALL govern only the recalculation:
the product's new energy, a rename, Herkku and the filling of items without kcal SHALL happen as
today with Päivitä kaikki or Vain jatkossa. Vain jatkossa SHALL save without recalculating;
Peruuta SHALL write nothing and return to the editor. With no other item to change there SHALL be
no dialog.

#### Scenario: Only from now on
- **GIVEN** Lakritsi 10 kcal / kpl logged 5 kpl on three days
- **WHEN** the user changes it to 12 kcal / kpl and taps Vain jatkossa
- **THEN** earlier items keep 50 kcal and the next Lakritsi 5 kpl is 60 kcal

#### Scenario: Cancel
- **WHEN** the user taps Peruuta in the dialog
- **THEN** the editor is still open with the new value typed and nothing is saved

#### Scenario: Nothing else logged
- **GIVEN** a product logged only in the item being edited
- **WHEN** the user changes its energy and saves
- **THEN** no dialog is shown

### Requirement: Undoing a correction
After Päivitä kaikki a bar "Päivitetty <N> kirjausta · Kumoa" SHALL show where the edit was made
(in the add sheet and the Lisää annos dialog on the sheet's line, in place of the edited item
"Adding an item" shows there; the app bar on Ruoka and in
Ruokakirjaus) for as long as such bars last. Kumoa SHALL undo the whole save: the product's old
energy, the edited item as it was, and every recalculated item whose kcal is still amount × new
energy (an item changed again since stays). When the bar is gone Kumoa is gone. When writing the
recalculated items fails, the bar SHALL read "Päivitettiin <X>/<N> kirjausta · Yritä uudelleen";
a retry SHALL recalculate from the items as they are now.

#### Scenario: Undo
- **WHEN** the user taps Kumoa on "Päivitetty 3 kirjausta"
- **THEN** Lakritsi is 10 kcal / kpl again and the three items read 50 kcal

### Requirement: Product use count
The product editor in Ruokakirjaus SHALL show under the energy field how many items of the product
are logged ("Kirjattu 6 kertaa", "Kirjattu 1 kerran"); it SHALL show nothing when the product is
not logged or the items are not known.

#### Scenario: Use count
- **GIVEN** Lakritsi logged on four items
- **WHEN** the user opens Lakritsi in Ruokakirjaus
- **THEN** the editor reads "Kirjattu 4 kertaa" under the energy
