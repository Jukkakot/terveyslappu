## ADDED Requirements

### Requirement: Keep the raw scale reading
For every saved scale weight the app SHALL keep on the phone the raw data behind it, without any
calculation: the deciding packet's bytes, and its fields as the scale sent them (company id, raw
weight value, decimals, display unit, stable flag, raw impedance, product id, attribute byte, the
scale's address and any further bytes), and SHALL drop or restore them together with the weight.

#### Scenario: Weigh-in kept
- **WHEN** a scale weigh-in saves 82.5 kg from a packet whose impedance field is 5123
- **THEN** the phone keeps the packet's hex bytes and the fields, impedance 5123 as sent, with the
  weight's time

#### Scenario: Whole packet named
- **WHEN** the deciding packet is `033a1403abcd01aabbccddeeff99` with company id `12c0`
- **THEN** the phone keeps `key=12c0`, `weight_raw=826`, `decimals=1`, `unit=kg`, `stable=1`,
  `impedance=5123`, `product_id=abcd`, `attrib=01`, `mac=AA:BB:CC:DD:EE:FF` and `extra=99` (the bytes
  after the 13th)

#### Scenario: Weigh again
- **WHEN** the user taps Punnitse uudelleen and then Kumoa
- **THEN** the raw data is removed and then kept again with the weight

#### Scenario: No impedance
- **WHEN** the scale reports impedance 0 (e.g. shoes on)
- **THEN** the raw data is kept with impedance 0
