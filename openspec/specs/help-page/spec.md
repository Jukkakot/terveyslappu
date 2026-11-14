# help-page Specification

## Purpose
TBD - created by archiving change help-page. Update Purpose after archive.

## Requirements

### Requirement: Ohje screen
The home screen's help button SHALL open a scrollable screen "Ohje" in the app theme (light and
dark); Asetukset SHALL NOT have an Ohje row. The screen SHALL show a short intro line and then one
section per feature, each with a title and one to three sentences: weighing from the widget, the
Quick Settings tile and the launcher shortcut; manual entry; Historia (chart, periods, list); owner
details and BMI; import and export (including OKOK .xls); the automatic backup; Jaa loki. It SHALL
NOT describe automatic weigh-in. Back from Ohje SHALL return to the home screen.

#### Scenario: Opening the guide
- **WHEN** the user taps the help button on the home screen
- **THEN** the Ohje screen opens with the feature sections

#### Scenario: Back
- **WHEN** the user presses back on Ohje
- **THEN** the home screen is shown
