# Spec Delta

## ADDED Requirements

### Requirement: Eaten and burned energy names
The eaten-energy tab and data, called Energia in this spec, SHALL read "Syöty" everywhere the user
sees it (Historia tab, own chart editor, legends, chart readouts, Ohje); the burned-energy tab and
data SHALL read "Kulutus". Both are in kcal. Neither SHALL use plus or minus signs or show a
difference between them. Historia's fixed tabs SHALL be Paino, BMI, Syöty, Askeleet, Kulutus, then
the own charts and Uusi kaavio.

#### Scenario: Tab row
- **WHEN** Historia is opened
- **THEN** the tabs read Paino, BMI, Syöty, Askeleet, Kulutus, the own charts and Uusi kaavio

### Requirement: Kulutus in own charts
Own charts SHALL offer Kulutus as a fourth data, drawn and listed in the order Paino, Syöty,
Kulutus, Askeleet. Kulutus, Pylväät: one bar per day with data, its height the day's total; in the
Allekkain layout split into lepo (light tone) and aktiivinen (full tone) as on the Kulutus tab, in
Päällekkäin one tone. Kulutus, Viiva: a line through each day's total. Its scale SHALL follow the
Syöty rule (`ceil(highest × 1,1 / 500) × 500` kcal, at least 500). With Keskiarvo on, its line is
the average total per day with data, today left out. Wherever this spec says "the three data" of an
own chart, it SHALL mean the four; in Päällekkäin only the first two drawn data have a scale. The
crosshair readout SHALL show Kulutus as total kcal with "aktiivinen N".

#### Scenario: Eaten and burned together
- **GIVEN** a chart with Syöty (Pylväät) and Kulutus (Viiva), Allekkain
- **THEN** a Syöty panel and a Kulutus panel are stacked on one time axis, each with its kcal scale

### Requirement: Data colours in own charts
Each data of an own chart SHALL have a colour chosen from a fixed palette of eight: sininen,
vihreä, oranssi, punainen, violetti, turkoosi, keltainen, harmaa, each with a light-theme and a
dark-theme shade that reads on the card background (contrast at least 3:1 for lines and bars). The
defaults SHALL be the current tones: Paino violetti, Syöty sininen, Askeleet turkoosi, Kulutus
oranssi; a chart saved before colours existed SHALL read as the defaults. The chosen colour SHALL be
used for the data's bars or line, its value axis labels in Päällekkäin, its legend swatch, and its
average line in Päällekkäin; Syöty's treat share and Kulutus's lepo part SHALL use a lighter tone
of the same colour. Two data MAY share a colour. The fixed Historia tabs keep their own tones.

#### Scenario: Own colour
- **GIVEN** a chart with Askeleet as Pylväät
- **WHEN** the user picks punainen for Askeleet in the editor and saves
- **THEN** the steps bars, the legend swatch and (in Päällekkäin) the steps axis labels are red

### Requirement: Chart editor colours
In the chart editor each data row (Paino, Syöty, Kulutus, Askeleet) SHALL hold, after its switch
and its Pylväät / Viiva choice, a row of the eight colour dots (each a 44 dp touch target, the
chosen one marked with a check and announced "valittu" to TalkBack, each named for TalkBack),
enabled only while the data is on. A new chart SHALL start with Kulutus off and every colour at its
default; Peruuta and back discard a colour change like any other change.

#### Scenario: New chart
- **WHEN** the user taps Uusi kaavio
- **THEN** Paino, Syöty and Askeleet are on, Kulutus is off and every data shows its default colour
