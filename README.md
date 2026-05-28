# MedTracker (Android)

Medication tracking app built with:

- Kotlin + Jetpack Compose UI
- Room persistence
- MVVM (Repository + ViewModels + Flows)
- AlarmManager reminders (intake)
- WorkManager periodic checks (low stock)

## Open / Run

Open the workspace folder in **Android Studio** and run the `app` configuration.

## Reminders

- Intake reminders are scheduled as exact alarms and re-scheduled on device reboot.
- Low stock alerts run every ~12 hours via WorkManager.

## Notes about the Gradle wrapper

This repo includes `gradle/wrapper/gradle-wrapper.properties` but not the binary `gradle-wrapper.jar`.
Android Studio can regenerate wrapper artifacts automatically when you sync the project.

