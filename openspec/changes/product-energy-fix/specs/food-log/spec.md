## ADDED Requirements

### Requirement: Correcting a product's energy
When the user saves a new energy for a product (kcal per piece or per 100 g, from an item's
editor or from Ruokakirjaus), every other logged item of that product (any day) whose kcal
came from the product's energy SHALL be recalculated as amount × new energy. An item counts as
coming from the product's energy when it has an amount in the energy's unit and its kcal equals
amount × old energy (within 1 kcal). Items without an amount, in another unit, or with any other
kcal (the user's own total, an AI import's value) SHALL NOT change. Before any other item
changes, a dialog SHALL ask once: title "Päivitetäänkö aiemmat kirjaukset?", the line "<name>:
<N> kirjausta <D> päivältä, yhteensä <old> → <new> kcal", a line "<M> itse annettua kirjausta
pysyy ennallaan" when M > 0, and the buttons "Päivitä kaikki", "Vain jatkossa" (the product
changes, other items stay) and Peruuta (nothing is saved, the editor stays open). With no
other item to change there SHALL be no dialog. After "Päivitä kaikki" the totals, Energia and
own charts SHALL follow, and a bar "Päivitetty <N> kirjausta · Kumoa" SHALL show; Kumoa SHALL
restore the product's old energy and every changed item's old kcal.

#### Scenario: Heavier liquorice
- **GIVEN** Lakritsi is 10 kcal / kpl, logged 5 kpl (50 kcal) on three days and once with a typed
  total of 80 kcal and no amount
- **WHEN** the user changes Lakritsi to 12 kcal / kpl and taps Päivitä kaikki
- **THEN** the three items read 60 kcal, the 80 kcal item is unchanged, those days' totals rise
  by 10 kcal each, and "Päivitetty 3 kirjausta · Kumoa" shows

#### Scenario: Only from now on
- **GIVEN** the same Lakritsi
- **WHEN** the user changes it to 12 kcal / kpl and taps Vain jatkossa
- **THEN** earlier items keep 50 kcal and the next Lakritsi 5 kpl is 60 kcal

#### Scenario: Own values stay
- **GIVEN** Rahka 65 kcal / 100 g, an item 200 g at 130 kcal and an AI-imported item 200 g at 150 kcal
- **WHEN** the user changes Rahka to 70 kcal / 100 g and taps Päivitä kaikki
- **THEN** the first item reads 140 kcal and the imported one stays 150 kcal

#### Scenario: Undo
- **WHEN** the user taps Kumoa on "Päivitetty 3 kirjausta"
- **THEN** Lakritsi is 10 kcal / kpl again and the three items read 50 kcal

#### Scenario: Nothing earlier
- **GIVEN** a product logged only today, in the item being edited
- **WHEN** the user changes its energy and saves
- **THEN** no dialog is shown

### Requirement: Product use count
The product editor in Ruokakirjaus SHALL show under the energy field how many times the product is
logged ("Kirjattu 6 kertaa"), so the reach of an energy change is visible; a product never logged
SHALL show nothing there.

#### Scenario: Use count
- **GIVEN** Lakritsi logged on four items
- **WHEN** the user opens Lakritsi in Ruokakirjaus
- **THEN** the editor reads "Kirjattu 4 kertaa" under the energy
