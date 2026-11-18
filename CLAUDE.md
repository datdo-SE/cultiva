# CLAUDE.md

## Project

Cultiva (product name **Phù Lục**) is a Vietnamese-language screen-time app for iOS and Android with a cultivation/talisman theme. Users hand-draw talismans (bùa) and stick them on distracting apps; each talisman is one rule on one app.

## Build (Android)

Project lives in `android/` (single `:app` module, package `com.yaycommerce.cultiva`, minSdk 26, target/compile 35). Needs JDK 17.

```sh
cd android
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
./gradlew assembleDebug        # APK: app/build/outputs/apk/debug/app-debug.apk
./gradlew testDebugUnitTest
```

`android/local.properties` points `sdk.dir` at `/opt/homebrew/share/android-commandlinetools` and is git-ignored. Versions live in `android/gradle/libs.versions.toml`. Theme colors in `ui/theme/Color.kt` mirror `docs/SPEC.md` §8; change both together.

## Stack and platform order

- Android ships first: native Kotlin. UI in Jetpack Compose (the canvas notes specify Compose for the hold gesture and breathing ring).
- iOS comes later (Swift/SwiftUI with FamilyControls). Its constraints in `docs/SPEC.md` §7.2 shape shared rules (e.g. the 18-schedule cap), so do not design Android-only behavior that iOS cannot match.
- Android blocking: a full-screen ritual Activity appears directly when a blocked app opens; there is no shield step. Two permission rows in onboarding, each with "Mở Cài đặt".

## Sources of truth

- `docs/SPEC.md`: behavior, rules, screens, platform mechanics, open questions. Read it before building any feature.
- Design canvas "Phù Lục — Thiết kế màn hình": https://claude.ai/artifact/EZez3nRWeAtXR4UBZgecc6. Visual source of truth (18 artboards, phone 390×844).
- If SPEC and canvas disagree, ask before building. When either changes, update the other in the same task.

## Invariants (easy to get wrong)

- One talisman = one rule on one app. A realm with K apps needs K talismans.
- Removing a talisman destroys it. It never returns to the pouch.
- Precedence: Cấm Phù in window > exhausted Định Thời > Tĩnh Tâm.
- Override cost: Định Thời 30 đạo hạnh (talisman torn), Cấm 50 (not torn, relocks after 15 min). Not enough points: button states the exact shortfall.
- Ấn son (seal) shows the template sign only: 定 Định Thời, 禁 Cấm, 靜 Tĩnh Tâm. Never the app name.
- Cấm Phù + active formations ≤ 18, because iOS allows 20 `DeviceActivity` schedules.
- Android: no Accessibility Service, ever.
- iOS (later): shield (`ShieldConfiguration`) cannot open the main app or take a hold gesture. Rituals start from a local notification. Tĩnh Tâm still needs a shield on iOS.
- No usage data leaves the device. The app never sees app names on iOS (opaque tokens).

## Conventions

- All UI copy is Vietnamese with full diacritics. Keep glossary terms (bùa, cõi, động phủ, trận pháp, phong ấn, phá phong, đạo hạnh, tu vi) as defined in `docs/SPEC.md` §2; do not translate or rename them in UI.
- Colors and fonts come from `docs/SPEC.md` §8. Linh khí `#8CC6A8` is for remaining time only.
- Every drawing interaction needs a tap or keyboard alternative. Touch targets ≥ 44 px.
