# apk-files

Check for new versions apks in the downloads folder

## Install

```bash
npm install apk-files
npx cap sync
```

## API

<docgen-index>

* [`listDownloadApks(...)`](#listdownloadapks)
* [`listAllApks(...)`](#listallapks)
* [`debugScan()`](#debugscan)
* [`scanSpecificPath(...)`](#scanspecificpath)
* [`requestAllFilesAccess()`](#requestallfilesaccess)
* [`checkPermissions()`](#checkpermissions)
* [`installApk(...)`](#installapk)
* [`startDownloadListener()`](#startdownloadlistener)
* [`stopDownloadListener()`](#stopdownloadlistener)
* [`downloadApk(...)`](#downloadapk)
* [`getDownloadStatus(...)`](#getdownloadstatus)
* [`cancelDownload(...)`](#canceldownload)
* [`deleteApk(...)`](#deleteapk)
* [`startDownloadProgressListener(...)`](#startdownloadprogresslistener)
* [`stopDownloadProgressListener()`](#stopdownloadprogresslistener)
* [`addListener('downloadComplete', ...)`](#addlistenerdownloadcomplete-)
* [`addListener('installationStarted', ...)`](#addlistenerinstallationstarted-)
* [`addListener('downloadProgress', ...)`](#addlistenerdownloadprogress-)
* [`removeAllListeners()`](#removealllisteners)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### listDownloadApks(...)

```typescript
listDownloadApks(options?: ListApksOptions | undefined) => Promise<ApkListResult>
```

List APK files found in the Downloads directories

| Param         | Type                                                        | Description                  |
| ------------- | ----------------------------------------------------------- | ---------------------------- |
| **`options`** | <code><a href="#listapksoptions">ListApksOptions</a></code> | - Optional filtering options |

**Returns:** <code>Promise&lt;<a href="#apklistresult">ApkListResult</a>&gt;</code>

--------------------


### listAllApks(...)

```typescript
listAllApks(options?: ListApksOptions | undefined) => Promise<ApkListResult>
```

List all APK files on the device (including Downloads and other directories)

| Param         | Type                                                        | Description                  |
| ------------- | ----------------------------------------------------------- | ---------------------------- |
| **`options`** | <code><a href="#listapksoptions">ListApksOptions</a></code> | - Optional filtering options |

**Returns:** <code>Promise&lt;<a href="#apklistresult">ApkListResult</a>&gt;</code>

--------------------


### debugScan()

```typescript
debugScan() => Promise<DebugScanResult>
```

Debug scan to check Download directories and their contents

**Returns:** <code>Promise&lt;<a href="#debugscanresult">DebugScanResult</a>&gt;</code>

--------------------


### scanSpecificPath(...)

```typescript
scanSpecificPath(options: ScanPathOptions) => Promise<ScanPathResult>
```

Scan a specific directory path for APK files

| Param         | Type                                                        | Description                  |
| ------------- | ----------------------------------------------------------- | ---------------------------- |
| **`options`** | <code><a href="#scanpathoptions">ScanPathOptions</a></code> | - Path and filtering options |

**Returns:** <code>Promise&lt;<a href="#scanpathresult">ScanPathResult</a>&gt;</code>

--------------------


### requestAllFilesAccess()

```typescript
requestAllFilesAccess() => Promise<PermissionResult>
```

Request "All Files Access" permission (Android 11+)

**Returns:** <code>Promise&lt;<a href="#permissionresult">PermissionResult</a>&gt;</code>

--------------------


### checkPermissions()

```typescript
checkPermissions() => Promise<PermissionsStatus>
```

Check current permissions status

**Returns:** <code>Promise&lt;<a href="#permissionsstatus">PermissionsStatus</a>&gt;</code>

--------------------


### installApk(...)

```typescript
installApk(options: InstallApkOptions) => Promise<InstallResult>
```

Install an APK file

| Param         | Type                                                            | Description                           |
| ------------- | --------------------------------------------------------------- | ------------------------------------- |
| **`options`** | <code><a href="#installapkoptions">InstallApkOptions</a></code> | - Installation options with file path |

**Returns:** <code>Promise&lt;<a href="#installresult">InstallResult</a>&gt;</code>

--------------------


### startDownloadListener()

```typescript
startDownloadListener() => Promise<DownloadListenerResult>
```

Start listening for download completion events
Returns a promise that resolves when listener is active
Fires 'downloadComplete' event when downloads finish

**Returns:** <code>Promise&lt;<a href="#downloadlistenerresult">DownloadListenerResult</a>&gt;</code>

--------------------


### stopDownloadListener()

```typescript
stopDownloadListener() => Promise<DownloadListenerResult>
```

Stop listening for download completion events

**Returns:** <code>Promise&lt;<a href="#downloadlistenerresult">DownloadListenerResult</a>&gt;</code>

--------------------


### downloadApk(...)

```typescript
downloadApk(options: DownloadApkOptions) => Promise<DownloadResult>
```

Download an APK file using Android DownloadManager

| Param         | Type                                                              | Description                                   |
| ------------- | ----------------------------------------------------------------- | --------------------------------------------- |
| **`options`** | <code><a href="#downloadapkoptions">DownloadApkOptions</a></code> | - Download options including URL and filename |

**Returns:** <code>Promise&lt;<a href="#downloadresult">DownloadResult</a>&gt;</code>

--------------------


### getDownloadStatus(...)

```typescript
getDownloadStatus(options: DownloadStatusOptions) => Promise<DownloadStatus>
```

Get the status of a download

| Param         | Type                                                                    | Description            |
| ------------- | ----------------------------------------------------------------------- | ---------------------- |
| **`options`** | <code><a href="#downloadstatusoptions">DownloadStatusOptions</a></code> | - Download ID to check |

**Returns:** <code>Promise&lt;<a href="#downloadstatus">DownloadStatus</a>&gt;</code>

--------------------


### cancelDownload(...)

```typescript
cancelDownload(options: CancelDownloadOptions) => Promise<CancelDownloadResult>
```

Cancel an ongoing download

| Param         | Type                                                                    | Description             |
| ------------- | ----------------------------------------------------------------------- | ----------------------- |
| **`options`** | <code><a href="#canceldownloadoptions">CancelDownloadOptions</a></code> | - Download ID to cancel |

**Returns:** <code>Promise&lt;<a href="#canceldownloadresult">CancelDownloadResult</a>&gt;</code>

--------------------


### deleteApk(...)

```typescript
deleteApk(options: DeleteApkOptions) => Promise<DeleteApkResult>
```

Delete an APK file from storage

| Param         | Type                                                          | Description           |
| ------------- | ------------------------------------------------------------- | --------------------- |
| **`options`** | <code><a href="#deleteapkoptions">DeleteApkOptions</a></code> | - File path to delete |

**Returns:** <code>Promise&lt;<a href="#deleteapkresult">DeleteApkResult</a>&gt;</code>

--------------------


### startDownloadProgressListener(...)

```typescript
startDownloadProgressListener(options: DownloadProgressOptions) => Promise<DownloadProgressListenerResult>
```

Start monitoring download progress for a specific download
Fires 'downloadProgress' events with real-time progress updates

| Param         | Type                                                                        | Description              |
| ------------- | --------------------------------------------------------------------------- | ------------------------ |
| **`options`** | <code><a href="#downloadprogressoptions">DownloadProgressOptions</a></code> | - Download ID to monitor |

**Returns:** <code>Promise&lt;<a href="#downloadprogresslistenerresult">DownloadProgressListenerResult</a>&gt;</code>

--------------------


### stopDownloadProgressListener()

```typescript
stopDownloadProgressListener() => Promise<DownloadProgressListenerResult>
```

Stop monitoring download progress

**Returns:** <code>Promise&lt;<a href="#downloadprogresslistenerresult">DownloadProgressListenerResult</a>&gt;</code>

--------------------


### addListener('downloadComplete', ...)

```typescript
addListener(eventName: 'downloadComplete', listenerFunc: (data: DownloadCompleteEvent) => void) => Promise<PluginListenerHandle>
```

Add a listener for download completion events

| Param              | Type                                                                                       | Description                       |
| ------------------ | ------------------------------------------------------------------------------------------ | --------------------------------- |
| **`eventName`**    | <code>'downloadComplete'</code>                                                            | - Event name ('downloadComplete') |
| **`listenerFunc`** | <code>(data: <a href="#downloadcompleteevent">DownloadCompleteEvent</a>) =&gt; void</code> | - Callback function               |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

--------------------


### addListener('installationStarted', ...)

```typescript
addListener(eventName: 'installationStarted', listenerFunc: (data: InstallationEvent) => void) => Promise<PluginListenerHandle>
```

Add a listener for installation events

| Param              | Type                                                                               | Description                          |
| ------------------ | ---------------------------------------------------------------------------------- | ------------------------------------ |
| **`eventName`**    | <code>'installationStarted'</code>                                                 | - Event name ('installationStarted') |
| **`listenerFunc`** | <code>(data: <a href="#installationevent">InstallationEvent</a>) =&gt; void</code> | - Callback function                  |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

--------------------


### addListener('downloadProgress', ...)

```typescript
addListener(eventName: 'downloadProgress', listenerFunc: (data: DownloadProgressEvent) => void) => Promise<PluginListenerHandle>
```

Add a listener for download progress events

| Param              | Type                                                                                       | Description                       |
| ------------------ | ------------------------------------------------------------------------------------------ | --------------------------------- |
| **`eventName`**    | <code>'downloadProgress'</code>                                                            | - Event name ('downloadProgress') |
| **`listenerFunc`** | <code>(data: <a href="#downloadprogressevent">DownloadProgressEvent</a>) =&gt; void</code> | - Callback function               |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

--------------------


### removeAllListeners()

```typescript
removeAllListeners() => Promise<void>
```

Remove all listeners for this plugin

--------------------


### Interfaces


#### ApkListResult

| Prop             | Type                   | Description                                |
| ---------------- | ---------------------- | ------------------------------------------ |
| **`files`**      | <code>ApkFile[]</code> | Array of APK files found                   |
| **`count`**      | <code>number</code>    | Total count of files found                 |
| **`filteredBy`** | <code>string</code>    | Package name used for filtering (if any)   |
| **`onlyLatest`** | <code>boolean</code>   | Whether only latest versions were returned |


#### ApkFile

| Prop                 | Type                          | Description                                                                            |
| -------------------- | ----------------------------- | -------------------------------------------------------------------------------------- |
| **`name`**           | <code>string</code>           | File name                                                                              |
| **`uri`**            | <code>string</code>           | Content URI of the file                                                                |
| **`path`**           | <code>string</code>           | Absolute file path (may not be available on all Android versions)                      |
| **`size`**           | <code>number</code>           | File size in bytes                                                                     |
| **`lastModified`**   | <code>number</code>           | Last modified timestamp (milliseconds since epoch)                                     |
| **`source`**         | <code>string</code>           | Source where the file was found (e.g., "Downloads", "MediaStore_Downloads", "Storage") |
| **`appId`**          | <code>string</code>           | Package name / application ID                                                          |
| **`apkVersionName`** | <code>string</code>           | Version name (e.g., "1.0.0")                                                           |
| **`apkVersionCode`** | <code>string \| number</code> | Version code (numeric version)                                                         |


#### ListApksOptions

| Prop             | Type                 | Description                                                                 |
| ---------------- | -------------------- | --------------------------------------------------------------------------- |
| **`appId`**      | <code>string</code>  | Filter results by package name (appId)                                      |
| **`onlyLatest`** | <code>boolean</code> | If true, only returns the latest version of each app (based on versionCode) |


#### DebugScanResult

| Prop                       | Type                         | Description                                    |
| -------------------------- | ---------------------------- | ---------------------------------------------- |
| **`downloadDirectories`**  | <code>DirectoryInfo[]</code> | Information about scanned download directories |
| **`hasStoragePermission`** | <code>boolean</code>         | Whether storage permission is granted          |
| **`externalStorageState`** | <code>string</code>          | External storage state                         |


#### DirectoryInfo

| Prop              | Type                    | Description                        |
| ----------------- | ----------------------- | ---------------------------------- |
| **`path`**        | <code>string</code>     | Directory path                     |
| **`exists`**      | <code>boolean</code>    | Whether directory exists           |
| **`isDirectory`** | <code>boolean</code>    | Whether path is a directory        |
| **`canRead`**     | <code>boolean</code>    | Whether directory is readable      |
| **`totalFiles`**  | <code>number</code>     | Total number of files in directory |
| **`apkCount`**    | <code>number</code>     | Number of APK files found          |
| **`files`**       | <code>FileInfo[]</code> | List of files in directory         |
| **`error`**       | <code>string</code>     | Error message if scan failed       |


#### FileInfo

| Prop                 | Type                 | Description                         |
| -------------------- | -------------------- | ----------------------------------- |
| **`name`**           | <code>string</code>  | File name                           |
| **`isFile`**         | <code>boolean</code> | Whether it's a file (not directory) |
| **`size`**           | <code>number</code>  | File size in bytes                  |
| **`canRead`**        | <code>boolean</code> | Whether file is readable            |
| **`isAPK`**          | <code>boolean</code> | Whether file is an APK              |
| **`apkVersionName`** | <code>string</code>  | APK version name (if available)     |
| **`apkVersionCode`** | <code>number</code>  | APK version code (if available)     |
| **`appId`**          | <code>string</code>  | Package name (if available)         |


#### ScanPathResult

| Prop                  | Type                 | Description                     |
| --------------------- | -------------------- | ------------------------------- |
| **`scannedPath`**     | <code>string</code>  | Path that was scanned           |
| **`pathExists`**      | <code>boolean</code> | Whether the path exists         |
| **`pathIsDirectory`** | <code>boolean</code> | Whether the path is a directory |
| **`pathCanRead`**     | <code>boolean</code> | Whether the path is readable    |


#### ScanPathOptions

| Prop             | Type                 | Description                                          |
| ---------------- | -------------------- | ---------------------------------------------------- |
| **`path`**       | <code>string</code>  | Directory path to scan                               |
| **`appId`**      | <code>string</code>  | Filter results by package name                       |
| **`onlyLatest`** | <code>boolean</code> | If true, only returns the latest version of each app |


#### PermissionResult

| Prop          | Type                 | Description                   |
| ------------- | -------------------- | ----------------------------- |
| **`granted`** | <code>boolean</code> | Whether permission is granted |
| **`message`** | <code>string</code>  | Status message                |


#### PermissionsStatus

| Prop                            | Type                 | Description                                         |
| ------------------------------- | -------------------- | --------------------------------------------------- |
| **`hasStoragePermission`**      | <code>boolean</code> | Whether storage permission is granted               |
| **`hasAllFilesAccess`**         | <code>boolean</code> | Whether "All Files Access" is granted (Android 11+) |
| **`canRequestInstallPackages`** | <code>boolean</code> | Whether app can request package installs            |
| **`sdkVersion`**                | <code>number</code>  | Android SDK version                                 |
| **`readExternalStorage`**       | <code>boolean</code> | Whether READ_EXTERNAL_STORAGE is granted            |


#### InstallResult

| Prop          | Type                | Description                                                                                                                         |
| ------------- | ------------------- | ----------------------------------------------------------------------------------------------------------------------------------- |
| **`status`**  | <code>string</code> | Installation status "installation_started" - Intent launched successfully "permission_required" - Need to enable install permission |
| **`message`** | <code>string</code> | Status message                                                                                                                      |
| **`apkPath`** | <code>string</code> | Path of the APK concerned (only when permission_required)                                                                           |


#### InstallApkOptions

| Prop       | Type                | Description                   |
| ---------- | ------------------- | ----------------------------- |
| **`path`** | <code>string</code> | Absolute path to the APK file |


#### DownloadListenerResult

| Prop             | Type                 | Description                                  |
| ---------------- | -------------------- | -------------------------------------------- |
| **`listening`**  | <code>boolean</code> | Whether listener is active                   |
| **`message`**    | <code>string</code>  | Status message                               |
| **`sdkVersion`** | <code>number</code>  | Android SDK version (when starting listener) |


#### DownloadResult

| Prop             | Type                | Description                                   |
| ---------------- | ------------------- | --------------------------------------------- |
| **`downloadId`** | <code>number</code> | Download ID assigned by DownloadManager       |
| **`fileName`**   | <code>string</code> | Filename being downloaded                     |
| **`url`**        | <code>string</code> | URL being downloaded                          |
| **`message`**    | <code>string</code> | Status message (deprecated, use other fields) |


#### DownloadApkOptions

| Prop              | Type                | Description                                |
| ----------------- | ------------------- | ------------------------------------------ |
| **`path`**        | <code>string</code> | URL of the APK file to download            |
| **`title`**       | <code>string</code> | Title shown in download notification       |
| **`description`** | <code>string</code> | Description shown in download notification |


#### DownloadStatus

| Prop                  | Type                | Description                                                                      |
| --------------------- | ------------------- | -------------------------------------------------------------------------------- |
| **`downloadId`**      | <code>number</code> | Download ID                                                                      |
| **`status`**          | <code>string</code> | Status string: "PENDING", "RUNNING", "PAUSED", "SUCCESSFUL", "FAILED", "UNKNOWN" |
| **`statusCode`**      | <code>number</code> | Status code (numeric)                                                            |
| **`reason`**          | <code>number</code> | Reason code (for failures/pauses)                                                |
| **`bytesDownloaded`** | <code>number</code> | Bytes downloaded so far                                                          |
| **`bytesTotal`**      | <code>number</code> | Total bytes to download                                                          |
| **`uri`**             | <code>string</code> | File URI (available when complete)                                               |
| **`title`**           | <code>string</code> | Download title                                                                   |
| **`progress`**        | <code>number</code> | Download progress percentage (0-100)                                             |


#### DownloadStatusOptions

| Prop             | Type                | Description          |
| ---------------- | ------------------- | -------------------- |
| **`downloadId`** | <code>number</code> | Download ID to check |


#### CancelDownloadResult

| Prop             | Type                 | Description                    |
| ---------------- | -------------------- | ------------------------------ |
| **`removed`**    | <code>boolean</code> | Whether download was removed   |
| **`downloadId`** | <code>number</code>  | Download ID that was cancelled |


#### CancelDownloadOptions

| Prop             | Type                | Description           |
| ---------------- | ------------------- | --------------------- |
| **`downloadId`** | <code>number</code> | Download ID to cancel |


#### DeleteApkResult

| Prop          | Type                 | Description                           |
| ------------- | -------------------- | ------------------------------------- |
| **`deleted`** | <code>boolean</code> | Whether file was deleted successfully |
| **`path`**    | <code>string</code>  | Path that was deleted                 |


#### DeleteApkOptions

| Prop       | Type                | Description                             |
| ---------- | ------------------- | --------------------------------------- |
| **`path`** | <code>string</code> | Absolute path to the APK file to delete |


#### DownloadProgressListenerResult

| Prop             | Type                 | Description                         |
| ---------------- | -------------------- | ----------------------------------- |
| **`listening`**  | <code>boolean</code> | Whether progress listener is active |
| **`downloadId`** | <code>number</code>  | Download ID being monitored         |
| **`message`**    | <code>string</code>  | Status message                      |


#### DownloadProgressOptions

| Prop             | Type                | Description            |
| ---------------- | ------------------- | ---------------------- |
| **`downloadId`** | <code>number</code> | Download ID to monitor |


#### PluginListenerHandle

| Prop         | Type                                      |
| ------------ | ----------------------------------------- |
| **`remove`** | <code>() =&gt; Promise&lt;void&gt;</code> |


#### DownloadCompleteEvent

| Prop             | Type                             | Description                                                  |
| ---------------- | -------------------------------- | ------------------------------------------------------------ |
| **`event`**      | <code>'download_complete'</code> | Event type                                                   |
| **`downloadId`** | <code>number</code>              | Download ID that completed                                   |
| **`timestamp`**  | <code>number</code>              | Timestamp when download completed (milliseconds since epoch) |
| **`apkPath`**    | <code>string</code>              | APK file path (if available)                                 |


#### InstallationEvent

Event fired when installation is triggered (after permission grant or directly)

| Prop          | Type                                                        | Description                                                |
| ------------- | ----------------------------------------------------------- | ---------------------------------------------------------- |
| **`event`**   | <code>'installation_started' \| 'installation_error'</code> | Event type: 'installation_started' or 'installation_error' |
| **`status`**  | <code>'error' \| 'installation_started'</code>              | Installation status                                        |
| **`message`** | <code>string</code>                                         | Status message                                             |
| **`path`**    | <code>string</code>                                         | Path of the APK being installed (optional)                 |


#### DownloadProgressEvent

Event fired during download progress
Updates every ~500ms while download is active

| Prop                  | Type                | Description                                                                     |
| --------------------- | ------------------- | ------------------------------------------------------------------------------- |
| **`downloadId`**      | <code>number</code> | Download ID being monitored                                                     |
| **`bytesDownloaded`** | <code>number</code> | Bytes downloaded so far                                                         |
| **`bytesTotal`**      | <code>number</code> | Total bytes to download                                                         |
| **`progress`**        | <code>number</code> | Download progress percentage (0-100)                                            |
| **`status`**          | <code>string</code> | Current download status: "PENDING", "RUNNING", "PAUSED", "SUCCESSFUL", "FAILED" |
| **`statusCode`**      | <code>number</code> | Status code (numeric)                                                           |
| **`error`**           | <code>string</code> | Error message (only present if status is "FAILED")                              |

</docgen-api>
