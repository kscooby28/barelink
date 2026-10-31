# Barelink

Android app that shortens a link by deleting the `?` and everything after it. That junk is almost always tracking (utm tags, fbclid, eBay mkcid, Amazon tag). The part that actually identifies the page stays.

Nothing leaves the phone. The app has no internet permission.

Private. Not on the Play Store.

## Install

1. On the phone, open this repo in the browser while signed into GitHub.
2. Open **Releases** and download `barelink.apk` (the build action publishes it a few minutes after a push).
3. Open the file and allow your browser to install unknown apps when Android asks.
4. Open **Barelink**.

The first release appears after the **Build APK** action on `main` finishes. If Releases is empty, wait for that green check, then refresh.

## How to use it

**Sharing (works as soon as the app is installed)**  
Share a link from any app and pick **Barelink**. It copies the short link and opens the share sheet again so you can send it to Messages, email, wherever. Turn off “Open the share sheet again” if you only want it on the clipboard.

**Selection menu, next to the keyboard**  
Select a link, tap the overflow on the selection toolbar, tap **Strip link**. The selected text is replaced with the short version.

**Automatic, on every copy**  
Turn on “Clean copied links automatically”. Android only lets the current keyboard, or the app that is on screen, read the clipboard. Barelink does not replace your keyboard. It uses a one-frame invisible window (that is the “display over other apps” permission), rewrites the link, and disappears. Also allow the quiet notification, and tell the phone not to sleep the app, or the watcher dies in the background.

**Quick Settings**  
Edit your tiles and add **Strip link**. One tap cleans whatever is copied.

## What gets cut

“Cut everything after ?” is on by default.

`https://www.ebay.com/itm/123?mkcid=1&campid=9`  
becomes  
`https://www.ebay.com/itm/123`

Words around the link are left alone.

Turn that switch **off** if you share YouTube `watch?v=` links or Google searches. Tracking-only mode drops `utm_*`, `fbclid`, `gclid`, eBay `mkcid` / `campid`, Amazon `tag`, and similar, and keeps real query parameters.

## Build it yourself

Android Studio, or:

```
gradle :app:testDebugUnitTest :app:assembleDebug
```

Gradle 8.11, JDK 17. Debug APK: `app/build/outputs/apk/debug/app-debug.apk`.
