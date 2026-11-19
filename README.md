# GitHub Zip Pusher (Android)

Share a **ZIP** from the Android file manager, choose a saved **repository configuration**, and **commit + push** the files to GitHub via the Contents API.

Reusable utility. Code comments and this README are in **English**.

## Features

- Multiple named configurations (repo URL, branch, token, path prefix, commit message)
- Tokens in **EncryptedSharedPreferences** (device-only)
- Share / open-with for ZIP files
- After share: **pick which configuration** to use, then push
- Optional **delete list** file inside the zip to remove paths from the repo
- **GitHub Actions** builds a debug APK (no Android Studio required)

## Build the APK (GitHub Actions)

1. Open **Actions** → **Build APK**
2. Download artifact `GitHubZipPusher-debug-apk`
3. Install on the device

Manual run: **Actions** → **Run workflow**.

## Configure on the phone

1. Open the app → **+**
2. Fill in repo URL, branch, token, optional path prefix, commit message
3. Save. Add more configs for other repos/folders.

### Token

Classic: `repo` scope. Fine-grained: Contents read/write on the target repo.  
Never commit tokens to git.

## Share a ZIP

1. File manager → select `.zip` → **Share** → **GitHub Zip Pusher**
2. Tap the configuration to use
3. Tap **Commit and push**

Path rules: zip entry `index.html` + prefix `assets` → `assets/index.html`.

## Deleting files (`github-pusher-delete.txt`)

To remove paths from the repository, include a text file in the zip named:

- `github-pusher-delete.txt`, or
- `.github-pusher-delete`

**Format:** one **repository-root-relative** path per line.

```text
# Comments start with #
assets/p0.txt
assets/p1.txt
assets/css/app-00.css
```

Rules:

- Paths are from the **repo root** (path prefix is **not** applied to delete paths)
- Blank lines and `#` comments are ignored
- Missing files on GitHub are skipped (no error)
- The delete-list file itself is **not** uploaded to the repo
- Order: all uploads first, then all deletes

Example zip for MobyRoma (path prefix empty):

```text
assets/index.html
github-pusher-delete.txt   # lists old p*.txt, bootstrap leftovers, etc.
```

## Security

- Token only sent to `api.github.com`
- Backup disabled (`allowBackup=false`)

## Limitations

- One commit per file upload or delete
- Large zips = more API calls / time
- Cannot delete directories via Contents API (list file paths only)

## License

Free to use and modify. No warranty.
