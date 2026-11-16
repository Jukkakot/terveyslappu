## MODIFIED Requirements

### Requirement: Treats apart
A product in category Herkku SHALL carry a small herkku sign on its items, and the day total, the
chart and the statistics SHALL show the treats' share apart from base food, so the user sees
whether the excess comes from ordinary meals or from treats. Herkku SHALL NOT be red or styled as
a warning. There SHALL be no other categories.

The treat share text ("herkkuja 230 kcal", "herkkuja ka 230 kcal/pv", the Energia statistics'
Herkut value) SHALL be drawn in the treat tone of the Energia bars wherever it is shown: the Ruoka
summary line, the add sheet's line under its header, the home food card, the Energia list's day and
group rows and the Energia statistics. Only the treat part takes the tone; the other parts of the
same line keep their present colour, and size and weight stay unchanged. A zero or missing
value ("0 kcal/pv", "–") keeps the normal colour. The chart marker card and legends keep their
treat dot or swatch with normal text.

#### Scenario: Day with a treat
- **GIVEN** today 1 530 kcal of Ruoka items and Suklaapala 230 kcal (Herkku)
- **THEN** the day reads 1 760 kcal · herkkuja 230 kcal, and its bar has a 230 kcal treat part

#### Scenario: Treat text in the treat tone
- **GIVEN** today has a 230 kcal treat share and one item without kcal
- **THEN** the Ruoka summary line shows "herkkuja 230 kcal" in the treat tone of the Energia bars
  and "1 ilman kcal" in the header's present colour
