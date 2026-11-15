# Spec Delta

## MODIFIED Requirements

### Requirement: Ruoka day screen
The app SHALL have a Ruoka screen for one day, opening on today, with arrows to earlier days (none
after today). It SHALL list the day's items in eating order, each with its name, amount and kcal,
and, for an item logged on the day itself, its logging time in small muted text. On top it SHALL
show the day's total kcal with the treats' share apart, and the number of items without kcal
("1 760 kcal · herkkuja 230 · 1 ilman kcal"). A day with nothing logged SHALL show only a short
line on how to add, without any warning, count or judgement. The screen SHALL show no budget,
target, remaining calories, streak or meal names. The top bar SHALL have a "Viikko" link that opens
Historia on Energia for 1 vk; back from there SHALL return to the Ruoka screen.

#### Scenario: A day in order
- **GIVEN** today the user logged Rahka 200 g at 7.10, Pullo 1 kpl at 10.05 and Pizza (no kcal) at 12.30
- **WHEN** the Ruoka screen opens
- **THEN** the three items are listed in that order with their times, and the top reads the total
  of Rahka and Pullo and "1 ilman kcal"

#### Scenario: Missed day
- **WHEN** the user moves to a day with nothing logged
- **THEN** only the how-to-add line is shown

#### Scenario: From the day to the week
- **WHEN** the user taps Viikko on the Ruoka screen
- **THEN** Historia opens on Energia with 1 vk, and tapping a day's bar there and then "Avaa päivä ›" opens that day on Ruoka
