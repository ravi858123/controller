# Bijdragen aan BT Control

Bedankt voor je interesse! Een paar richtlijnen:

## Licentie

Dit project staat onder de Apache License 2.0 (zie `LICENSE`). Door een
pull request in te dienen ga je ermee akkoord dat je bijdrage onder
dezelfde licentie wordt vrijgegeven (zie Sectie 5 van de Apache License).

## Afhankelijkheden

Om het project geschikt te houden voor opname in F-Droid:
- Voeg geen afhankelijkheden toe die niet-vrije/proprietary code bevatten
  (bijv. Google Play Services, Firebase, Crashlytics, advertentie-SDK's).
- Gebruik bij voorkeur bibliotheken die al in de AndroidX/Jetpack-familie
  zitten, of voeg een korte onderbouwing toe in de pull request waarom een
  nieuwe dependency nodig is.
- Pin altijd een exacte versie (geen `+` of dynamische ranges) voor
  reproduceerbare builds.

## Code-stijl

- Kotlin, met de standaard Android/JetBrains-conventies.
- Nieuwe bestanden krijgen de Apache 2.0-headerblok bovenaan (zie een
  bestaand `.kt`-bestand als voorbeeld).
- UI-tekst gaat via `strings.xml`, geen hardcoded strings in layouts/code.

## Testen

Dit project kan niet op een emulator getest worden (Bluetooth HID vereist
een echte radio). Test wijzigingen op een fysiek toestel met Android 9+ en
vermeld in je pull request op welk toestel/Android-versie je hebt getest.

## Pull requests

1. Fork de repository en maak een feature-branch.
2. Beschrijf in de PR wat er verandert en waarom.
3. Zorg dat de GitHub Actions build (`.github/workflows/build.yml`) slaagt.
