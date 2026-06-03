# The Ring Quest: Middle-earth Step RPG 💍

**The Ring Quest** is an immersive fitness adventure that turns your daily steps into an epic journey across Middle-earth. Walk from the peaceful Shire all the way to the fires of Mount Doom, unlocking legendary landmarks and lore along the way.

## ✨ Features

- **Epic Journey**: Track your progress across 80+ iconic Middle-earth milestones.
- **Accurate Tracking**: Uses Android's hardware `TYPE_STEP_COUNTER` for battery-efficient and precise background step counting.
- **Persistent Notification**: A permanent, non-intrusive lock screen notification shows your daily paces and quest progress at a glance.
- **Home Screen Widget**: Stay motivated with a custom widget showing your current location and distance to the next landmark.
- **Lore Unlocked**: Reach new locations to read secret lore and character quotes from the books.
- **Health Metrics**: Track calories burned (measured in Lembas bread slices) and leagues marched.
- **RPG Elements**: Earn ranks as you travel, from "Hobbit Traveler" to "Middle-earth Saviour."

## 🚀 Getting Started

### Prerequisites

- An Android device with a step counter sensor.
- [Android Studio](https://developer.android.com/studio) (to build from source).

### Installation

1.  **Clone the repository**:
    ```bash
    git clone https://github.com/yourusername/ring-quest.git
    ```
2.  **Open in Android Studio**:
    - Select **Open** and navigate to the project folder.
3.  **Permissions**:
    - Upon first launch, allow **Physical Activity** (to count steps) and **Notifications** (for the background tracker).
4.  **Start Walking**:
    - Your journey begins at Bag End. May the light of Eärendil guide you!

## 🛠️ Technical Details

- **Language**: 100% Kotlin
- **UI**: Jetpack Compose (Modern, declarative UI)
- **Database**: Room (Persistent storage for step logs)
- **Background**: Foreground Service with Health type support (Android 14+ compliant)
- **Sensor Logic**: Robust baseline calculation that survives device reboots and app restarts.

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

*“It’s a dangerous business, Frodo, going out your door. You step onto the road, and if you don’t keep your feet, there’s no knowing where you might be swept off to.”*
