name: Android CI Build

on:
  push:
    branches: [ main, master ]
  pull_request:
    branches: [ main, master ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4
        with:
          gradle-version: '8.7'

      - name: Build debug APK
        run: gradle assembleDebug --no-daemon --stacktrace

      - name: Upload debug APK
        uses: actions/upload-artifact@v4
        with:
          name: app-debug-apk
          path: app/build/outputs/apk/debug/*.apk

      - name: Build release APK (unsigned)
        run: gradle assembleRelease --no-daemon --stacktrace

      - name: Upload release APK
        uses: actions/upload-artifact@v4
        with:
          name: app-release-unsigned-apk
          path: app/build/outputs/apk/release/*.apk
