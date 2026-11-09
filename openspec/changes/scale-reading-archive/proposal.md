# Proposal

## Why

The scale sends more than the weight: the impedance and the raw packet. Today only the weight goes to
Health Connect and the rest is in the log, which rotates away. The user wants everything the scale
sends kept (user 2026-11-09), so that body composition or other uses stay possible later without
having lost the history. Health Connect has no record for raw impedance.

## What Changes

- Each saved scale weigh-in also stores its full reading on the phone: time, weight, impedance, the
  scale's unit and the raw bytes of the deciding packet.
- The export and the automatic backup file get two more columns, impedance and raw packet, filled for
  scale weigh-ins, so the data leaves the phone with the backup.
- Tuo historia reads those columns back into the phone's store (moving to a new phone keeps them).
- Deleting a scale weight (list, Punnitse uudelleen) also drops its reading; Kumoa brings it back.
- Nothing is shown in the app yet; body composition stays out (Q4).

## Capabilities

### New Capabilities

### Modified Capabilities
- `scale-weigh-in`: new requirement to keep the full scale reading.
- `weight-export`: file content gets the impedance and raw packet columns.

## Impact

Scale weigh-in save path, a small local store in app storage, the CSV export/backup writer and the
history import parser. No new permission, no UI change. Old exports without the columns still
import.
