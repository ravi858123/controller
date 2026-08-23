# BT Control

[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

Een Android-app (Kotlin) die je telefoon laat werken als een echte
**Bluetooth HID-gamepad** — je verbindt hem net als een normale draadloze
controller met een PC, laptop of console. 100% vrije software, geen
tracking, geen advertenties, geen niet-vrije dependencies.

## Functies

- Analoge joystick, D-pad en 8 standaardknoppen (A/B/X/Y, L/R, Select/Start)
- Tot **8 zelf toe te voegen knoppen**: naam kiezen, toevoegen, hernoemen
  (door te verwijderen en opnieuw aan te maken) en verwijderen — lang
  indrukken op een eigen knop opent het verwijderdialoog
- Overzichtelijke Bluetooth-kiezer als bottom sheet, met live status per
  gekoppeld apparaat en een snelkoppeling naar de systeem Bluetooth-instellingen
- Splash screen met logo en app-naam bij het opstarten
- Donker, high-contrast controllerontwerp met ripple-feedback op elke knop

## Hoe werkt het?

De app gebruikt de ingebouwde Android `BluetoothHidDevice` API
(beschikbaar sinds Android 9 / API 28). Daarmee meldt de telefoon zich aan
als HID-apparaat met een "Game Pad" report descriptor (16 knoppen, een
analoge joystick en een D-pad — zie `HidGamepadDescriptor.kt`). Geen root
nodig, geen app op de host-computer.

## Belangrijke beperkingen

- **Alleen op een echt toestel testen**, niet op de emulator (geen echte
  Bluetooth-radio).
- Niet elke telefoon ondersteunt de HID-Device rol — hangt af van de
  Bluetooth-chipset/firmware. De meeste recente Android-toestellen
  ondersteunen dit wel.
- Koppel het toestel eerst via de normale Bluetooth-instellingen (of via de
  snelkoppeling in de app) vóórdat je een apparaat in de app selecteert.
- Op Android 12+ moet je de Bluetooth-permissies expliciet toestaan (vraagt
  de app automatisch bij de eerste start).

## Projectstructuur

```
BtController/
├── app/src/main/
│   ├── AndroidManifest.xml
│   ├── java/com/example/btcontroller/
│   │   ├── SplashActivity.kt              # Logo + naam bij opstarten
│   │   ├── MainActivity.kt                # UI-logica, knoppen → HID reports
│   │   ├── BluetoothHidController.kt      # Wrapper rond BluetoothHidDevice API
│   │   ├── HidGamepadDescriptor.kt        # HID report descriptor (16 knoppen)
│   │   ├── CustomButtonsManager.kt        # Opslag van eigen knoppen (JSON)
│   │   ├── BluetoothDeviceAdapter.kt      # RecyclerView-adapter apparatenlijst
│   │   ├── BluetoothDevicesBottomSheet.kt # Bluetooth-kiezer UI
│   │   └── views/JoystickView.kt          # Custom analoge joystick view
│   └── res/
│       ├── layout/                        # activity_main, activity_splash, ...
│       ├── drawable/ic_logo.xml           # Het app-logo
│       └── values/                        # strings, colors, styles, themes
├── .github/workflows/build.yml            # CI: bouwt automatisch een APK
├── .fdroid.yml                            # In-repo F-Droid build metadata
├── fastlane/metadata/android/             # F-Droid/Play store listing-teksten
├── LICENSE                                # Apache License 2.0
├── NOTICE
└── CHANGELOG.md
```

## Zelf bouwen

### Optie 1: Android Studio
1. Open de map `BtController/` in Android Studio (Hedgehog of nieuwer).
2. Laat Android Studio de Gradle-wrapper aanmaken/syncen.
3. Run op een fysiek toestel (minimaal Android 9 / API 28).

### Optie 2: command line
```bash
gradle assembleDebug
```
De APK verschijnt in `app/build/outputs/apk/debug/`.

## Automatisch bouwen via GitHub Actions

`.github/workflows/build.yml` bouwt bij elke push/PR automatisch een debug-
én release-APK en zet die klaar als download onder "Artifacts".

## Eigen knoppen aanpassen

- Standaardknop-mapping staat in `MainActivity.kt` bij `object ButtonBit`.
- Eigen knoppen gebruiken automatisch de eerste vrije bit in het tweede
  knoppen-byte (zie `CustomButtonsManager.kt`), tot een maximum van 8.
- De HID-descriptor (rapportformaat) staat in `HidGamepadDescriptor.kt`.

## Bijdragen aan F-Droid

Dit project is bewust vrij van niet-vrije dependencies, bevat een
Apache 2.0-licentie, een `NOTICE`-bestand en een `.fdroid.yml` met
in-repo buildmetadata, zodat het klaar is voor opname.

Om het daadwerkelijk in F-Droid te krijgen, moet er een merge request naar
de aparte metadata-repository [fdroiddata](https://gitlab.com/fdroid/fdroiddata)
worden ingediend (F-Droid bouwt apps altijd zelf vanuit broncode, dus een
APK aanleveren is niet voldoende):

1. Zorg dat er een getagde release staat in de broncode-repo, bijvoorbeeld
   `git tag v2.0.0 && git push --tags`.
2. Fork `fdroiddata` op GitLab.
3. Maak `metadata/com.example.btcontroller.yml` aan met (in essentie) de
   inhoud van dit repo's `.fdroid.yml`, of verwijs naar de in-repo variant
   volgens de [huidige F-Droid-documentatie over in-repo metadata](https://f-droid.org/docs/Build_Metadata_Reference/).
4. Dien een merge request in tegen `fdroiddata`. Een reviewer controleert
   reproduceerbaarheid van de build en de afwezigheid van niet-vrije code.
5. Volg eventuele feedback op in de merge request-discussie.

> Let op: `applicationId` in `app/build.gradle` (`com.example.btcontroller`)
> is een placeholder. Wijzig dit naar jouw eigen, unieke package-naam
> vóórdat je een merge request indient — F-Droid vereist een package-naam
> die je zelf beheert (bijv. een domein dat je bezit, omgekeerd genoteerd).

## Troubleshooting

- **"Geen gekoppelde apparaten gevonden"** → koppel het toestel eerst
  handmatig via Bluetooth-instellingen (knop onderaan de kiezer).
- **Host ziet de telefoon niet als gamepad** → niet elk toestel/OS
  ondersteunt de HID-Device rol; test op een ander toestel.
- **App crasht bij opstarten op Android 12+** → controleer of alle
  Bluetooth-permissies zijn geaccepteerd in de systeeminstellingen.

## Licentie

Apache License 2.0 — zie [`LICENSE`](LICENSE) en [`NOTICE`](NOTICE).
