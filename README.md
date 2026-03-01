<h1 align="center">AVERTOX-TOWNS</h1>
<p align="center">
  <b>Advanced City and Village Territory System for Spigot/Paper</b><br>
  Developed by <b>TrulyKing03</b> - All Rights Reserved
</p>

<p align="center">
  <img src="https://img.shields.io/badge/version-1.0.0-brightgreen.svg?style=for-the-badge" alt="Version"/>
  <img src="https://img.shields.io/badge/API-Spigot%20%7C%20Paper-blue?style=for-the-badge&logo=minecraft" alt="Server API"/>
  <img src="https://img.shields.io/badge/Language-Java%2017-orange?style=for-the-badge&logo=openjdk" alt="Java 17"/>
  <img src="https://img.shields.io/badge/Database-MySQL-informational?style=for-the-badge" alt="MySQL"/>
  <img src="https://img.shields.io/badge/Status-Active-success?style=for-the-badge" alt="Project Status"/>
  <img src="https://img.shields.io/badge/License-Private-red?style=for-the-badge" alt="License"/>
</p>

---

## Overview

**AVERTOX-TOWNS** is a full city and village territory framework with player applications, admin approval workflow, role-based automation points, territory-only boosts, and owner revenue generation.

Players apply to create a city or village, mark a claim area with in-world corners, submit for review, and unlock territory mechanics only after approval.

---

## Feature Showcase

| Type | Highlights |
|------|-------------|
| Territory Claims | Players apply, mark corners, and submit city/village territory for approval |
| Approval Workflow | Every creation request enters admin dashboard for approve/reject |
| Territory Effects | Auto crop regrowth, faster fishing, and movement boost inside approved territory |
| Role Boosts | Farmer, fisherman, woodcutter, and miner boosts apply only in territory |
| Revenue | Cities and villages generate owner revenue over time |
| Automation | `/set city <role>` binds automation block location by role |
| Sell Integration | Sell handler and automation menu both accessible through role block |
| Notifications | Enter/leave messages for city and village borders |
| Persistence | City borders, role points, and automation data can be stored in MySQL |

---

## Requirements

- Java 17
- Maven 3.8+
- Spigot/Paper server
- MySQL 8+ (if persistent storage is enabled)
- Vault plus economy provider (if revenue payouts are enabled)

---

## Build

```bash
mvn clean package
```

Output jar:
- `target/AVERTOX-TOWNS-1.0.0.jar`

---

## Installation

1. Put the jar in your server `plugins/` folder.
2. Start server once to generate plugin config files.
3. Configure MySQL and economy settings if used.
4. Restart server.
5. Verify admin dashboard access for city/village approval queue.

---

## Configuration

Main config source: `src/main/resources/config.yml`  
Runtime copy: `plugins/AVERTOX-TOWNS/config.yml`

Config includes:
- City and village application settings
- Corner-marking tool settings for `/create city <name>`
- Territory effect toggles and boost values
- Role-specific boosts (`farmer`, `fisherman`, `woodcutter`, `miner`)
- Revenue generation rates for city and village owners
- Enter/leave notification messages
- Automation block behavior and menu integration
- Sell handler integration at automation block locations
- MySQL connection settings and autosave interval

---

## Commands

- `/create city <name>`
  - Starts a city/village creation application and gives a stick to mark territory corners.

- `/create city finish`
  - Submits marked area to admin dashboard for approval or rejection.

- `/set city farmer`
- `/set city fisherman`
- `/set city woodcutter`
- `/set city miner`
  - Sets the role point where that role automation block is placed.

- `/townadmin`
  - Opens the admin dashboard (requires `avertoxtowns.admin` permission).

- `Admin dashboard actions`
  - Review pending applications, approve territories, reject territories.

---

## Admin GUI Controls

- View pending city/village applications
- Inspect claimed corner markers and total area
- Approve application and activate territory effects
- Reject application with reason
- View owner and role assignment points
- Manage territory revenue settings
- Remove or suspend city/village status

Permission:
- `avertoxtowns.admin` (default: op)

---

## Active Territory Rules

- Only approved city/village territories activate town mechanics
- Entering territory shows: `Entering city <name>`
- Leaving territory shows: `Leaving city <name>`
- Resource and break boosts only apply inside city/village territory
- Cities and villages generate revenue for owners while active

---

## Bound Role Progression

Each city role is tied to a set point inside territory using `/set city <role>`.

Role behavior rules:
- Role points must be inside approved territory
- Each role point becomes that role automation block location
- Role automation and sell handling are not available without a valid role point
- Reassigning role point moves access to the new location

---

## Automation Blocks

