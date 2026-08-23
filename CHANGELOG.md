# Changelog

Alle noemenswaardige wijzigingen aan dit project worden hier bijgehouden.
Indeling gebaseerd op [Keep a Changelog](https://keepachangelog.com/).

## [2.0.0] - 2026-08-23
### Toegevoegd
- Nieuw app-logo, verwerkt als adaptive icon en in een eigen splash screen.
- Splash screen met logo en de naam "BT Control" bij het opstarten.
- Volledig herontworpen bedieningselementen (D-pad, joystick, face buttons,
  schouderknoppen) met een donker thema en ripple-feedback.
- Nieuwe Bluetooth-apparatenkiezer als bottom sheet met verbindingsstatus per
  apparaat, in plaats van een kale systeemdialoog.
- Ondersteuning voor tot 8 door de gebruiker zelf toe te voegen, te
  hernoemen en te verwijderen extra knoppen (HID-descriptor uitgebreid van
  8 naar 16 knoppen).
- Apache License 2.0, NOTICE-bestand en F-Droid inclusiemetadata
  (`.fdroid.yml`, fastlane store-listing).

### Gewijzigd
- HID input-report uitgebreid van 4 naar 5 bytes om ruimte te maken voor de
  eigen knoppen (zie `HidGamepadDescriptor.kt`).
- App-naam gewijzigd naar "BT Control".

## [1.0.0] - 2026-08-22
### Toegevoegd
- Eerste versie: telefoon als Bluetooth HID-gamepad met joystick, D-pad,
  4 face buttons, 2 schouderknoppen en select/start.
