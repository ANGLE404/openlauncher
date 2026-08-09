# Open Launcher 1.0.3 Pixel Release Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deliver Open Launcher 1.0.3 as a fully localized, readable Chinese pixel-style launcher with coherent rounded UI, reliable weather/trip behavior, and a signed GitHub release APK.

**Architecture:** Keep the existing Material 3 palette resolver and DataStore settings model. Centralize the remaining geometry in `ui/theme/Shapes.kt`, route all normal Chinese typography through BoutiqueBitmap9x9, and preserve explicit alternate font choices except for the obsolete Noto Sans SC migration. Keep weather state race-safe with a monotonically increasing request sequence and throttle only successful requests.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, DataStore Preferences, Retrofit, Gradle 9.4.1, Android SDK 36.1.

---

### Task 1: Lock 1.0.3 regression expectations

**Files:**
- Modify: `app/src/debug/java/com/openlauncher/app/RegressionChecks.kt`
- Modify: `app/src/main/java/com/openlauncher/app/data/ThemeMigration.kt`
- Modify: `app/src/main/java/com/openlauncher/app/model/WeatherState.kt`

- [ ] **Step 1: Add checks for the newly required pure behaviors**

```kotlin
check(migrateStoredAppFont("NOTO_SANS_SC") == AppFont.PIXEL)
check(migrateStoredAppFont("JETBRAINS_MONO") == AppFont.JETBRAINS_MONO)
check(!shouldUseCustomGradient(
    applyCustomBackground = true,
    useGradient = true,
    isDayMode = true,
    gradientEndLuminance = 0.01f
))
check(shouldCommitWeatherFetch(success = true))
check(!shouldCommitWeatherFetch(success = false))
```

- [ ] **Step 2: Run the regression task and confirm the missing helpers fail compilation**

Run: `A:\\CODE\\gradle-9.4.1\\bin\\gradle.bat regressionCheck --no-daemon`

Expected: failure that identifies `migrateStoredAppFont` and `shouldCommitWeatherFetch` as unresolved.

- [ ] **Step 3: Add minimal pure helper implementations**

```kotlin
fun migrateStoredAppFont(storedValue: String?): AppFont? = when (storedValue) {
    AppFont.NOTO_SANS_SC.name -> AppFont.PIXEL
    else -> storedValue?.let { value -> runCatching { AppFont.valueOf(value) }.getOrNull() }
}

fun shouldCommitWeatherFetch(success: Boolean): Boolean = success
```

- [ ] **Step 4: Run regression checks again**

Run: `A:\\CODE\\gradle-9.4.1\\bin\\gradle.bat regressionCheck --no-daemon`

Expected: `BUILD SUCCESSFUL`.

### Task 2: Apply the readable pixel typography and migration

**Files:**
- Modify: `app/src/main/java/com/openlauncher/app/ui/theme/Type.kt`
- Modify: `app/src/main/java/com/openlauncher/app/ui/theme/Theme.kt`
- Modify: `app/src/main/java/com/openlauncher/app/data/SettingsRepository.kt`

- [ ] **Step 1: Replace the unsafe display font path**

```kotlin
typography = launcherTypography(
    bold = fontBold,
    scale = textScale,
    fontFamily = appFont.toFontFamily(),
    displayFontFamily = appFont.toFontFamily()
)
```

`PixelFashion16` remains bundled for attribution compatibility but is not selected for Chinese UI, because its glyph coverage is incomplete.

- [ ] **Step 2: Read persisted font values through the migration helper**

```kotlin
appFont = migrateStoredAppFont(prefs[Keys.APP_FONT]) ?: defaults.appFont
```

- [ ] **Step 3: Verify the debug regression task**

Run: `A:\\CODE\\gradle-9.4.1\\bin\\gradle.bat regressionCheck --no-daemon`

Expected: `BUILD SUCCESSFUL`.

### Task 3: Unify rounded pixel surfaces and day-mode contrast

**Files:**
- Modify: `app/src/main/java/com/openlauncher/app/ui/theme/Shapes.kt`
- Modify: `app/src/main/java/com/openlauncher/app/ui/screen/HomeScreen.kt`
- Modify: `app/src/main/java/com/openlauncher/app/ui/screen/AppLibraryScreen.kt`
- Modify: `app/src/main/java/com/openlauncher/app/ui/screen/SettingsScreen.kt`
- Modify: `app/src/main/java/com/openlauncher/app/ui/screen/OnboardingScreen.kt`
- Modify: `app/src/main/java/com/openlauncher/app/ui/components/ColorPickerDialog.kt`
- Modify: `app/src/main/java/com/openlauncher/app/ui/components/Sidebar.kt`
- Modify: `app/src/main/java/com/openlauncher/app/ui/widget/NowPlayingWidget.kt`
- Modify: `app/src/main/java/com/openlauncher/app/ui/widget/SoundboardWidget.kt`
- Modify: `app/src/main/java/com/openlauncher/app/ui/widget/TripTrackerWidget.kt`
- Modify: `app/src/main/java/com/openlauncher/app/ui/widget/VitalsWidget.kt`

- [ ] **Step 1: Define the compact pixel geometry tokens**