Automation blocks are bound to role set points:
- `/set city farmer`
- `/set city fisherman`
- `/set city woodcutter`
- `/set city miner`

Automation rules:
- Block is placed at the role set location
- Opening that block gives access to automation menu
- Sell handler must be accessible from the same block
- Automation behavior is valid only when role belongs to an approved city/village

---

## Data Storage (MySQL)

Suggested tables:

- `towns_table`
  - `id`, `owner_uuid`, `type`, `name`, `status`, `created_at`
- `town_claims`
  - `town_id`, `world`, `x1`, `y1`, `z1`, `x2`, `y2`, `z2`
- `town_roles`
  - `town_id`, `role`, `world`, `x`, `y`, `z`
- `town_revenue`
  - `town_id`, `balance`, `last_payout`
- `town_automation`
  - `town_id`, `role`, `block_location`, `stored_items`

Data lifecycle:
- Load on startup
- Load on player join when needed
- Save on change
- Autosave on interval
- Save on shutdown

---

## City and Village Progression Highlights

### Farmer
- Automatic crop regrowth is enabled only inside approved city/village territory
- Farming boosts apply only while player is inside territory
- Farmer automation point is set with `/set city farmer`

### Fisherman
- Fishing is faster only inside approved city/village territory
- Fisherman boost does not apply outside territory
- Fisherman automation point is set with `/set city fisherman`

### Woodcutter
- Woodcutting break boost applies only inside approved territory
- Woodcutter automation point is set with `/set city woodcutter`

### Miner
- Mining break boost applies only inside approved territory
- Miner automation point is set with `/set city miner`

---

## Anti-Exploit

- Prevent non-approved applications from granting territory effects
- Validate that role points are inside claimed territory bounds
- Deny automation and sell access from unlinked blocks
- Enforce territory-only boosts to avoid global buff abuse
- Validate ownership and admin approval before activation
- Prevent duplicate city/village names (case-insensitive)

---

## Territory Effects

Inside approved city/village territory:
- Crops regrow automatically
- Fishing is faster
- Players move slightly faster
- Role boosts for farmer, fisherman, woodcutter, miner are active

Outside territory:
- Crops do not regrow automatically
- Territory boosts are inactive

---

## Understanding the Plugin (Player FAQ)

### How do I create a city or village?
Run `/create city <name>`, mark corners with the provided stick, then run `/create city finish`.

### Does creation activate immediately?
No. The request goes to admin dashboard and must be approved first.

### Can two towns use the same name?
No. City/village names are unique (case-insensitive) while pending/approved.

### Where do I set role automation blocks?
Use `/set city farmer`, `/set city fisherman`, `/set city woodcutter`, `/set city miner` inside your approved territory.

### How do I know when I enter a city or village?
You receive `Entering city <name>` on entry and `Leaving city <name>` on exit.

### Do boosts work everywhere?
No. Resource boosts and break boosts are territory-only.

### Can I use crop regrowth outside city territory?
No. Auto crop regrowth is disabled outside city/village territory.

### Where do I open automation menu and sell handler?
From the automation block placed at the role set location.

---

## How Systems Interact (Big Picture)

1. Player starts application using `/create city <name>`
2. Player marks territory corners with provided stick
3. Player submits request using `/create city finish`
4. Admin approves or rejects through dashboard
5. Approved territory starts entry/exit notifications and boosts
6. Owner sets role points with `/set city <role>` commands
7. Role automation blocks provide automation menu and sell handler
8. City or village generates revenue for owner while active

---

## Progression Deep Dive

### 1) Application Loop
Create request, mark claim, submit, wait for admin decision.

### 2) Territory Activation Model
Approval state controls whether territory mechanics are active or inactive.

### 3) Role Binding Model
`/set city <role>` captures an exact in-world block point per role.

### 4) Automation Access Model
Role-linked automation block is the gateway for automation menu and sell handler.

### 5) Territory Effects Model
Boosts and regrowth are attached to territory boundary checks, not global player state.

### 6) Revenue Model
Approved city/village entities can generate owner revenue based on configured schedule.

### 7) Protection Layer
Validation checks enforce ownership, approved status, in-bounds role points, and effect scope.

---

## Notes

- Designed for territory-first server progression
- Suitable for economy servers using owner revenue systems
- Intended for strict inside/outside territory behavior consistency

---

## Developer and Rights

Developed by **TrulyKing03**  
All rights reserved.  
Email: **TrulyKingDevs@gmail.com**

---

<p align="center">
  <sub><b>AVERTOX-TOWNS</b> - Designed and developed by TrulyKing03</sub>
</p>
