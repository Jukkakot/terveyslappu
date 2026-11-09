## MODIFIED Requirements

### Requirement: Weight chart
Historia SHALL show, above the weight list, a chart of the weights this app wrote (scale, manual and
imported) in the chosen period: each weight as a dot, joined by a line in time order. Weights written
by other apps SHALL NOT be shown.

#### Scenario: A week of weigh-ins
- **GIVEN** the app wrote seven weights in the last seven days
- **WHEN** Historia is shown with period 1 vk
- **THEN** the chart shows seven dots joined by a line, with day labels

#### Scenario: Other app's weight
- **GIVEN** another app wrote a weight yesterday
- **WHEN** the chart is shown
- **THEN** that weight is not in the chart

#### Scenario: No weights in the period
- **WHEN** the chosen period has no weights of this app
- **THEN** the chart area says there are no weights in the period
