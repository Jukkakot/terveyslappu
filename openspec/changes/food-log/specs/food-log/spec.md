## ADDED Requirements

### Requirement: Ruoka day screen
The app SHALL have a Ruoka screen for one day, opening on today. It SHALL show the date with
arrows to the previous and next day (no next day after today), the day's total kcal with the
treats' share apart ("1 850 kcal, josta herkkuja 320"), and the slots Aamupala, Lounas,
Päivällinen, Iltapala and Väliin, each with its logged items (name, grams or servings, kcal), its
sum and an add button. A day with nothing logged SHALL show the empty slots without any warning,
count or judgement. The screen SHALL show no budget, target, remaining calories or streak.

#### Scenario: Today with breakfast
- **GIVEN** today the user logged Kaurapuuro 300 g (210 kcal) and Kahvi 1 annos (5 kcal) in Aamupala
- **WHEN** the Ruoka screen opens
- **THEN** it shows today, Aamupala with both items and 215 kcal, the other slots empty, and the
  total 215 kcal

#### Scenario: Missed day
- **WHEN** the user moves to a day with nothing logged
- **THEN** the five slots are shown empty and nothing else is said about the day

#### Scenario: No next day after today
- **WHEN** the screen shows today
- **THEN** the next-day arrow is not offered

### Requirement: Saved items
The user SHALL be able to save an item with a name, a herkku mark, and one energy form: kcal per
100 g with a usual portion in grams, or kcal per serving. A saved item SHALL be editable and
removable; changing or removing it SHALL NOT change items already logged. Names SHALL be unique,
ignoring case.

#### Scenario: Save a dish
- **WHEN** the user saves Jauhelihakastike, 140 kcal / 100 g, usual portion 250 g
- **THEN** it can be logged in any slot with 250 g prefilled, giving 350 kcal

#### Scenario: Save a fixed item
- **WHEN** the user saves Proteiinijogurtti, 120 kcal per serving
- **THEN** logging it offers 1 annos, giving 120 kcal

#### Scenario: Edit does not rewrite the past
- **GIVEN** Jauhelihakastike was logged yesterday at 350 kcal
- **WHEN** the user changes it to 150 kcal / 100 g
- **THEN** yesterday still shows 350 kcal and a new log of 250 g gives 375 kcal

### Requirement: Ruokakirjaus in Asetukset
Asetukset's Ruokakirjaus sub-screen SHALL list the saved meals and saved items with their kcal per
usual portion or serving, and SHALL let the user add, edit and remove them. Its row in Asetukset
SHALL read the counts ("12 ruokaa, 3 ateriaa").

#### Scenario: See what usual dishes contain
- **WHEN** the user opens Ruokakirjaus
- **THEN** Jauhelihakastike shows 140 kcal / 100 g and 350 kcal per 250 g

### Requirement: Saved meals
The user SHALL be able to save a meal: a name, a slot and a list of saved items with amounts. One
tap on it in its slot SHALL log all its items with those amounts, each as its own logged item.

#### Scenario: Usual breakfast in one tap
- **GIVEN** a saved meal "Arkiaamu" in Aamupala: Kaurapuuro 300 g and Kahvi 1 annos
- **WHEN** the user taps Arkiaamu in Aamupala's add list
- **THEN** both items are logged in Aamupala today and the slot sums 215 kcal

### Requirement: Adding to a slot
A slot's add button SHALL open a list with, in order: the slot's saved meals, the saved items
logged in that slot in the last 60 days ordered by how often, then the other saved items by name,
plus Pikalisäys and Uusi ruoka. Picking a per-100 g item SHALL ask the grams with the usual
portion prefilled; picking a per-serving item SHALL ask the servings with 1 prefilled; both SHALL
show the resulting kcal while typing and SHALL log with one confirm. Pikalisäys SHALL log a bare
kcal number with an optional name and herkku mark. Uusi ruoka SHALL save the item and then log it.

#### Scenario: Frequent first
- **GIVEN** in Lounas the user logged Kanakeitto 6 times and Lasagne once in the last 60 days
- **WHEN** the user opens Lounas' add list
- **THEN** Kanakeitto comes before Lasagne, and both before the never-logged items

#### Scenario: Only the portion varies
- **WHEN** the user picks Jauhelihakastike, changes 250 to 300 g and confirms
- **THEN** 300 g, 420 kcal is logged in the slot

#### Scenario: Quick add
- **WHEN** the user picks Pikalisäys, types 180 and marks herkku
- **THEN** an item "Pikalisäys" 180 kcal marked herkku is logged

### Requirement: Treats apart
An item logged with the herkku mark (from its saved item or set in Pikalisäys) SHALL show a herkku
sign in the day list, and every sum the app shows (slot, day, chart, statistics) SHALL show the
treats' share apart from the rest. The herkku sign SHALL NOT be red or styled as a warning.

#### Scenario: Day with a treat
- **GIVEN** today 1 530 kcal of meals and Suklaapatukka 230 kcal marked herkku in Väliin
- **THEN** the day reads 1 760 kcal, josta herkkuja 230, and the bar has a 230 kcal treat part

### Requirement: Edit and delete logged items
Tapping a logged item SHALL allow changing its amount or slot and deleting it; a delete SHALL offer
undo, as for weights. Earlier days SHALL be editable and loggable the same way as today.

#### Scenario: Delete with undo
- **WHEN** the user deletes Kahvi from Aamupala and taps Kumoa
- **THEN** Kahvi is back with the same amount, kcal and slot

#### Scenario: Log a past day
- **WHEN** the user moves to yesterday and logs Lasagne in Päivällinen
- **THEN** it is shown on yesterday and counted in yesterday's total

### Requirement: Food in Health Connect
Every logged item SHALL be written to Health Connect as a nutrition record with its name, its
energy in kcal and the meal type of its slot (Aamupala breakfast, Lounas lunch, Päivällinen
dinner, Iltapala and Väliin snack); the slot, the grams or servings and the herkku mark SHALL be
kept with the record so the day can be rebuilt from Health Connect alone. The app SHALL show only
the nutrition records it wrote. Nutrition permissions SHALL be asked the first time the Ruoka
screen opens and never from a weigh-in; without them the Ruoka screen SHALL show the reason and a
button to grant them, and the rest of the app SHALL work as before.

#### Scenario: Written with meal type
- **WHEN** the user logs Iltapalaleipä 2 annos (300 kcal) in Iltapala
- **THEN** Health Connect has a nutrition record "Iltapalaleipä", 300 kcal, meal type snack, and
  the app shows it in Iltapala, not Väliin

#### Scenario: Other apps' food
- **GIVEN** Samsung Health holds a nutrition record the user logged there
- **THEN** the Ruoka screen and Energia do not show it

#### Scenario: Permission denied
- **GIVEN** the user denied the nutrition permissions
- **WHEN** the Ruoka screen opens
- **THEN** it explains that the food log needs them and offers a button to grant them, and the
  weigh-in still works

#### Scenario: Record without our details
- **GIVEN** an own nutrition record whose slot details are missing, meal type lunch
- **THEN** it is shown in Lounas without a herkku sign
