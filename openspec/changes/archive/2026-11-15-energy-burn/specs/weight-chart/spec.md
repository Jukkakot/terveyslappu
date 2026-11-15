# Spec Delta

## ADDED Requirements

### Requirement: Eaten and burned energy names
The eaten-energy tab and data, called Energia in this spec, SHALL read "Syöty" where the user sees
it as a tab or data name (Historia tab, own chart editor, own chart legends and readouts, Ohje); a
product's energy field and the food texts ("Ei ruokakirjauksia…", legend "ruoka", "herkut") stay.
The burned-energy tab and data SHALL read "Kulutus". Both are in kcal. Neither SHALL use plus or
minus signs or show a difference between them. Historia's fixed tabs SHALL be Paino, BMI, Syöty,
Askeleet, Kulutus, then the own charts and Uusi kaavio.

#### Scenario: Tab row
- **WHEN** Historia is opened
- **THEN** the tabs read Paino, BMI, Syöty, Askeleet, Kulutus, the own charts and Uusi kaavio

### Requirement: Kulutus in own charts
Own charts SHALL offer Kulutus as a fourth data, drawn and listed in the order Paino, Syöty,
Kulutus, Askeleet. Kulutus, Pylväät: one bar per day with data, its height the day's total; in the
Allekkain layout split into lepo (light tone) and aktiivinen (full tone) as on the Kulutus tab, in
Päällekkäin one tone. Kulutus, Viiva: a line through each day's total. Its scale SHALL follow the
Syöty rule (`ceil(highest × 1,1 / 500) × 500` kcal, at least 500). With Keskiarvo on, its line is
the average total per day with data, today left out. An own chart SHALL hold one to four data; in
Päällekkäin up to four bar data stand side by side in a day's slot and only the first two drawn
data have a scale. The crosshair readout SHALL show Kulutus as "2 300 kcal · aktiivinen 450" (a
day without a split: "2 300 kcal"), "ei tietoa" without a value. Its Allekkain panel without values
SHALL read "Ei kulutustietoja tällä jaksolla". A new chart SHALL start with Kulutus off.

#### Scenario: Eaten and burned together
- **GIVEN** a chart with Syöty (Pylväät) and Kulutus (Viiva), Allekkain
- **THEN** a Syöty panel and a Kulutus panel are stacked on one time axis, each with its kcal scale

### Requirement: Data colours in own charts
Each data of an own chart SHALL have a colour: "Oletus" (the tone it has today, from the app's
theme; Kulutus's Oletus is oranssi) or one of eight fixed colours: sininen, vihreä, oranssi,
punainen, violetti, turkoosi, keltainen, harmaa, each with a light-theme and a dark-theme shade. A
chart saved before colours existed SHALL read as Oletus for every data, so it looks unchanged. The
colour SHALL be used for the data's bars or line, its value axis labels in Päällekkäin, its legend
swatch and its average line in Päällekkäin; Syöty's treat share and Kulutus's lepo part SHALL use a
lighter tone of the same colour. Two data MAY share a colour. The fixed Historia tabs keep their own
tones.

#### Scenario: Own colour
- **GIVEN** a chart with Askeleet as Pylväät
- **WHEN** the user picks punainen for Askeleet in the editor and saves
- **THEN** the steps bars, the legend swatch and (in Päällekkäin) the steps axis labels are red

#### Scenario: Old chart
- **GIVEN** a chart saved before colours existed
- **THEN** it looks as before and the editor shows Oletus for every data

### Requirement: Chart editor colours
In the chart editor each data row (Paino, Syöty, Kulutus, Askeleet) SHALL hold, after its switch
and its Pylväät / Viiva choice, one colour dot (44 dp touch target, named for TalkBack with its
colour) that opens a small picker of nine choices (Oletus and the eight colours, a 3 × 3 grid of
44 dp targets, the chosen one marked with a check and announced "valittu"); picking closes the
picker. The dot SHALL be shown but disabled while the data is off. Peruuta and back discard a
colour change like any other change.

#### Scenario: Pick a colour
- **WHEN** the user taps Askeleet's colour dot and picks vihreä
- **THEN** the picker closes and the dot shows green; Tallenna keeps it
