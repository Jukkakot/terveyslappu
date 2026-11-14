---
name: notify-when-blocked
description: End every turn that waits for the user's decision with a PushNotification naming the decision
metadata:
  type: feedback
---

When a turn ends because the work waits for the user (a question, a decision, a held push), send
a `PushNotification` (load it with ToolSearch) whose message names what is waiting, e.g.
"food-log valmis, odottaa: nollataanko build-numero (1.24.0)?". The user follows from the phone
and does not see that the work stopped.

**Why:** the user, 2026-11-14 ("Taaskaan ei tullut ilmoitusta, vaikka homma ei etene"), after a
turn ended on a question about the version reset with no notification. It had happened before.

**How to apply:** only when the work is blocked on the user; not for routine progress or when the
turn simply finished everything asked.
