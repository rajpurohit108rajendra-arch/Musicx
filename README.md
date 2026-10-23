# MusicX Android app

MusicX is a starter Android app written in Kotlin + Jetpack Compose. It includes:
- Spotify-inspired dark interface (not affiliated with Spotify)
- Search by track title or artist
- Streaming playback using AndroidX Media3 ExoPlayer
- A simple local favorites playlist
- Downloads to the device's public Downloads folder using Android DownloadManager

## Requirements
- Android Studio (current stable recommended)
- JDK 17
- Android SDK Platform 35
- Internet access for Gradle dependencies and streaming demo audio

## Open and run
1. Extract `MusicX-Android.zip`.
2. Open the extracted `MusicX-Android` folder in Android Studio.
3. Allow Gradle sync to finish.
4. Select an emulator or connect an Android phone with USB debugging enabled.
5. Press Run.

Android Studio will create the Gradle wrapper if needed, or you can create it from Android Studio's Gradle tooling. A wrapper is not included in this starter archive.

## Build an APK
In Android Studio choose **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
The debug APK normally appears at:
`app/build/outputs/apk/debug/app-debug.apk`

## Add your own music
Edit the `tracks` list in:
`app/src/main/java/com/musicx/app/MainActivity.kt`

Each track uses:
- `title`: track name
- `artist`: artist/credit
- `url`: direct HTTPS URL to an audio file you own or are licensed to stream/download

Replace the demo URLs with direct links to your own audio files or tracks whose licence explicitly allows both streaming and downloading. A webpage URL is not necessarily a direct audio URL. Verify the licence and host terms before publishing.

## Notes and limitations
- Demo URLs are for testing the player flow; confirm the provider's current terms and permissions before using them in a published app.
- Favorites are stored in memory and reset when the app process is restarted. Persistent playlists, accounts, server catalogue, artwork, and robust download management need a backend/database.
- The download button saves a copy to the device Downloads folder; it is not DRM or offline-cache integration.
- This is a starter project, not a signed release APK. You must build and sign it in Android Studio before distributing.
- Do not download or redistribute copyrighted tracks without permission.


## Phone and online audio (updated build)
- Tap **Add phone songs** and select one or more audio files from your device. This uses Android's document picker, so no broad storage permission is needed.
- To add online audio, paste a direct `https://` or `http://` audio-stream URL. Ordinary music webpage links (YouTube/Spotify pages) are not direct audio streams and will not work. Use only streams you have permission to play.
- The three SoundHelix items remain demo tracks.
