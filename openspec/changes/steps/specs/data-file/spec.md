## MODIFIED Requirements

### Requirement: Data kinds
The app's data file SHALL carry the measured data the app keeps, one part per data kind: `weights`
(every weight with its time, kg, the writing app and its raw data), `foods` (every logged food
item) and `products` (the own products, the user's own work). Owner details, app settings and
state tied to the phone (backup access, the last weigh-in, the log, Health Connect permissions)
SHALL NOT be carried. Steps SHALL NOT be a data kind either: they are measured and kept by Garmin and Health Connect, not by this app. A data kind the app starts keeping later SHALL be added as its own part in
both formats.

#### Scenario: No steps
- **GIVEN** Health Connect holds steps and the steps card is in use
- **THEN** the file holds no steps, and importing a file never changes steps

#### Scenario: Only data
- **GIVEN** owner details are set, the theme is Tumma and the backup is on
- **THEN** the file holds the weights, foods and products and nothing about the owner details,
  the theme or the backup
