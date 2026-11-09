# Proposal

## Why

The user wants every piece of data the scale (and imported history) provides kept as raw data,
untouched by calculations, while the app shows only the weight (user 2026-11-09). Derived values
(BMI, body fat ...) can then be computed later in the app from the raw data plus the user's own
details. Today only the weight goes to Health Connect and the rest is in the log, which rotates
away. Health Connect has no record for raw impedance or packets.

## What Changes

- A raw-data archive on the phone: one entry per weight, keyed by its time, holding named raw fields
  exactly as received.
- Each saved scale weigh-in stores the full deciding packet (hex) and its fields as sent. The whole
  13-byte payload is now named (openScale `OkOkHandler` C0 path and its older `BluetoothOKOK2`,
  checked 2026-11-09): bytes 0-1 raw weight, 2-3 impedance (tenths of an ohm), 4-5 product id,
  6 attributes (stable, decimals, unit), 7-12 the scale's own Bluetooth address. The company id's
  high byte (`key`) is not a fixed value: openScale scans for every `xxC0` id. Stored fields:
  `packet`, `key`, `weight_raw`, `decimals`, `unit`, `stable`, `impedance`, `product_id`, `attrib`,
  `mac`, plus `extra` for any bytes after the 13th. Every packet of a weigh-in stays in the log as
  today; the packet log line also gets the advertiser's address (`addr=`) so a shared log shows
  whether bytes 7-12 really are it.
- The export and the automatic backup file get one more column `raw` with the entry's fields as
  `name=value` pairs, so the archive leaves the phone with the backup; Tuo historia reads it back.
- Deleting a weight (list, Punnitse uudelleen) also drops its entry; Kumoa brings it back.
- Imports can add their own fields to the archive (used by okok-xls-import).
- Nothing new is shown in the app.

## Capabilities

### New Capabilities

### Modified Capabilities
- `scale-weigh-in`: new requirement to keep the full raw scale reading.
- `weight-export`: file content gets the `raw` column.

## Impact

Scale weigh-in save path, a small local store in app storage, the CSV export/backup writer and the
history import parser. No new permission, no UI change. Old exports without the column still import.
