<h1 align="center">AVERTOX-TOWNS</h1>
<p align="center">
  <b>City and Village Territory System for Spigot/Paper</b><br>
  Developed by <b>TrulyKing03</b> - All Rights Reserved
</p>

<p align="center">
  <img src="https://img.shields.io/badge/version-1.0.0-brightgreen.svg?style=for-the-badge" alt="Version"/>
  <img src="https://img.shields.io/badge/API-Spigot%20%7C%20Paper-blue?style=for-the-badge&logo=minecraft" alt="Server API"/>
  <img src="https://img.shields.io/badge/Language-Java%2017-orange?style=for-the-badge&logo=openjdk" alt="Java 17"/>
  <img src="https://img.shields.io/badge/Status-Active-success?style=for-the-badge" alt="Project Status"/>
  <img src="https://img.shields.io/badge/License-Private-red?style=for-the-badge" alt="License"/>
</p>

---

## Overview

**AVERTOX-TOWNS** adds player-owned cities and villages with territory-based boosts, role-linked automation placement, and admin approval flow.

Players apply for a city or village, mark territory corners in-world, submit for review, and wait for admin approval from dashboard tools.

---

## Feature Checklist

- [x] Players must apply to create a city or village.
- [x] Resource boosts and break boost (for fisherman, farmer, woodcutter, miner) only apply within city territory.
- [x] Cities and villages generate revenue for their owners.
- [x] Enter/leave territory notifications.
- [x] Territory-only crop regrowth, faster fishing, and movement boost.
- [x] Outside territory: no automatic crop regrowth.
- [x] Automation block tied to `/set city <role>` location.
- [x] Automation menu and sell handler available through the placed automation block.

---

## City and Village Creation Process

1. Player runs `/create city <name>` (or village equivalent if configured).
2. System gives a stick to mark territory corners.
3. Player marks the full area.
4. Player runs `/create city finish` to submit the application.
5. Player sets role points with commands such as:
   - `/set city farmer`
   - `/set city miner`
   - `/set city fisherman`
   - `/set city woodcutter`
6. Submission is sent to admin dashboard queue.
7. Admin approves or rejects the city/village request.

---

## Territory Functionality

### Entry and Exit Notifications

- Entering a city/village: `Entering city <name>`
- Leaving a city/village: `Leaving city <name>`

### Effects Enabled Only Inside City/Village Territory

- Crops regrow automatically.
- Fishing is faster.
- Players move slightly faster.
- Role resource boosts and break boost are active only for:
  - fisherman
  - farmer
  - woodcutter
  - miner

### Effects Outside Territory

- Crops do not regrow automatically.
- Territory role boosts are not active.

---

## Automation Blocks and Role Placement

Automation blocks are placed at the position selected by each role command (for example `/set city farmer`).

Required behavior:
- Each role gets its own automation access point from its set location.
- Right interaction with this block opens automation menu.
- Sell handler must also be accessible through the same block.

---

## Commands

- `/create city <name>`
  - Start city application and receive territory marker stick.

- `/create city finish`
  - Submit marked area for admin approval.

- `/set city farmer`
- `/set city miner`
- `/set city fisherman`
- `/set city woodcutter`
  - Set role automation location inside city territory.

---

## Build

```bash
mvn clean package
```

Expected artifact:
- `target/AVERTOX-TOWNS-1.0.0.jar`

---

## Rights

Developed by **TrulyKing03**  
**All rights reserved.**  
Email: **TrulyKingDevs@gmail.com**

---

<p align="center">
  <sub><b>AVERTOX-TOWNS</b> - Designed and developed by TrulyKing03</sub>
</p>

