# Smart Business Toolkit

Indian small-business platform for GST billing, customers, expenses, UPI payments, inventory and business reports.

## Project type

Android app (Kotlin, Gradle). Application ID: `com.aistudio.smartbiz.kvrtxm`

## Build

```bash
./gradlew assembleDebug
```

## Setup

- If the app uses Gemini, copy `.env.example` to `.env` and add your own `GEMINI_API_KEY`. Never commit real keys.
- Release builds need your own keystore. Keep it outside the repo and pass `STORE_PASSWORD` / `KEY_PASSWORD` as environment variables.

> Note: `Business-tool-kit-web-` and `Smart-Business-Toolkit` contain the same app. Keep one and archive the other.

GST calculations should be verified by a CA before real customers rely on them.
