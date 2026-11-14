## MODIFIED Requirements

### Requirement: Data kinds
The app's data file SHALL carry the measured data the app keeps, one part per data kind: `weights`
(every weight with its time, kg, the writing app and its raw data), `foods` (every logged food
item) and `food_items` (the own products, the user's own work). Owner details, app settings and
state tied to the phone (backup access, the last weigh-in, the log, Health Connect permissions)
SHALL NOT be carried. A data kind the app starts keeping later SHALL be added as its own part in
both formats.

#### Scenario: Only data
- **GIVEN** owner details are set, the theme is Tumma and the backup is on
- **THEN** the file holds the weights, foods and products and nothing about the owner details,
  the theme or the backup

## ADDED Requirements

### Requirement: Food parts
A `foods` item SHALL hold the day, the logging time when known (with its offset), the position in
the day, the name, the kcal when known, the amount with its unit (`g` or `kpl`) when known, and
`treat` only for a Herkku item. A `food_items` item SHALL hold an own product's name, category and,
when known, its energy (kcal per 100 g or per piece) and last used amount. In CSV each is its own
`#table;` part. Reading a file SHALL write the logged foods to Health Connect, skipping ones
already there, and SHALL add the products whose name is not yet an own product.

#### Scenario: Round trip
- **GIVEN** three logged items, one Herkku and one without kcal, and two own products
- **WHEN** they are exported as JSON and imported on a phone without them
- **THEN** the Ruoka screen shows the same items in the same order with the same kcal and herkku
  sign, and both products are suggested

#### Scenario: Import twice
- **WHEN** the same file is imported twice
- **THEN** each logged item is in Health Connect once and each product once

#### Scenario: Older file
- **WHEN** a file has only a `weights` part
- **THEN** the weights are read and no foods or products change
