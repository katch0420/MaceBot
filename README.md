# MaceBot

Turn any world into a mace combat training ground. MaceBot spawns a fully customizable PvP bot that blocks, dodges, crits, and fights back with real timing — built specifically to help you sharpen your mace combat: aim, spacing, stun slams, pearl macing, all of it.

Dial the bot's difficulty up or down and pick from focused practice modes instead of just a straight fight. Everything is controllable entirely through commands, so you don't need any client-side mod to use it — but installing on client unlocks a full GUI with live combat hitbox overlays, a theme editor, a proper kit editor for players who want the extra convenience and more flexible controls.

---

## 🚀 What's New in v1.3

- **Full command control** — every setting, kit, and bot action can now be managed through commands. The client-side mod is no longer required to use MaceBot.
- **Difficulty levels** — Noob, Pro, and Master, adjusting the bot's reach, accuracy, and available actions.
- **Practice Modes** — NPC, Fight, and a dedicated Practice mode with Stun Slam, Aim, and Pearl Catch sub-modes.
- **Kit management via commands** — create, edit, duplicate, and delete kits without touching the GUI.
- **Custom Name** — spawn the bot with any name; if it matches a real account, it uses that player's real skin.
- **Redesigned client GUI** — reworked into a single taskbar-style screen with a new Theme Editor for full color customization.
- **Combat Hitbox overlay** — see your target's hitbox rendered in a customizable color.
- **Safer defaults** — buffs and auto-refill are now off by default, and everything requires operator permissions to prevent accidental use in survival worlds.

---

<details>
  <summary>✨ Features</summary>

### Server Side

- **Combat AI**
  - Performs mace attacks, crits, and elytra mace strikes.
  - Uses totems and heals itself when low.

- **Difficulty**
  - Three difficulty levels: **Noob**, **Pro**, and **Master**.
  - Adjusts the bot's reach and accuracy (how often it misses), and restricts certain actions — like elytra macing — depending on the level chosen.

- **Modes**
  - **NPC** — A stationary punching bag for basic combat practice.
  - **Fight** — The full simulated bot that fights back.
  - **Practice** — Three focused training sub-modes:
    - **Stun Slam** — Hold your shield to practice landing stun slams.
    - **Aim** — The bot floats in mid-air in a flying state, giving you a smaller hitbox to practice your aim against.
    - **Pearl Catch** — The bot uses wind charges to repeatedly launch itself around 6 blocks into the air, letting you practice pearling and macing timing.

- **Equipment Management**
  - Automatic inventory refill.
  - Supports elytra flight and customizable kits.

- **Kits**
  - Create, delete, and view kits entirely through commands.
  - Build a new kit from scratch, or generate one directly from your current inventory.
  - There's no in-game kit editor on the server side, so if you don't have the client-side mod installed, you can still edit a kit: equip it, adjust the items in your regular inventory, then run `/macebot kits <kit> copyInv` to save those changes back to the kit.

- **Bot Identity**
  - Spawn the bot with a custom name. If that name matches a real Minecraft account, the bot will use that account's actual skin — for example, spawning the bot as "Wemmbu" gives it the name and skin of the player Wemmbu.

- **Customization**
  - Toggle crits, ordinary mace attacks, elytra usage, and more.
  - Control whether the bot can damage you.
  - Full command-based control over every MaceBot setting — see Commands below.

- **Safety Defaults**
  - Buffs and auto-refill are **disabled by default** to prevent accidental use in regular survival worlds.
  - All commands require **Level 3 operator permissions** for the same reason.

### Client Side

The client-side mod is entirely optional — MaceBot works fully through commands without it — but it adds a complete GUI for players who'd rather not type. Open it with the default keybind **`P`**.

- **Taskbar GUI** — Settings, Control Panel, Kits, and Info all live in one unified, taskbar-style screen instead of separate windows, with full flexible control over everything — well beyond what commands alone offer.
- **Control Panel** — Start, pause, spawn, or despawn the bot, and rename it on the fly through a Name Field. If the name matches a real Minecraft account, MaceBot fetches and displays that player's real skin.
- **Theme Editor** — Customize the colors used throughout the GUI, including the Combat Hitbox color, from the Settings screen.
- **Kits Screen & Kit Menu** — Browse all built-in and custom kits, then rename (with color codes), delete, duplicate, edit, or load them.
- **Kit Loader Menu** — Load a kit onto MaceBot, yourself, or all players, with a choice of Unbreaking III & Mending or an Unbreakable tag (breakable items only).
- **Kit Editor** — A flexible editor covering item names (with color codes), enchantments, attributes, item count, any creative-menu item, and a built-in NBT editor for fine-grained control. Includes a one-click Copy Inventory button that copies your current inventory straight into the kit. Built-in kits can't be edited directly, but you can duplicate and customize them.
- **Kit Viewer** — Preview kits before loading them.
- **Combat Hitboxes** — Highlights your current target's hitbox in a customizable color (default: red) to help with spacing and aim practice.
- **Hotkeys** — Quickly open GUI screens, toggle the client mod, or toggle Combat Hitboxes without opening a menu.
- **Safety Restrictions** — The client mod restricts certain actions without operator permissions by default; operators can disable these restrictions in settings if they want non-op players to use the mod freely.

