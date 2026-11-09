## ADDED Requirements

### Requirement: Weight chart
The main screen SHALL show, above the weight list, a chart of the weights this app wrote (scale,
manual and imported) in the chosen period: each weight as a dot and a smoothed trend line. Weights
written by other apps SHALL NOT be in the chart.

#### Scenario: A week of weigh-ins
- **GIVEN** the app wrote seven weights in the last seven days
- **WHEN** the main screen is shown with period 1 vk
- **THEN** the chart shows seven dots and a trend line across them, with day labels

#### Scenario: Other app's weight
- **GIVEN** another app wrote a weight yesterday
- **WHEN** the chart is shown
- **THEN** that weight is not a dot and does not move the trend

#### Scenario: No weights in the period
- **WHEN** the chosen period has no weights of this app
- **THEN** the chart area says there are no weights in the period

### Requirement: Trend
The trend SHALL be an exponentially smoothed average of the daily weights (the first weigh-in of each
day), where each new day moves the trend 10 % of the way to that day's weight and missing days are
accounted for, computed from all of the app's weights before the period too, so a period starts from
the real trend and not from its first reading.

#### Scenario: Steady weight
- **WHEN** every day weighs 82.0 kg
- **THEN** the trend is 82.0 kg

#### Scenario: One heavy day
- **GIVEN** the trend is 82.0 kg
- **WHEN** the next day weighs 83.0 kg
- **THEN** the trend becomes 82.1 kg

#### Scenario: Two weigh-ins in a day
- **WHEN** a day has 82.4 kg at 7:00 and 83.1 kg at 21:00
- **THEN** the trend uses 82.4 kg for that day and both are dots

### Requirement: Periods
The chart SHALL offer the periods 1 vk, 1 kk, 3 kk, 1 v, Kaikki and Oma, where Oma opens a date-range
picker. The first time 1 vk SHALL be chosen; after that the app SHALL open with the last chosen period.

#### Scenario: Change period
- **WHEN** the user taps 1 kk
- **THEN** the chart, stats and list show the last 30 days

#### Scenario: Own range
- **WHEN** the user taps Oma and picks 1.9.–15.10.
- **THEN** the chart, stats and list show that range and the chip shows the dates

#### Scenario: Remembered
- **GIVEN** the user chose 3 kk
- **WHEN** the app is opened again
- **THEN** 3 kk is chosen

#### Scenario: Long period without history access
- **GIVEN** the history permission is not granted
- **WHEN** the user picks a period reaching further back than 30 days
- **THEN** the chart shows what it can read and offers to grant access to older weights

### Requirement: Period statistics
Under the chart the app SHALL show for the chosen period: the trend at its end, the trend change over
the period in kg, the rate in kg per week, the lowest and highest weight, and the number of weights.

#### Scenario: Losing
- **GIVEN** the trend was 83.0 kg at the start of a 1 kk period and is 82.0 kg now
- **WHEN** the stats are shown
- **THEN** they show trend 82.0, change −1.0 kg and about −0.2 kg/vko

#### Scenario: One weight only
- **WHEN** the period has a single weight
- **THEN** the stats show it as lowest and highest and no change or rate

### Requirement: Point details
Tapping a dot SHALL show that weight's date, time, value and the trend on that day.

#### Scenario: Tap a dot
- **WHEN** the user taps the dot of 82.6 kg on 8.11. at 7.05
- **THEN** the chart shows "8.11. 7.05 82,6 kg" with the trend of that day
