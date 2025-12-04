
<img width="1080" height="720" alt="BannerTreasureHuntFinal" src="https://github.com/user-attachments/assets/23e69c68-bf3c-49d1-83fc-d85b72820651" />

#  Echoes of Gold v1.1
**Echoes of Gold** is a fully customizable treasure-hunting minigame plugin designed to bring exploration and adventure to your Minecraft server.  
Players search for hidden treasures, collect rewards, and compete for the top spot on the leaderboard!

---

## ‼️New Release!
Check out the **new release**, v1.1, [here](https://github.com/ItsAndrew1/TreasureHuntPlugin/releases/tag/v1.1)!

---

## ⚙️ Features
- 💎 Fully configurable treasures, via interactive GUIs
- 📜 Custom hints with clues or lore (via `books.yml`)  
- 🧭 Customizable particles
- 🧮 Optional dynamic top 3 leaderboard (auto-updating)  
- ⏱️ Optional boss bar timer for event duration  
- 🧰 Simple setup and clean configuration files

---

## 🪄 Commands
| Command                             | Description | Permission |
|-------------------------------------|--------------|-------------|
| `/treasurehunt enable`              | Starts the event | `treasurehunt.admin` |
| `/treasurehunt disable`             | Stops the event | `treasurehunt.admin` |
| `/treasurehunt reload`              | Reloads the event | `treasurehunt.admin` |
| `/treasurehunt treasures` | Opens the  treasure manager | `treasurehunt.admin` |
| `/treasurehunt hints create <name>` | Creates the book (for hints) | `treasurehunt.admin` |
| `/treasurehunt hints delete <name>` | Deletes the desired book | `treasurehunt.admin` |
| `/treasurehunt hints manage <name> ...` | Helps managing all hints | `treasurehunt.admin` |
| `/treasurehunt help`                | Opens the help menu | `treasurehunt.admin` |
| `/hints`                            | Opens the hints GUI | `treasurehunt.use` |

---

## 📁 Configuration Files

### `config.yml`
Contains general settings for sounds, GUIs, scoreboard, boss bar and much more.

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
4. Configure all the *.yml* files to your liking.
5. Run `/treasurehunt enable` and let the hunt begin!

---

## ❤️ Credits
Developed and tested by **\_ItsAndrew_**  
Special thanks to everyone who help, test and give feedback!  
My discord: *\_ItsAndrew_*
