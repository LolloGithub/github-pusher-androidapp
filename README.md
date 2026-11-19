# GitHub Zip Pusher (Android)

Share a **ZIP** from the Android file manager, choose a saved **repository configuration**, and **commit + push** the files to GitHub via the Contents API.

Reusable utility. Code comments and this README are in **English**.

## Features

- Multiple named configurations (repo URL, branch, token, path prefix, commit message)
- Tokens in **EncryptedSharedPreferences** (device-only)
- Share / open-with for ZIP files
- After share: **pick which configuration** to use, then push
- **GitHub Actions** builds a debug APK (no Android Studio required)

## Build the APK (GitHub Actions)

1. Open **Actions** → **Build APK**
2. Download artifact `GitHubZipPusher-debug-apk`
3. Install on the device

Manual run: **Actions** → **Run workflow**.

## Configure on the phone

1. Open the app → **+**
2. Fill in:

| Field | Example |
|--------|---------|
| Display name | `MobyRoma assets` |
| Repository URL | `https://github.com/OWNER/REPO` or `OWNER/REPO` |
| Branch | `main` |
| Token | PAT with `repo` or fine-grained Contents write |
| Path prefix | `assets` (optional; empty = repo root) |
| Commit message | `Update from zip` |

3. Save. Add more configs for other repos/folders.

### Token

GitHub → Settings → Developer settings → Personal access tokens.  
Classic: `repo` scope. Fine-grained: Contents read/write on the target repo.  
Never commit tokens to git.

## Share a ZIP

1. File manager → select `.zip` → **Share** → **GitHub Zip Pusher**
2. Tap the configuration to use
3. Tap **Commit and push**

One config only → auto-selected.

Path rules: zip entry `index.html` + prefix `assets` → `assets/index.html`.

## Security

- Token only sent to `api.github.com`
- Backup disabled (`allowBackup=false`)
- Prefer fine-grained, repo-scoped tokens

## Layout

```
app/src/main/java/com/githubpusher/app/
  MainActivity.kt, EditConfigActivity.kt, SharePushActivity.kt
  ConfigStore.kt, RepoConfig.kt, RepoUrlParser.kt
  ZipReader.kt, GitHubUploader.kt, ConfigListAdapter.kt, ShareIntents.kt
```

## Limitations

- One commit per file in the zip
- Large zips = more API calls / time

## License

Free to use and modify. No warranty.
