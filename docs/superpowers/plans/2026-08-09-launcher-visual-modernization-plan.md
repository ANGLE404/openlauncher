# 开放启动器视觉现代化实施计划

> **For agentic workers:** Execute this plan task-by-task with verification after each task.

**Goal:** Implement three polished rounded themes, customizable colors, complete visible Chinese localization, and a signed desktop APK.

**Architecture:** Keep `AppSettings` and `SettingsRepository` as the persisted source of theme choices. Add centralized theme palette and shape helpers under `ui/theme`, then migrate screens/components to those helpers while preserving widget IDs and protocol strings.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, DataStore Preferences, Gradle 9.4.1, Android SDK 36.1.

---

### Task 1: Lock the design baseline

**Files:**
- Create: `docs/superpowers/specs/2026-08-09-launcher-visual-modernization-design.md`
- Create: `docs/superpowers/plans/2026-08-09-launcher-visual-modernization-plan.md`

- [x] Commit the design and implementation plan before source edits.

### Task 2: Centralize theme palettes and shapes

**Files:**
- Modify: `app/src/main/java/com/openlauncher/app/ui/theme/Color.kt`
- Modify: `app/src/main/java/com/openlauncher/app/ui/theme/Theme.kt`
- Create: `app/src/main/java/com/openlauncher/app/ui/theme/Shapes.kt`
- Modify: `app/src/main/java/com/openlauncher/app/data/AppSettings.kt`
- Modify: `app/src/main/java/com/openlauncher/app/data/SettingsRepository.kt`

- [ ] Define the three theme presets and a `ThemeColors` value object containing background, surface, elevated surface, accent, border, primary text, and secondary text.
- [ ] Define shared `RoundedCornerShape` values for card, dialog, button, field, chip, and navigation surfaces.
- [ ] Make Material 3 color schemes consume the resolved palette and animated accent color.
- [ ] Persist the selected preset and color overrides with DataStore; fall back to OEM defaults when keys are missing.

### Task 3: Build the theme editor UI

**Files:**
- Modify: `app/src/main/java/com/openlauncher/app/ui/screen/SettingsScreen.kt`

- [ ] Add a three-theme selector with Chinese names and selected-state preview.
- [ ] Add color swatches for accent, background, surface, text, and border colors.
- [ ] Add a reset-to-theme-default action that clears overrides without clearing unrelated settings.
- [ ] Use shared shapes and animated color transitions for the selector.

### Task 4: Apply rounded surfaces across the UI

**Files:**
- Modify: `app/src/main/java/com/openlauncher/app/ui/screen/HomeScreen.kt`
- Modify: `app/src/main/java/com/openlauncher/app/ui/screen/SettingsScreen.kt`
- Modify: `app/src/main/java/com/openlauncher/app/ui/screen/AppLibraryScreen.kt`
- Modify: `app/src/main/java/com/openlauncher/app/ui/components/Sidebar.kt`
- Modify: `app/src/main/java/com/openlauncher/app/ui/components/ColorPickerDialog.kt`
- Modify: `app/src/main/java/com/openlauncher/app/ui/components/ConfirmDialog.kt`
- Modify: `app/src/main/java/com/openlauncher/app/ui/widget/*.kt`

- [ ] Replace primary rectangular surfaces with shared card/dialog/button/field shapes.
- [ ] Preserve stable grid dimensions and touch targets while rounding only visual containers.
- [ ] Use theme border and surface colors instead of hardcoded black/gray borders where the component is user-facing.
- [ ] Keep metric readouts monospaced while human-readable Chinese labels use Noto Sans SC.

### Task 5: Complete visible Chinese cleanup

**Files:**
- Modify: all Kotlin files under `app/src/main/java/com/openlauncher/app/ui`
- Modify: `app/src/main/java/com/openlauncher/app/MainActivity.kt`
- Modify: `app/src/main/res/values/strings.xml`

- [ ] Scan string literals and `contentDescription` values for user-visible English.
- [ ] Translate remaining labels, empty states, errors, dialog actions, and accessibility text.
- [ ] Leave widget IDs, API keys, package names, units, media metadata, and protocol values unchanged.
- [ ] Run a second scan and manually classify every remaining English literal.

### Task 6: Add focused motion and state polish

**Files:**
- Modify: `app/src/main/java/com/openlauncher/app/ui/theme/Theme.kt`
- Modify: `app/src/main/java/com/openlauncher/app/ui/widget/TelemetryWidget.kt`
- Modify: `app/src/main/java/com/openlauncher/app/MainActivity.kt`

- [ ] Animate theme color changes over 420ms.
- [ ] Smooth compass rotation and preserve shortest-path behavior around 0/360 degrees.
- [ ] Keep page transitions short and disable decorative continuous animation.

### Task 7: Build, sign, verify, and publish

**Files:**
- Modify: `README.md` if the artifact name changes
- Add: `release/openlauncher-zh-ayc404.apk`

- [ ] Run `gradle.bat :app:assembleRelease` with JDK 17 and SDK 36.1.
- [ ] Sign with the existing local test keystore so upgrades remain compatible.
- [ ] Verify APK with `apksigner verify --verbose` and inspect package metadata with `aapt dump badging`.
- [ ] Copy the signed APK to `C:\Users\QQB\Desktop`.
- [ ] Commit source and APK, then push through the configured proxy to `codex/zh-modernize-baseline`.
