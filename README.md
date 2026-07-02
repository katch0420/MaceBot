<div align="center">
  <h1>MaceBot</h1>
  <p>A customizable PvP bot designed specifically for Mace combat practice.</p>
  <a href="https://github.com/katch0420/MaceBot"><img src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/github_vector.svg" height="36"></a>
  <a href="#"><img src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/built-with/gradle_vector.svg" height="36"></a>
  <a href="#"><img src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/requires/fabric-api_vector.svg" height="36"></a>
  <a href="https://modrinth.com/mod/macebot/"><img src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/fabric_vector.svg" height="36"></a>
</div>

---

**MaceBot** is a Fabric mod that introduces a customizable PvP bot designed specifically for **Mace combat practice**. It helps players sharpen their aim, timing, and combat strategies against a bot that fights back with realistic mechanics.

---

## New GUI System!
Since **v1.2.0**, MaceBot includes a **client-side GUI** for easier control, opened with the default keybind **`O`**:

- **Control Panel**  
  Start, pause, spawn, or despawn the bot directly from a menu. Also provides access to other screens.
- **Options Screen**  
  Manage all MaceBot options in one place — no more typing commands.
- **Kits Screen**  
  Browse all kits (built-in and custom). Kits are stored on disk and synced to the client during handshake. From here you can open the Kit Viewer or Kit Menu.
- **Kit Menu**  
  Rename (with color codes), delete, duplicate, edit, and load kits.
- **Kit Loader Menu**  
  Options to load kits:
  - Load to → [MaceBot | Yourself | All players (except MaceBot)]
  - Properties → [Unbreaking III & Mending | Unbreakable Tag]  
    *(Properties apply only to breakable items.)*
- **Kit Editor**  
  A user-friendly editor with buttons and dropdowns. You can edit:
  - Item names (supports color codes like `&a`, `&c`, `&m`)
  - Enchantments
  - Item count
  - Any item from the creative menu  
    *(Built-in kits cannot be edited directly, but you can duplicate and customize them.)*
- **Kit Viewer**  
  Preview kits before loading them.

This GUI makes bot management far more intuitive compared to command-only control.  
See the preview of each screen in the gallery.

---

## Environment
v1.0 and v1.1 were completely server side only. Since the new GUI release of v1.2 it is required on the client side too because of a lack of control without the GUI.
And MaceBot is mainly designed for singleplayer use, but you can use it in dedicated servers and use your client side mod to control it.

---

<details>
  <summary>✨ Features</summary>

- **Combat AI**
  - Performs mace attacks, crits, and elytra mace strikes.
  - Uses totems and heals itself when low.

- **Equipment Management**
  - Automatic inventory refill.
  - Supports elytra flight and customizable kits.

- **Customization**
  - Toggle crits, ordinary mace attacks, elytra usage, and more.
  - Control whether the bot can damage you.
</details>

---

<details>
  <summary>⚙️ Commands</summary>

**/macebot**

- **bot**
  - **spawn** → Spawns the bot.
  - **play** → Resumes the bot.
  - **pause** → Pauses the bot.
  - **mace-kit** → Shows all built‑in kits available for MaceBot.
  - **settings**
    - `elytra` → Toggles elytra usage.  
    - `auto-refill` → Toggles inventory refill.  
    - `attack` → Decides whether the bot can hurt you.  
    - `crits` → Toggles critical attacks.  
    - `ordinary-mace` → Toggles classic mace attacks.

- **player**
  - **mace-kit** → Shows all available kits from the server.
  - **settings**
    - `auto-refill` → Prevents items from decreasing when used.  

---

**Note:** We recommend using MaceBot with the **GUI system**.  
Commands only provide basic controls, while the GUI offers full management options.
</details>

---

<details>
  <summary>📥 Installation</summary>

**Server**
- Place the correct version of MaceBot in the `mods` folder and restart the server.
- Access MaceBot using commands listed above.
- We recommend using MaceBot client-side for more controls and easier access.

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

## 📝 Conclusion
MaceBot is our first major mod project. It may have bugs or incompatibilities, but we’re committed to improving it. Please share feedback, suggestions, and ideas for new features — your input helps shape the future of MaceBot.

---

<div align="center">
  <p>Available exclusively for the Fabric Mod Loader ecosystem.</p>
</div>
