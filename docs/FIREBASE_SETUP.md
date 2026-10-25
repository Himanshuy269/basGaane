# Setting Up Cloud Sync for Friends (Firebase)

BasGaane is architected so that any song you add can automatically be available to your friends when they install the app on their devices.

To enable this cross-device sharing, you can connect a free Firebase project.

---

## Step 1: Create a Firebase Project

1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Click **Add Project** and name it `BasGaane` (or any name you prefer).
3. Disable or enable Google Analytics (either is fine), then click **Create Project**.

---

## Step 2: Register Android App

1. In your Firebase project dashboard, click the **Android** icon to add an Android app.
2. Enter the Package Name:
   ```
   com.example.basgaane
   ```
3. Enter an App nickname (e.g. `BasGaane App`).
4. Download the `google-services.json` file.
5. Move `google-services.json` into the `app/` folder of this project:
   ```
   BasGaane/
   └── app/
       └── google-services.json   <-- Place here
   ```

---

## Step 3: Enable Firebase Storage & Firestore

1. **Cloud Firestore**:
   - In the Firebase sidebar, go to **Build > Firestore Database**.
   - Click **Create Database**, select a region close to you.
   - Choose **Start in test mode** (allows read/write during development) or configure security rules.

2. **Cloud Storage**:
   - In the Firebase sidebar, go to **Build > Storage**.
   - Click **Get Started**, choose **Start in test mode**, and finish setup.
   - This bucket is where MP3/audio files uploaded via the app will be stored.

---

## Step 4: Enable the Google Services Plugin in Gradle

Once you have placed `google-services.json` in `app/`:

1. In `app/build.gradle.kts`, apply the Google services plugin:
   ```kotlin
   plugins {
       alias(libs.plugins.android.application)
       alias(libs.plugins.kotlin.compose)
       id("com.google.gms.google-services")
   }
   ```
2. Add Firebase libraries to your dependencies:
   ```kotlin
   implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
   implementation("com.google.firebase:firebase-firestore-ktx")
   implementation("com.google.firebase:firebase-storage-ktx")
   ```

---

## How It Works For Your Friends

1. **You** open the app, tap **Add Music**, select a song from your phone, and hit **Add Track**.
2. The app uploads the audio file to Firebase Cloud Storage and stores the title, artist, duration, and audio stream URL in Firestore.
3. When **your friends** install and open the app on their devices, the Firestore real-time listener automatically loads all shared tracks!
4. They can tap any song to stream or download it instantly.
