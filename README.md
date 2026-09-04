# Clock App

A Jetpack Compose Android clock app with Alarm, World Clock, Stopwatch, and Timer tabs.

## Getting started
**1. Clone the repo:**
```bash
git clone https://github.com/RAK5A/Mini-Clock-App.git
```

```bash
cd Mini-Clock-App2
```

**2. Open the folder in Android Studio and let Gradle sync.**

## Branch workflow
We don't commit directly to `main`. Every change goes through its own branch and a pull request.

**3. Make sure main is up to date:**
   
   ```bash
   git checkout main
   ```
   
   ```bash
   git pull origin main
   ```

**4. Make your branch.** Name it after yourself: full name, lowercase, words
joined by hyphens. `Chea Reaksa` becomes `chea-reaksa`.

```bash
git checkout -b chea-reaksa
```


**5. Work, then stage**
   
```bash
git add .
```
I recommend to add the files you did instead to prevent error but `git add .` should be fine too

```bash
git add <modified files>
```
**6. After all you added all your files, commit it**

```bash
git commit -m "Add alarm screen scaffold"
```
**Some `git commit` messages before pushing:**
- New feature
```text
feat(alarm): add ability to set recurring alarms
```

- Bug fix
```text
fix(worldclock): correct timezone offset calculation
```

- Refactoring
```text
refactor(stopwatch): use StateFlow instead of LiveData
```

- Documentation
```text
docs: add setup instructions for new developers
```

- Style fixes
```text
style: apply ktlint formatting across all files
```

- Performance
```text
perf(timer): reduce UI recompositions by 40%
```

**7. Push to GitHub Repository.** The first push needs `-u`, after that `git push` is
enough.

```bash
git push -u origin chea-reaksa
```

```bash
git push
```

**8. If main has moved on while you were working, sync before pushing:**

```bash
git checkout main
```

```bash
git pull origin main
```

```bash
git checkout kimpheng
```

```bash
git merge main
```

**9. Open a pull request on GitHub from your branch into `main`, and get it reviewed before merging.**

## Project structure
- `app/src/main/java/com/reaksa/clockapp/`
  - `MainActivity.kt`
  - `ClockApp.kt`
  - `navigation/`
    - `ClockDestination.kt`
  - `screens/`
    - `AlarmScreen.kt`
    - `WorldClockScreen.kt`
    - `StopwatchScreen.kt`
    - `TimerScreen.kt`
  - `theme/`
    - `Color.kt`
    - `Theme.kt`
    - `Type.kt`
