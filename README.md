# BT Controller — telefoon als Bluetooth-gamepad

Een Android-app (Kotlin) die je telefoon laat werken als een echte
**Bluetooth HID-gamepad** — dus je verbindt hem net als een normale
draadloze controller met een PC, laptop of console.

## Hoe werkt het?

De app gebruikt de ingebouwde Android `BluetoothHidDevice` API
(beschikbaar sinds Android 9 / API 28). Daarmee meldt de telefoon zich
aan als HID-apparaat met een standaard "Game Pad" report descriptor
(8 knoppen, een analoge joystick en een D-pad). Geen root nodig.

Er wordt **geen** app op de host-computer geïnstalleerd — voor Windows,
macOS, Linux en de meeste consoles is dit een standaard Bluetooth-apparaat
zodra je hem koppelt.

## Belangrijke beperkingen

- **Alleen op een echt toestel testen**, niet op de emulator (die heeft
  geen echte Bluetooth-radio).
- Niet elke telefoon ondersteunt de HID-Device rol — dit hangt af van de
  Bluetooth-chipset/firmware. De meeste recente Android-toestellen (Qualcomm/
  Broadcom chipsets) ondersteunen dit wel.
- Koppel de telefoon eerst via de normale Bluetooth-instellingen van het
  host-apparaat (of vanaf de telefoon) vóórdat je in de app op "Verbind"
  drukt — de app kiest uit al gekoppelde (bonded) apparaten.
- Op Android 12+ moet je de Bluetooth-permissies expliciet toestaan
  (dit vraagt de app automatisch).

## Projectstructuur

```
BtController/
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/btcontroller/
│       │   ├── MainActivity.kt              # UI-logica, knoppen → HID reports
│       │   ├── BluetoothHidController.kt     # Wrapper rond BluetoothHidDevice API
│       │   ├── HidGamepadDescriptor.kt       # HID report descriptor (gamepad)
│       │   └── views/JoystickView.kt         # Custom analoge joystick view
│       └── res/layout/activity_main.xml      # Knoppen, D-pad, joystick UI
├── .github/workflows/build.yml               # CI: bouwt automatisch een APK
├── build.gradle / settings.gradle / gradle.properties
└── README.md
```

## Zelf bouwen

### Optie 1: Android Studio
1. Open de map `BtController/` in Android Studio (Hedgehog of nieuwer).
2. Laat Android Studio de Gradle-wrapper aanmaken/syncen.
3. Run op een fysiek toestel (minimaal Android 9 / API 28).

### Optie 2: command line
Zonder Gradle-wrapper (die zit niet in dit project, zie hieronder):
```bash
gradle assembleDebug
```
De APK verschijnt in `app/build/outputs/apk/debug/`.

> **Waarom geen `gradlew`?** Dit project levert bewust geen binaire
> `gradle-wrapper.jar` mee. Open het project één keer in Android Studio
> en het genereert de wrapper automatisch (`File > Sync Project with
> Gradle Files`), of gebruik een lokaal geïnstalleerde Gradle (zie
> [gradle.org/install](https://gradle.org/install/)).

## Automatisch bouwen via GitHub Actions

De workflow in `.github/workflows/build.yml` bouwt bij elke push/PR
automatisch een debug- én een (ongesigneerde) release-APK, en zet die
klaar als download onder "Artifacts" van de workflow-run. Er is ook een
losse kopie van dit bestand meegeleverd (`build.yml`) die je in elk ander
Android-project kunt plakken onder `.github/workflows/`.

## Knoppen aanpassen / uitbreiden

- Knop-mapping (welke bit hoort bij welke knop) staat in
  `MainActivity.kt` bij `object ButtonBit`.
- De HID-descriptor (rapportformaat) staat in `HidGamepadDescriptor.kt` —
  wil je bijvoorbeeld triggers (analoge L2/R2) of meer dan 8 knoppen,
  pas dan zowel de descriptor als `sendReport()` aan.
- De layout (knopposities) staat in `res/layout/activity_main.xml`.

## Troubleshooting

- **"Geen gekoppelde apparaten gevonden"** → koppel de telefoon eerst
  handmatig via Bluetooth-instellingen op het host-apparaat.
- **Host ziet de telefoon niet als gamepad** → niet elk toestel/OS
  ondersteunt de HID-Device rol; test op een andere telefoon of check of
  je Android-versie ≥ 9 is.
- **App crasht bij opstarten op Android 12+** → controleer of alle
  Bluetooth-permissies zijn geaccepteerd in de systeeminstellingen van
  de app.
