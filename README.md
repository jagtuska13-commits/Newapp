# Aether AI Companion

Aether is an AI companion application built with Android, Kotlin, Jetpack Compose, and the Gemini API.

## Setup Instructions

### Requirements
* Gradle 9.3.1 (configured via `gradle/wrapper/gradle-wrapper.properties`)
* Environment files: You will need to create a `.env` file in the project root containing `GEMINI_API_KEY`. See `.env.example` for details.

### Build and Deployment

#### 1. Gemini API Key
The app uses the Secrets Gradle Plugin to inject API keys.
Create a `.env` file at the root of the project by copying `.env.example`:
```bash
cp .env.example .env
```
Fill in your `GEMINI_API_KEY` in the `.env` file.

#### 2. Debug Build Configuration
To run a debug build, you need a local `debug.keystore` file in the root of the project. If you don't have one, generate one:
```bash
keytool -genkey -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Unknown, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=Unknown"
```
Run `./gradlew assembleDebug` to build the debug APK.

#### 3. Release Build Configuration
For a release build (`assembleRelease` or `bundleRelease`), you must set environment variables for keystore passwords, and ensure a keystore file named `my-upload-key.jks` exists in the project root.

To generate a new keystore (DO NOT COMMIT THIS FILE TO VERSION CONTROL!):
```bash
keytool -genkey -v -keystore my-upload-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
```

Provide the keystore passwords as environment variables during build:
```bash
STORE_PASSWORD=your_store_password KEY_PASSWORD=your_key_password ./gradlew assembleRelease
# Or for a bundle:
STORE_PASSWORD=your_store_password KEY_PASSWORD=your_key_password ./gradlew bundleRelease
```

**Note:** Never commit `my-upload-key.jks` or `debug.keystore` to version control. They are ignored in `.gitignore`.

### Testing
Use Robolectric and Roborazzi for testing. Run the tests with:
```bash
./gradlew test
```
