
<img width="1080" height="720" alt="BannerTreasureHuntFinal" src="https://github.com/user-attachments/assets/23e69c68-bf3c-49d1-83fc-d85b72820651" />

<p align = "center">
  <img src="https://img.shields.io/badge/Plugin%20Version-1.2.1-blue?style=for-the-badge"><img src="https://img.shields.io/badge/Minecraft%20Version-1.20%2B-green?style=for-the-badge"><img src="https://img.shields.io/badge/License-MIT-purple?style=for-the-badge"><br>
</p>

**Echoes of Gold** is a fully customizable treasure-hunting minigame plugin designed to bring exploration and adventure to your Minecraft server.  
Players search for hidden treasures, collect rewards, and compete for the top spot on the leaderboard!

---

## ‼️New Release!
Check out the **new release**, v1.2.1, [here](https://github.com/ItsAndrew1/TreasureHuntPlugin/releases/tag/v1.2.1)!

---

## ⚙️ Features
- 💎 Fully configurable treasures, via interactive GUIs
- ✨ Fully configurable particles
- 📜 Custom hints with clues or lore (via `books.yml`)  
- 🧭 Customizable particles
- 🧮 Optional dynamic top 3 leaderboard (auto-updating)  
- ⏱️ Optional boss bar timer for event duration  
- 🧰 Simple setup and clean configuration files  
And **much more**!
---

## 🪄 Commands
| Command                             | Description | Permission |
|-------------------------------------|--------------|-------------|
| `/eog enable`              | Starts the event | `eog.admin` |
| `/eog disable`             | Stops the event | `eog.admin` |
| `/eog reload`              | Reloads the event | `eog.admin` |
| `/eog treasures` | Opens the  treasure manager | `eog.admin` |
| `/eog hints create <name>` | Creates the book (for hints) | `eog.admin` |
| `/eog hints delete <name>` | Deletes the desired book | `eog.admin` |
| `/eog hints manage <name> ...` | Helps managing all hints | `eog.admin` |
| `/eog help`                | Opens the help menu | `eog.admin` |
| `/hints`                            | Opens the hints GUI | `eog.use` |

---

## 📁 Configuration Files
**Echoes of Gold** uses 4 different *configuration file*, each with it's own use.

### `config.yml`
Contains *important settings* for the plugin:
- Configuring the **sounds** and their *volume* and *pitch*
- Configuring the scoreboard
- Configuring the boss bar
- Configure the messages  
  And much more!

### `treasures.yml`
Stores all the treasures that you may create. 

### `books.yml`
Stores the in-game hint books that players can unlock as they progress.

### `playerdata.yml`
Tracks player progress and the treasures they’ve discovered.

---

## ❤️ Credits
Developed and tested by **\_ItsAndrew_**  
Special thanks to everyone who help, test and give feedback!  
My discord: *\_ItsAndrew_*
