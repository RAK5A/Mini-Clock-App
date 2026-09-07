# Clock App

A Jetpack Compose Android clock app featuring Alarm, World Clock, Stopwatch, and Timer tabs.

## Getting Started

**1. Clone the repository:**
```bash
git clone [https://github.com/RAK5A/Mini-Clock-App.git](https://github.com/RAK5A/Mini-Clock-App.git)
cd Mini-Clock-App
```
**2. Open the project in Android Studio and run a Gradle sync.**
### Branch Workflow
**We do not commit directly to main. Every change goes through its own feature branch and a pull request.**

**3. Ensure your local main branch is up to date:**
```bash
git checkout main
git pull origin main
```

**4. Create your branch. Name it after yourself (lowercase, words joined by hyphens; e.g., `Chea Reaksa` becomes `chea-reaksa`):**
```bash
git checkout -b chea-reaksa
```

**5. Stage your changes:**

Staging specific files is recommended to prevent unintended commits:
```bash
git add <path-to-modified-file>
```
Or stage all modified files at once:
```bash
git add .
```

**6. Commit your staged changes using clear commit messages:**
```bash
git commit -m "feat(alarm): add alarm screen scaffold"
```
**Recommended commit conventions:**

- Feature: `feat(alarm): add ability to set recurring alarms`

- Bug Fix: `fix(worldclock): correct timezone offset calculation`

- Refactoring: `refactor(stopwatch): use StateFlow instead of LiveData`

- Documentation: `docs: add setup instructions for new developers`

- Style: `style: apply ktlint formatting across all files`

- Performance: `perf(timer): reduce UI recompositions by 40%`

**7. Push to the GitHub repository:**

For the first push of a new branch:
```bash
git push -u origin chea-reaksa
```
For subsequent pushes:
```bash
git push
```

**8. If main has moved forward while you were working, sync before pushing:**
```bash
git checkout main
git pull origin main
git checkout chea-reaksa
git merge main
```

**9. Open a Pull Request (PR) on GitHub from your branch into `main`, and obtain a code review before merging.**

### Project Structure
```text
com.sda5.clockapp/
├── alarm/                     # Alarm scheduling, notifications, background receivers, and UI
│   ├── AlarmActionReceiver.kt
│   ├── AlarmEditScreen.kt
│   ├── AlarmReceiver.kt
│   ├── AlarmRingingService.kt
│   ├── AlarmScheduler.kt
│   ├── AlarmScreen.kt
│   ├── AlarmTimeUtils.kt
│   ├── AlarmViewModel.kt
│   ├── AlarmViewModelFactory.kt
│   ├── BootReceiver.kt
│   └── NotificationHelper.kt
├── data/                      # Room Database setup, Converters, and DAOs
│   ├── AlarmDao.kt
│   ├── ClockDatabase.kt
│   ├── DayOfWeekSetConverter.kt
│   └── WorldClockDao.kt
├── model/                     # Data entities for Alarms and World Clock Cities
│   ├── Alarm.kt
│   └── WorldClockCity.kt
├── navigation/                # Navigation destinations and routing
│   └── ClockDestination.kt
├── stopwatch/                 # Background stopwatch service, lap history, and UI
│   ├── LapHistoryTable.kt
│   ├── StopwatchScreen.kt
│   ├── StopwatchService.kt
│   ├── StopwatchState.kt
│   ├── StopwatchTimeUtils.kt
│   └── StopwatchViewModel.kt
├── timer/                     # Timer service, preset configurations, state management, and UI
│   ├── TimerPresets.kt
│   ├── TimerScreen.kt
│   ├── TimerService.kt
│   ├── TimerState.kt
│   └── TimerViewModel.kt
├── ui/                        # Reusable design system components and Material 3 theme
│   ├── components/
│   │   ├── SelectableRow.kt
│   │   ├── StartButton.kt
│   │   └── WheelPicker.kt
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── worldclock/                # World clock city management, timezone utilities, and UI
│   ├── AddCityScreen.kt
│   ├── WorldClockScreen.kt
│   ├── WorldClockTimeUtils.kt
│   ├── WorldClockViewModel.kt
│   └── WorldClockViewModelFactory.kt
├── ClockApp.kt                # Main app Composable container & bottom navigation layout
├── ClockApplication.kt        # Global Application class
└── MainActivity.kt            # Entry point activity
```