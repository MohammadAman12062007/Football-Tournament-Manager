# Football Tournament Manager

A JavaFX desktop application built for **Object Oriented Programming (CSE202)** at **NMAM Institute of Technology, Nitte**. It automates organizing and tracking round-robin football leagues—including team and squad management, fixture generation, live league standings, and top-scorer analytics.

---

## Features

* **Teams & Players:** Add or delete teams, manage squads (up to 15 players per team), assign jersey numbers (`1–99`) and positions (`Striker`, `Midfielder`, `Defender`, `Goalkeeper`), and record player goals.
* **Fixtures & Results:** Automatically generate a full round-robin schedule with one click, enter or edit match scores using spinners, and visually track completed matches (highlighted in green).
* **Live Standings:** Automatically recalculates the league table (`P`, `W`, `D`, `L`, `GF`, `GA`, `GD`, `Pts`) using standard 3-1-0 scoring and tie-breakers (`Points → Goal Difference → Goals For → Team Name`), with the league leader highlighted in yellow.
* **Top Scorers:** Displays a ranked top-10 leaderboard alongside a live JavaFX `BarChart` of the top 5 goal scorers.
* **Persistent Storage:** Automatically saves and loads all teams, players, and match results in a local `tournament.txt` file (seeds 4 sample teams on first launch).
* **Input Validation:** Prevents invalid operations (empty names, duplicate teams, duplicate jersey numbers, full squads) and reports them through JavaFX `Alert` dialogs.

---

## Project Structure

| File | Layer | Description |
| :--- | :--- | :--- |
| `MainApp.java` | Frontend (UI) | Four-tab JavaFX GUI, menu bar, event handlers, and live view synchronization. |
| `TournamentService.java` | Backend (Logic) | Tournament rules, round-robin scheduler, standings recalculation, Streams queries, and file I/O. |
| `TournamentException.java` | Backend (Exception) | Custom unchecked exception thrown on rule violations. |
| `Storable.java` | Model (Interface) | Serialization contract (`toLine()`) implemented by all domain models. |
| `Team.java` | Model | Stores team name, squad roster, and recalculated match statistics. |
| `Player.java` | Model (Abstract) | Abstract base class for players with a static factory method (`Player.create()`). |
| `Positions.java` | Model (Subclasses) | Concrete subclasses (`Striker`, `Midfielder`, `Defender`, `Goalkeeper`) extending `Player`. |
| `Match.java` | Model | Represents a fixture between two teams, its scoreline, and played status. |

---

## OOP Concepts Demonstrated

* **Encapsulation:** Private fields with controlled getters and unmodifiable squad lists.
* **Abstraction & Interfaces:** Abstract `Player` class and `Storable` persistence interface.
* **Inheritance & Polymorphism:** Position subclasses extending `Player` and overriding `getPosition()` / `describe()`.
* **Factory Method Pattern:** `Player.create()` instantiates the matching position subclass.
* **Custom Exception Handling:** `TournamentException` handled centrally in the UI to show clean error alerts.
* **Collections, Generics & Streams:** Chained `Comparator` sorting for standings, `flatMap()` for top scorers, and generic `TableView` column helpers.

---

## Prerequisites

* **JDK 17 or later** (tested on JDK 27)
* **JavaFX SDK** matching your JDK version (download from [openjfx.io](https://openjfx.io/))

---

## How to Compile and Run

1. Clone or download this repository and open a terminal inside the project folder.
2. Compile all Java files (replace the path with your local JavaFX SDK `lib` folder):
   ```bash
   javac --module-path "/path/to/javafx-sdk/lib" --add-modules javafx.controls,javafx.fxml *.java
   ```
3. Run the application:
   ```bash
   java --module-path "/path/to/javafx-sdk/lib" --add-modules javafx.controls,javafx.fxml MainApp
   ```

> **Windows example:**
> ```cmd
> javac --module-path "C:\javafx-sdk-27.0.1.1\lib" --add-modules javafx.controls,javafx.fxml *.java
> java --module-path "C:\javafx-sdk-27.0.1.1\lib" --add-modules javafx.controls,javafx.fxml MainApp
> ```

---

## Author

* **Mohammad Aman P K** (`NN25AIM071`)
* Department of Artificial Intelligence & Machine Learning, NMAMIT, Nitte
* **Guided by:** Ms. Sneha Shetty R, Assistant Professor