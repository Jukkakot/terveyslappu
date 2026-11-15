## MODIFIED Requirements

### Requirement: Ruokakirjaus in Asetukset
Asetukset's Ruokakirjaus sub-screen SHALL list the own products with their category and energy,
and SHALL let the user change the name, energy, usual amount and category, and remove a product
(from suggestions; logged items keep their values) with the product editor's red "Poista tuote",
which shows "<name> poistettu · Kumoa"; Kumoa SHALL bring the product back as it was. Its row in Asetukset SHALL read the count of
own products.

#### Scenario: See what usual dishes contain
- **WHEN** the user opens Ruokakirjaus
- **THEN** Jauhelihakastike shows 140 kcal / 100 g, usually 250 g

#### Scenario: Undo a product removal
- **WHEN** the user removes Jauhelihakastike under Ruokakirjaus and taps Kumoa
- **THEN** Jauhelihakastike is listed again with 140 kcal / 100 g, usually 250 g

### Requirement: Preview and fine-tuning
Before anything is saved, the preview SHALL show every item with its amount and kcal, the sum, the
products to keep, the number of skipped entries, and the day the items go to: today by default;
tapping the day SHALL open a calendar to pick another day (not after today). The user SHALL be able to edit each item (name, amount, kcal,
Herkku) and each product (name, energy, Herkku), and remove items or products, before saving. Removing a row SHALL show "<name> poistettu ·
Kumoa" in the preview; Kumoa SHALL put the row back in its place.
Peruuta SHALL save nothing.

#### Scenario: Fix one portion
- **GIVEN** the preview shows Pulla 1 kpl 250 kcal
- **WHEN** the user changes it to 2 kpl 500 kcal and taps Lisää
- **THEN** Pulla is logged 2 kpl 500 kcal and the sum read 500 kcal more than before

#### Scenario: Shared late in the evening
- **GIVEN** today already holds Rahka 200 g
- **WHEN** the user shares the day's estimate at 21.30
- **THEN** the items are added after Rahka on today

#### Scenario: Yesterday's estimate
- **WHEN** the user shares yesterday's estimate in the morning, taps the day and picks yesterday
- **THEN** the items are added at the end of yesterday, without a shown time

#### Scenario: Cancel
- **WHEN** the user taps Peruuta in the preview
- **THEN** nothing is logged and no product changes

#### Scenario: Undo a removed row
- **WHEN** the user removes Pulla from the preview and taps Kumoa
- **THEN** Pulla is back in the same place and the sum includes it again

## ADDED Requirements

### Requirement: Undoing Ohita
Tapping ✕ (Ohita) on the clipboard card SHALL show "Ohitettu · Kumoa"; Kumoa SHALL make the content
offered again, as if it had never been skipped.

#### Scenario: Skipped by mistake
- **WHEN** the user taps ✕ on the card and then Kumoa
- **THEN** the card is shown again with the same items
