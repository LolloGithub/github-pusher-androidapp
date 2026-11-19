# GitHub Zip Pusher (Android)

Share a **ZIP** from the Android file manager, choose a saved **repository configuration**, and **commit + push** the files to GitHub via the Contents API.

Reusable utility. Code comments and this README are in **English**.

## Features

- Multiple named configurations (repo URL, branch, token, path prefix, commit message)
- Tokens in **EncryptedSharedPreferences** (device-only)
- **Export / import** configurations to a JSON file
- Share / open-with for ZIP files
- After share: **pick which configuration** to use, then push
- Optional **delete list** file inside the zip to remove paths from the repo
- **GitHub Actions** builds a debug APK (no Android Studio required)

## Build the APK (GitHub Actions)

1. Open **Actions** → **Build APK**
2. Download artifact `GitHubZipPusher-debug-apk`
3. Install on the device

## Configure on the phone

1. Open the app → **+**
2. Fill in repo URL, branch, token, optional path prefix, commit message
3. Save

### Export / import configurations

On the home screen:

- **Export** — saves all configurations to a JSON file (you choose the location)
- **Import** — loads configurations from a JSON file and merges them by `id`

Default export name: `github-zip-pusher-configs.json`

**Security:** the file contains **tokens in plain text**. Do not share it or commit it to git.

## Share a ZIP

1. File manager → `.zip` → **Share** → **GitHub Zip Pusher**
2. Choose configuration → **Commit and push**

Path rules: zip entry `index.html` + prefix `assets` → `assets/index.html`.

## Deleting files (`github-pusher-delete.txt`)

Include in the zip:

- `github-pusher-delete.txt`, or `.github-pusher-delete`

One **repo-root-relative** path per line (`#` comments allowed).  
Path prefix is **not** applied to delete paths. Missing files are skipped.

## Security

- Token only sent to `api.github.com` (except when you export a backup file)
- Backup disabled (`allowBackup=false`)

## Limitations

- One commit per file upload or delete
- Cannot delete directories via Contents API (list file paths only)

## License

Free to use and modify. No warranty.