</details>

---

<details>
  <summary>⚙️ Commands</summary>

All commands are grouped under **`/macebot`** and require **Level 3 operator permissions**.

<details>
<summary><code>/macebot controls</code> — start, stop, and manage the bot</summary>

| Command | Description |
|---|---|
| `/macebot controls spawn` | Spawns the bot. |
| `/macebot controls kick` | Removes the bot. |
| `/macebot controls play` | Resumes the bot. |
| `/macebot controls stop` | Pauses the bot. |
| `/macebot controls setName <name>` | Renames the bot. If `<name>` matches a real Minecraft account, the bot will also use that account's skin. |

</details>

<details>
<summary><code>/macebot settings</code> — configure MaceBot, player, and mod options</summary>

Settings are split into three categories. Each parent command exposes every editable setting in that category as a sub-command — booleans toggle directly, and dropdown-style settings (like Difficulty or Mode) suggest their valid options in chat.

| Command | Description |
|---|---|
| `/macebot settings macebot` | All editable bot-specific settings. |
| `/macebot settings player` | All editable player-specific settings. |
| `/macebot settings mod` | All editable global mod settings. |

</details>

<details>
<summary><code>/macebot kits</code> — create, edit, and manage kits</summary>

| Command | Description |
|---|---|
| `/macebot kits new empty <name>` | Creates a new, empty custom kit. |
| `/macebot kits new fromInv <name>` | Creates a new custom kit using your current inventory. |
| `/macebot kits <kit> load <target>` | Loads a kit onto `MACEBOT`, `PLAYER` (yourself), or `ALL_PLAYERS`, with Unbreaking III & Mending applied by default. |
| `/macebot kits <kit> load <target> unbreakable` | Loads the kit with an Unbreakable tag instead. |
| `/macebot kits <kit> load <target> unbreakingAndMending` | Loads the kit with Unbreaking III and Mending. |
| `/macebot kits <kit> load <target> unbreakingOnly` | Loads the kit with Unbreaking III only. |
| `/macebot kits <kit> load <target> mendingOnly` | Loads the kit with Mending only. |
| `/macebot kits <kit> duplicate` | Duplicates an existing kit, including built-in ones, as a new custom kit. |
| `/macebot kits <kit> info` | Shows basic info about a kit (name, ID, whether it's custom). |
| `/macebot kits <kit> info items` | Shows the same info plus a full item list for the kit. |
| `/macebot kits <kit> copyInv` | Overwrites a custom kit with your current inventory. This is the server-side workaround for editing a kit — equip/hold what you want, then run this to save it — since there's no in-game kit editor without the client mod. |
| `/macebot kits <kit> delete` | Deletes a custom kit. Built-in kits cannot be deleted. |

</details>

**Note:** Commands give you full control on their own — no client mod required. If you do have the client mod installed, the GUI wraps all of this in a more visual, point-and-click experience.
</details>

---

<details>
  <summary>📥 Installation</summary>

**Server**
- Place the correct version of MaceBot in the `mods` folder and restart the server.
- Access MaceBot using the commands listed above (requires Level 3 operator permissions).
- The client-side mod is optional — install it too if you want the full GUI experience.

**Client**
- Place the correct version of MaceBot in the `mods` folder and restart the client.
- In singleplayer, you can use both commands and the GUI to control MaceBot.
- On dedicated servers, you can also control MaceBot via the GUI.  
  *(Be mindful of version compatibility.)*
</details>

---

## ✅ Compatibility
- Compatible with Carpet and its forks (Carpet PvP) (since v1.2.1).

---

<details>
  <summary>📜 Version History</summary>

- **v1.0** — Initial release. Server-side only, command-controlled.
- **v1.1** — Server-side only, follow-up release with additional fixes and refinements.
- **v1.2** — Added the first client-side GUI for easier control.
- **v1.3** — Full command-based control, Difficulty levels, Modes, bot identity/skins, a redesigned taskbar GUI with a Theme Editor, Combat Hitbox overlay, and safer defaults. See "What's New in v1.3" above.

</details>

---

## 📝 Conclusion
MaceBot is our first major mod project. It may have bugs or incompatibilities, but we're committed to improving it. Please share feedback, suggestions, and ideas for new features — your input helps shape the future of MaceBot.

---

<div align="center">
  <p><strong>Available exclusively for the Fabric Mod Loader ecosystem.</strong></p>
  <a href="https://github.com/katch0420/MaceBot"><img src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/github_vector.svg" height="36"></a>
  <a href="#"><img src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/built-with/gradle_vector.svg" height="36"></a>
  <a href="#"><img src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/requires/fabric-api_vector.svg" height="36"></a>
  <a href="https://modrinth.com/mod/macebot/"><img src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/fabric_vector.svg" height="36"></a>
</div>
