#  Echoes of Gold
**Echoes of Gold** is a fully customizable treasure-hunting minigame plugin designed to bring exploration and adventure to your Minecraft server.  
Players search for hidden treasures, collect rewards, and compete for the top spot on the leaderboard!

---

## ⚙️ Features
- 💎 Fully configurable treasure locations  
- ⚔️ Reward tiers based on treasures found  
- 📜 Custom books with clues or lore (via `books.yml`)  
- 🧭 Real-time particle effects guiding players  
- 🧮 Dynamic top 3 leaderboard (auto-updating)  
- ⏱️ Optional boss bar timer for event duration  
- 🧰 Simple setup and clean configuration files

---

## 🪄 Commands
| Command | Description | Permission |
|----------|--------------|-------------|
| `/treasurehunt enable` | Starts the event | `treasurehunt.admin` |
| `/treasurehunt disable` | Stops the event | `treasurehunt.admin` |
| `/treasurehunt reload` | Reloads the event | `treasurehunt.admin` |
| `/treasurehunt books create <name>` | Creates the book (for hints) | `treasurehunt.admin` |
| `/treasurehunt books delete <name>` | Deletes the desired book | `treasurehunt.admin` |
| `/treasurehunt books list` | Displays the list of all the books  | `treasurehunt.admin` |
| `/treasurehunt help` | Opens the help menu | `treasurehunt.admin` |
| `/hints` | Opens the hints GUI | `treasurehunt.use` |

---

## 📁 Configuration Files

### `config.yml`
Contains general settings like messages, scoreboard titles, sound effects, and boss bar settings.

### `treasures.yml`
Defines all treasure chest locations, including world, coordinates, and identifiers.

### `books.yml`
Stores the in-game hint books that players can unlock as they progress.

### `playerdata.yml`
Tracks player progress and the treasures they’ve discovered.

---

## 🧱 Installation
1. Download the latest release from [GitHub Releases](#).  
2. Drop the `.jar` file into your server’s `plugins` folder.  
3. Start your server to generate configuration files.  
4. Configure treasures and books to your liking.  
5. Run `/treasurehunt enable` and let the hunt begin!

---

## ❤️ Credits
Developed by **_ItsAndrew_**  
Special thanks to everyone who help, test and give feedback!
My discord: _itsandrew_