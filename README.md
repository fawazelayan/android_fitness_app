# Scoop Tracker 🍦💧🥗

A premium, modern Jetpack Compose Android application designed to track your daily nutrition scoop counts, water hydration, food macros, and wellness targets. Built with Kotlin, Material Design 3, Room Database, and optimized for butter-smooth scrolling.

---

## 🌟 Key Features

### 👤 Multi-Profile Support
- **Quick Switching**: Switch between two active profiles seamlessly.
- **Custom Renaming**: Fully rename profiles to suit your needs, persisting all related data separately.

### 🍦 Scoop Tracker
- **Interactive Dial Selector**: Smooth, circular gesture-based dial to easily set and log your daily scoop intakes.
- **Database Buffering**: Performance-optimized drag gestures that update the UI instantly and write to the database only on release.
- **Calendar & History**: Quick visual logs of your scoop intakes over the past week/month.
- **Reminders**: Schedule local reminders with custom sound alerts to make sure you never miss your scoop times.

### 💧 Water & Hydration Tracker
- **Visual Hydration Progress**: Beautiful UI showing target progress with quick-tap logging presets.
- **Water Logs History**: Track exactly when and how much water you've consumed throughout the day.

### 🥗 Nutrition & Macro Tracker
- **Macro Breakdowns**: Real-time progress indicators for Calories, Protein, Carbs, and Fats.
- **Interactive Circle Indicators**: Clean visual arcs representing macro targets.
- **AI Food Logger (Mock)**: A mock interface demonstrating AI-assisted food logging.
- **Manual Food Logger**: Add individual foods with specific calorie and macro values.

### ⚙️ Theme & Settings
- **Smooth Dark Mode**: Seamless toggle between Dark and Light themes with consistent custom color palettes.
- **Profile Settings**: Easily edit body parameters like current weight.

---

## 🛠️ Architecture & Tech Stack

- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (100% declarative UI)
- **Design System**: Material Design 3 (custom color schemes for dark/light modes)
- **Language**: [Kotlin](https://kotlinlang.org/)
- **Architecture Pattern**: MVVM (Model-View-ViewModel)
- **Asynchronous Flow**: Kotlin Coroutines & StateFlow/SharedFlow
- **Local Persistence**: Room Database (with custom DAO queries, migrations, and relationships)
- **Reminders**: Android AlarmManager, Broadcast Receivers, and local Notification services

---

## 🚀 Getting Started

### Prerequisites
- [Android Studio](https://developer.android.com/studio) (Koala / Ladybug or newer recommended)
- JDK 17 or higher
- Android SDK (API 34+)

### Cloning and Building
1. Open Android Studio.
2. Select **File -> New -> Import Project...** (or select **Open** from the welcome screen) and navigate to the project directory.
3. Wait for the Gradle sync to complete successfully.
4. Run the project:
   - Select your target device (Emulator or USB Debugging physical device).
   - Press **Run (Shift + F10)**.

### Running via Command Line
To build the debug APK from your terminal:
```bash
# On Windows (PowerShell/CMD)
.\gradlew.bat assembleDebug

# On macOS/Linux
./gradlew assembleDebug
```
The built APK will be located under `app/build/outputs/apk/debug/app-debug.apk`.

---

## ⚡ Performance Optimizations Included
- **Recomposition Control**: Stable wrappers and keys assigned to list items in `LazyColumn` for minimum recompositions during fast scrolling.
- **Deferred Database Writes**: Circular gesture dial inputs are throttled/buffered to avoid database and shared preferences write bottlenecks during high-frequency dragging.
- **Optimized Assets**: Lightweight vector graphics and clean layout structures for minimal memory footprints.
