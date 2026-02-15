export interface ApkFilesPlugin {
    /**
     * List APK files found in the Downloads directories
     * @param options - Optional filtering options
     */
    listDownloadApks(options?: ListApksOptions): Promise<ApkListResult>;
    /**
     * List all APK files on the device (including Downloads and other directories)
     * @param options - Optional filtering options
     */
    listAllApks(options?: ListApksOptions): Promise<ApkListResult>;
    /**
     * Debug scan to check Download directories and their contents
     */
    debugScan(): Promise<DebugScanResult>;
    /**
     * Scan a specific directory path for APK files
     * @param options - Path and filtering options
     */
    scanSpecificPath(options: ScanPathOptions): Promise<ScanPathResult>;
    /**
     * Request "All Files Access" permission (Android 11+)
     */
    requestAllFilesAccess(): Promise<PermissionResult>;
    /**
     * Check current permissions status
     */
    checkPermissions(): Promise<PermissionsStatus>;
    /**
     * Install an APK file
     * @param options - Installation options with file path
     */
    installApk(options: InstallApkOptions): Promise<InstallResult>;
    /**
     * Start listening for download completion events
     * Returns a promise that resolves when listener is active
     * Fires 'downloadComplete' event when downloads finish
     */
    startDownloadListener(): Promise<DownloadListenerResult>;
    /**
     * Stop listening for download completion events
     */
    stopDownloadListener(): Promise<DownloadListenerResult>;
    /**
     * Download an APK file using Android DownloadManager
     * @param options - Download options including URL and filename
     */
    downloadApk(options: DownloadApkOptions): Promise<DownloadResult>;
    /**
     * Get the status of a download
     * @param options - Download ID to check
     */
    getDownloadStatus(options: DownloadStatusOptions): Promise<DownloadStatus>;
    /**
     * Cancel an ongoing download
     * @param options - Download ID to cancel
     */
    cancelDownload(options: CancelDownloadOptions): Promise<CancelDownloadResult>;
    /**
     * Delete an APK file from storage
     * @param options - File path to delete
     */
    deleteApk(options: DeleteApkOptions): Promise<DeleteApkResult>;
    /**
     * Start monitoring download progress for a specific download
     * Fires 'downloadProgress' events with real-time progress updates
     * @param options - Download ID to monitor
     */
    startDownloadProgressListener(options: DownloadProgressOptions): Promise<DownloadProgressListenerResult>;
    /**
     * Stop monitoring download progress
     */
    stopDownloadProgressListener(): Promise<DownloadProgressListenerResult>;
    /**
     * Add a listener for download completion events
     * @param eventName - Event name ('downloadComplete')
     * @param listenerFunc - Callback function
     */
    addListener(eventName: 'downloadComplete', listenerFunc: (data: DownloadCompleteEvent) => void): Promise<PluginListenerHandle>;
    /**
     * Add a listener for installation events
     * @param eventName - Event name ('installationStarted')
     * @param listenerFunc - Callback function
     */
    addListener(eventName: 'installationStarted', listenerFunc: (data: InstallationEvent) => void): Promise<PluginListenerHandle>;
    /**
     * Add a listener for download progress events
     * @param eventName - Event name ('downloadProgress')
     * @param listenerFunc - Callback function
     */
    addListener(eventName: 'downloadProgress', listenerFunc: (data: DownloadProgressEvent) => void): Promise<PluginListenerHandle>;
    /**
     * Remove all listeners for this plugin
     */
    removeAllListeners(): Promise<void>;
}
export interface ListApksOptions {
    /**
     * Filter results by package name (appId)
     */
    appId?: string;
    /**
     * If true, only returns the latest version of each app
     * (based on versionCode)
     */
    onlyLatest?: boolean;
}
export interface ApkListResult {
    /**
     * Array of APK files found
     */
    files: ApkFile[];
    /**
     * Total count of files found
     */
    count: number;
    /**
     * Package name used for filtering (if any)
     */
    filteredBy?: string;
    /**
     * Whether only latest versions were returned
     */
    onlyLatest?: boolean;
}
export interface ApkFile {
    /**
     * File name
     */
    name: string;
    /**
     * Content URI of the file
     */
    uri: string;
    /**
     * Absolute file path (may not be available on all Android versions)
     */
    path?: string;
    /**
     * File size in bytes
     */
    size: number;
    /**
     * Last modified timestamp (milliseconds since epoch)
     */
    lastModified?: number;
    /**
     * Source where the file was found
     * (e.g., "Downloads", "MediaStore_Downloads", "Storage")
     */
    source: string;
    /**
     * Package name / application ID
     */
    appId?: string;
    /**
     * Version name (e.g., "1.0.0")
     */
    apkVersionName?: string;
    /**
     * Version code (numeric version)
     */
    apkVersionCode?: number | string;
}
export interface DebugScanResult {
    /**
     * Information about scanned download directories
     */
    downloadDirectories: DirectoryInfo[];
    /**
     * Whether storage permission is granted
     */
    hasStoragePermission: boolean;
    /**
     * External storage state
     */
    externalStorageState: string;
}
export interface DirectoryInfo {
    /**
     * Directory path
     */
    path: string;
    /**
     * Whether directory exists
     */
    exists: boolean;
    /**
     * Whether path is a directory
     */
    isDirectory: boolean;
    /**
     * Whether directory is readable
     */
    canRead: boolean;
    /**
     * Total number of files in directory
     */
    totalFiles?: number;
    /**
     * Number of APK files found
     */
    apkCount?: number;
    /**
     * List of files in directory
     */
    files?: FileInfo[];
    /**
     * Error message if scan failed
     */
    error?: string;
}
export interface FileInfo {
    /**
     * File name
     */
    name: string;
    /**
     * Whether it's a file (not directory)
     */
    isFile: boolean;
    /**
     * File size in bytes
     */
    size: number;
    /**
     * Whether file is readable
     */
    canRead: boolean;
    /**
     * Whether file is an APK
     */
    isAPK?: boolean;
    /**
     * APK version name (if available)
     */
    apkVersionName?: string;
    /**
     * APK version code (if available)
     */
    apkVersionCode?: number;
    /**
     * Package name (if available)
     */
    appId?: string;
}
export interface ScanPathOptions {
    /**
     * Directory path to scan
     */
    path: string;
    /**
     * Filter results by package name
     */
    appId?: string;
    /**
     * If true, only returns the latest version of each app
     */
    onlyLatest?: boolean;
}
export interface ScanPathResult extends ApkListResult {
    /**
     * Path that was scanned
     */
    scannedPath: string;
    /**
     * Whether the path exists
     */
    pathExists: boolean;
    /**
     * Whether the path is a directory
     */
    pathIsDirectory: boolean;
    /**
     * Whether the path is readable
     */
    pathCanRead: boolean;
}
export interface PermissionResult {
    /**
     * Whether permission is granted
     */
    granted: boolean;
    /**
     * Status message
     */
    message: string;
}
export interface PermissionsStatus {
    /**
     * Whether storage permission is granted
     */
    hasStoragePermission: boolean;
    /**
     * Whether "All Files Access" is granted (Android 11+)
     */
    hasAllFilesAccess: boolean;
    /**
     * Whether app can request package installs
     */
    canRequestInstallPackages: boolean;
    /**
     * Android SDK version
     */
    sdkVersion: number;
    /**
     * Whether READ_EXTERNAL_STORAGE is granted
     */
    readExternalStorage: boolean;
}
export interface InstallApkOptions {
    /**
     * Absolute path to the APK file
     */
    path: string;
}
export interface InstallResult {
    /**
     * Installation status
     * "installation_started" - Intent launched successfully
     * "permission_required" - Need to enable install permission
     */
    status: string;
    /**
     * Status message
     */
    message: string;
    /**
     * Path of the APK concerned (only when permission_required)
     */
    apkPath?: string;
}
export interface DownloadListenerResult {
    /**
     * Whether listener is active
     */
    listening: boolean;
    /**
     * Status message
     */
    message: string;
    /**
     * Android SDK version (when starting listener)
     */
    sdkVersion?: number;
}
export interface DownloadApkOptions {
    /**
     * URL of the APK file to download
     */
    path: string;
    /**
     * Title shown in download notification
     */
    title?: string;
    /**
     * Description shown in download notification
     */
    description?: string;
}
export interface DownloadResult {
    /**
     * Download ID assigned by DownloadManager
     */
    downloadId: number;
    /**
     * Filename being downloaded
     */
    fileName: string;
    /**
     * URL being downloaded
     */
    url: string;
    /**
     * Status message (deprecated, use other fields)
     */
    message?: string;
}
export interface DownloadStatusOptions {
    /**
     * Download ID to check
     */
    downloadId: number;
}
export interface DownloadStatus {
    /**
     * Download ID
     */
    downloadId: number;
    /**
     * Status string: "PENDING", "RUNNING", "PAUSED", "SUCCESSFUL", "FAILED", "UNKNOWN"
     */
    status: string;
    /**
     * Status code (numeric)
     */
    statusCode: number;
    /**
     * Reason code (for failures/pauses)
     */
    reason: number;
    /**
     * Bytes downloaded so far
     */
    bytesDownloaded: number;
    /**
     * Total bytes to download
     */
    bytesTotal: number;
    /**
     * File URI (available when complete)
     */
    uri?: string;
    /**
     * Download title
     */
    title?: string;
    /**
     * Download progress percentage (0-100)
     */
    progress?: number;
}
export interface CancelDownloadOptions {
    /**
     * Download ID to cancel
     */
    downloadId: number;
}
export interface CancelDownloadResult {
    /**
     * Whether download was removed
     */
    removed: boolean;
    /**
     * Download ID that was cancelled
     */
    downloadId: number;
}
export interface DeleteApkOptions {
    /**
     * Absolute path to the APK file to delete
     */
    path: string;
}
export interface DeleteApkResult {
    /**
     * Whether file was deleted successfully
     */
    deleted: boolean;
    /**
     * Path that was deleted
     */
    path: string;
}
export interface DownloadProgressOptions {
    /**
     * Download ID to monitor
     */
    downloadId: number;
}
export interface DownloadProgressListenerResult {
    /**
     * Whether progress listener is active
     */
    listening: boolean;
    /**
     * Download ID being monitored
     */
    downloadId?: number;
    /**
     * Status message
     */
    message?: string;
}
export interface DownloadCompleteEvent {
    /**
     * Event type
     */
    event: 'download_complete';
    /**
     * Download ID that completed
     */
    downloadId: number;
    /**
     * Timestamp when download completed (milliseconds since epoch)
     */
    timestamp: number;
    /**
     * APK file path (if available)
     */
    apkPath?: string;
}
/**
 * Event fired when installation is triggered (after permission grant or directly)
 */
export interface InstallationEvent {
    /**
     * Event type: 'installation_started' or 'installation_error'
     */
    event: 'installation_started' | 'installation_error';
    /**
     * Installation status
     */
    status: 'installation_started' | 'error';
    /**
     * Status message
     */
    message: string;
    /**
     * Path of the APK being installed (optional)
     */
    path?: string;
}
/**
 * Event fired during download progress
 * Updates every ~500ms while download is active
 */
export interface DownloadProgressEvent {
    /**
     * Download ID being monitored
     */
    downloadId: number;
    /**
     * Bytes downloaded so far
     */
    bytesDownloaded: number;
    /**
     * Total bytes to download
     */
    bytesTotal: number;
    /**
     * Download progress percentage (0-100)
     */
    progress: number;
    /**
     * Current download status: "PENDING", "RUNNING", "PAUSED", "SUCCESSFUL", "FAILED"
     */
    status: string;
    /**
     * Status code (numeric)
     */
    statusCode: number;
    /**
     * Error message (only present if status is "FAILED")
     */
    error?: string;
}
export interface PluginListenerHandle {
    remove: () => Promise<void>;
}
