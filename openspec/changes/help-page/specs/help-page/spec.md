## ADDED Requirements

### Requirement: Ohje screen
Asetukset's Tuki section SHALL have a row "Ohje" that opens a scrollable screen "Ohje" in the
app theme (light and dark). The screen SHALL show a short intro line and then one section per
feature, each with a title and one to three sentences: weighing from the widget, the Quick Settings
tile and the launcher shortcut; manual entry; Historia (chart, periods, list); owner details and
BMI; import and export (including OKOK .xls); the automatic backup; Jaa loki. It SHALL NOT describe
automatic weigh-in. Back from Ohje SHALL return to Asetukset.

#### Scenario: Opening the guide
- **WHEN** the user taps Ohje in Asetukset
- **THEN** the Ohje screen opens with the feature sections

#### Scenario: Back
- **WHEN** the user presses back on Ohje
- **THEN** Asetukset is shown