```kotlin
val LauncherPixelShape = RoundedCornerShape(6.dp)
val LauncherChipShape = RoundedCornerShape(8.dp)
val LauncherControlShape = RoundedCornerShape(10.dp)
val LauncherCardShape = RoundedCornerShape(12.dp)
val LauncherLargeShape = RoundedCornerShape(14.dp)
val LauncherDialogShape = RoundedCornerShape(16.dp)
```

- [ ] **Step 2: Replace direct rectangular/rounded surface declarations with the closest token**

Use `LauncherPixelShape` for compact inputs and pads, `LauncherChipShape` for chips, `LauncherControlShape` for buttons and menus, `LauncherCardShape` for widgets, and `LauncherDialogShape` for dialogs. Keep only actual circular dials and album-art masks circular.

- [ ] **Step 3: Replace non-semantic day/night hard-coded text and background colors**

```kotlin
val contentColor = MaterialTheme.colorScheme.onSurface
val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
val trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
```

The onboarding surface must use `background`, `surface`, `onSurface`, and `outline` so light mode cannot inherit a near-black screen. The empty-shortcut add icon must use `onSurfaceVariant` instead of a fixed low-contrast gray.

- [ ] **Step 4: Verify all UI color and shape calls are explainable**

Run: `rg -n 'RoundedCornerShape|Color\\(0x|Color\\.White|Color\\.Black' app/src/main/java/com/openlauncher/app/ui app/src/main/java/com/openlauncher/app/MainActivity.kt`

Expected: remaining occurrences are only palette definitions, intentional album-art scrims, theme-selector swatches, or circular control accents.

### Task 4: Make weather refresh recoverable and race-safe

**Files:**
- Modify: `app/src/main/java/com/openlauncher/app/model/WeatherState.kt`
- Modify: `app/src/main/java/com/openlauncher/app/viewmodel/LauncherViewModel.kt`
- Modify: `app/src/debug/java/com/openlauncher/app/RegressionChecks.kt`

- [ ] **Step 1: Track successful fetches, not failed attempts**

```kotlin
if (shouldCommitWeatherFetch(success)) {
    lastSuccessfulFetchMs = now
    lastSuccessfulFetchCoordinates = lat to lon
}
```

- [ ] **Step 2: Keep in-flight requests cancellable and stale responses ignored**

```kotlin
val requestSequence = ++weatherRequestSequence
weatherJob?.cancel()
weatherJob = viewModelScope.launch {
    val success = runCatching { WeatherApi.service.getForecast(...) }.isSuccess
    if (requestSequence == weatherRequestSequence && success) {
        onWeatherFetchSucceeded(lat, lon)
    }
}
```

- [ ] **Step 3: Verify fresh coordinate changes still supersede the prior response**

Run: `A:\\CODE\\gradle-9.4.1\\bin\\gradle.bat regressionCheck --no-daemon`

Expected: `BUILD SUCCESSFUL` and current `weatherRequestSequence` remains the authority for response delivery.

### Task 5: Update release metadata and documentation

**Files:**
- Modify: `version.properties`
- Modify: `README.md`
- Create: `release 1.0.3 notes.md`

- [ ] **Step 1: Set the sole release source to 1.0.3 / 10003**

```properties
VERSION_NAME=1.0.3
VERSION_CODE=10003
```

- [ ] **Step 2: Document the pixel typography migration, three themes, light-mode protection, and weather recovery behavior in Chinese**

- [ ] **Step 3: Check that active documentation refers to the release source**

Run: `rg -n '当前版本|VERSION_NAME=|1\\.0\\.2|1\\.0\\.3' README.md 'release 1.0.3 notes.md' version.properties`

Expected: the README and `version.properties` identify 1.0.3.

### Task 6: Build, sign, verify, publish, and deliver

**Files:**
- Add: `release/openlauncher-zh-ayc404-v1.0.3.apk`
- Add: `A:\\Desktop\\openlauncher-zh-ayc404-v1.0.3.apk`

- [ ] **Step 1: Run all project verification tasks**

Run: `A:\\CODE\\gradle-9.4.1\\bin\\gradle.bat clean regressionCheck test lintRelease assembleRelease --no-daemon`

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 2: Align and sign the release APK with the existing upgrade-compatible keystore**

```powershell
& 'C:\\Android\\Sdk-new\\build-tools\\36.0.0\\zipalign.exe' -f -p 4 app-release-unsigned.apk openlauncher-zh-ayc404-v1.0.3-aligned.apk
& 'C:\\Android\\Sdk-new\\build-tools\\36.0.0\\apksigner.bat' sign --ks 'A:\\CODE\\openlauncher-debug.keystore' --ks-key-alias androiddebugkey --ks-pass pass:android --key-pass pass:android --out release\\openlauncher-zh-ayc404-v1.0.3.apk openlauncher-zh-ayc404-v1.0.3-aligned.apk
```

- [ ] **Step 3: Verify the exact signed artifact and package version**

Run: `apksigner verify --verbose release\\openlauncher-zh-ayc404-v1.0.3.apk` and `aapt dump badging release\\openlauncher-zh-ayc404-v1.0.3.apk`

Expected: signature verification succeeds and badging reports `versionName='1.0.3' versionCode='10003'`.

- [ ] **Step 4: Commit, push, tag, and publish**

Use a Lore-format commit, push `HEAD:codex/zh-modernize-baseline` with the configured local proxy, create `v1.0.3-ayc404`, upload the signed APK to the matching GitHub Release, and copy the same byte-identical file to `A:\\Desktop`.
