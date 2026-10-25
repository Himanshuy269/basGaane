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

## Step 3: Enable Anonymous Authentication

In order for you and your friends to securely upload and stream audio without requiring a manual login screen:
1. In the Firebase sidebar, go to **Build > Authentication**.
2. Click **Get Started** (if not already enabled).
3. Under the **Sign-in method** tab, select **Anonymous**.
4. Enable the toggle and click **Save**.

The app automatically signs in anonymously in the background so both you and your friends can upload and stream music seamlessly!

---

## Step 4: Configure Security Rules (Firestore & Storage)

### 1. Cloud Firestore Rules
In the Firebase Console, go to **Firestore Database > Rules** tab, paste the following rules, and click **Publish**:
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

### 2. Cloud Storage Rules
In the Firebase Console, go to **Storage > Rules** tab, paste the following rules, and click **Publish**:
```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

> [!NOTE]
> `request.auth != null` allows any user using your app (authenticated anonymously) to upload songs and listen to shared songs, while keeping your database protected from unauthenticated scrapers.

---

## Step 5: How Cross-Device Sync Works

1. **Upload**: When you tap **Add Music**, select a song, and tap **Add Track**:
   - The app signs in anonymously (if not already authenticated).
   - The audio stream is uploaded to Firebase Cloud Storage.
   - The song metadata (title, artist, duration, and cloud download URL) is written to Firestore in the `songs` collection.
   - A snackbar confirmation appears: `✓ Song '<title>' uploaded and synced with friends!`.
2. **Instant Sync for Friends**:
   - When your friend installs and opens the app, `FirebaseSongDataSource` listens to Firestore in real-time.
   - All songs published by you (and other friends) appear immediately in their song list with a cyan **Cloud** badge.
   - Tapping any cloud song streams high-fidelity audio directly from Cloud Storage via ExoPlayer.
3. **Offline / Device Fallback**:
   - If an upload fails (e.g., no internet or incorrect Firebase rules), the app saves the track locally, flags it with a **Device** badge, and displays a descriptive error message explaining why cloud sync failed.

