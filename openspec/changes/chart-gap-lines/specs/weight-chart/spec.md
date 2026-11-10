## MODIFIED Requirements

### Requirement: Weight chart
Historia SHALL show, above the weight list, a chart of the weights this app wrote (scale, manual and
imported) in the chosen period: each weight as a dot, joined by a straight line from dot to dot in
time order. Where two consecutive weights are more than 30 days apart the line SHALL continue between
them as a dashed straight line, so the trend reads through the gap while it is clear that there were
no weighings in between; a weight with such a gap on both sides SHALL show as a dot even when the
chart otherwise leaves the dots out. The x-axis labels SHALL show day and month, or month and year
when the chart spans more than a year. Weights written by other apps SHALL NOT be shown. The plot
SHALL start right at the y-axis and end at the plot's right edge: the period's start (for Kaikki the
first weight) sits on the y-axis and the period's end on the right edge, with no empty margin before
or after; this holds for every chart and quantity.

#### Scenario: A week of weigh-ins
- **GIVEN** the app wrote seven weights in the last seven days
- **WHEN** Historia is shown with period 1 vk
- **THEN** the chart shows seven dots joined by a solid straight line, with day labels

#### Scenario: Long gap
- **GIVEN** weights until 20.5.2024 and the next one on 18.3.2025
- **WHEN** Historia is shown with period Kaikki
- **THEN** a dashed straight line joins the weight of 20.5.2024 to the weight of 18.3.2025, and the
  line on either side of the gap is solid

#### Scenario: Sparse history
- **GIVEN** weights about every 40 days for a year
- **WHEN** Historia is shown with period Kaikki
- **THEN** every weight is a dot and consecutive dots are joined by dashed straight lines

#### Scenario: Lone weight between gaps
- **GIVEN** a single weight in October 2024 with no other weight within 30 days, in a chart dense
  enough to leave the dots out
- **WHEN** the chart is shown
- **THEN** that weight shows as a dot, joined to its neighbours by dashed lines

#### Scenario: Several years
- **GIVEN** weights from 24.9.2022 to today
- **WHEN** Historia is shown with period Kaikki
- **THEN** the x-axis labels read like "9/2022", "7/2023"

#### Scenario: Other app's weight
- **GIVEN** another app wrote a weight yesterday
- **WHEN** the chart is shown
- **THEN** that weight is not in the chart

#### Scenario: No weights in the period
- **WHEN** the chosen period has no weights of this app
- **THEN** the chart area says there are no weights in the period

#### Scenario: First weight on the axis
- **GIVEN** weights from 24.9.2022 to today
- **WHEN** Historia is shown with period Kaikki
- **THEN** the line starts right at the y-axis, with no gap between the axis and the first weight
