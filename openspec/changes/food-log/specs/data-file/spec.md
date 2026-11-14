## MODIFIED Requirements

### Requirement: Data kinds
The app's data file SHALL carry the measured data the app keeps, one part per data kind: `weights`
(every weight with its time, kg, the writing app and its raw data), `foods` (every logged food
item) and `food_items` (the saved items and meals, the user's own work). Owner details, app
settings and state tied to the phone (backup access, the last weigh-in, the log, Health Connect
permissions) SHALL NOT be carried. A data kind the app starts keeping later SHALL be added as its
own part in both formats.

#### Scenario: Only data
- **GIVEN** owner details are set, the theme is Tumma and the backup is on
- **THEN** the file holds the weights, foods and saved items and nothing about the owner details,
  the theme or the backup

## ADDED Requirements

### Requirement: Food parts
A `foods` item SHALL hold the time (with the record's offset), the slot (`breakfast`, `lunch`,
`dinner`, `evening`, `snack`), the name, the kcal, the grams or servings when known, and
`treat` only for a herkku item. A `food_items` item SHALL hold a saved item's name, herkku mark
and energy form (kcal per 100 g with the usual grams, or kcal per serving), or a saved meal's
name, slot and items with amounts. In CSV each is its own `#table;` part. Reading a file SHALL
write the logged foods to Health Connect, skipping ones already there, and SHALL add the saved
items and meals whose name is not yet saved.

#### Scenario: Round trip
- **GIVEN** three logged items, one marked herkku, and two saved items
- **WHEN** they are exported as JSON and imported on a phone without them
- **THEN** the Ruoka screen shows the same items, slots, kcal and herkku sign, and both saved items
  can be logged

#### Scenario: Import twice
- **WHEN** the same file is imported twice
- **THEN** each logged item is in Health Connect once and each saved item once

#### Scenario: Older file
- **WHEN** a file has only a `weights` part
- **THEN** the weights are read and no foods or saved items change
