## MODIFIED Requirements

### Requirement: Kulutus card
The front page SHALL have a card "Kulutus" built like the steps card at every size: title
"Kulutus", today's total large with "kcal" beside it in a smaller type, as the food card, with "aktiivinen N kcal" under it, small two-tone daily
bars of the card's own period (7, 30 or 90 days, its own menu, remembered, 7 by default), the
period's first and last day and the average total ("keskiarvo 2 300 kcal/pv", "keskiarvo 2 300 kcal/pv" at
small size), the big size's scale top and dashed average line. A tap anywhere but the period label
SHALL open Historia on Kulutus. Without the permissions it SHALL read "Kulutus: anna lupa" and a
tap SHALL ask for them. The card SHALL be hidden by default (see home-screen "Card grid"); its default size is small.

#### Scenario: Card added
- **WHEN** the user adds Kulutus from Lisää kortti and taps Valmis
- **THEN** the card shows today's burned kcal and the last 7 days' bars

