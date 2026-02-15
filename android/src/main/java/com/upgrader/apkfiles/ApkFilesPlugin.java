package com.upgrader.apkfiles;

import android.webkit.URLUtil;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo; // Import PackageInfo
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider; // For FileProvider

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;
import android.content.SharedPreferences;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import androidx.core.content.ContextCompat;

@CapacitorPlugin(name = "ApkFiles", permissions = {
        @Permission(alias = "storage", strings = {
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.MANAGE_EXTERNAL_STORAGE
        }),
        // Add REQUEST_INSTALL_PACKAGES for direct installation (Android 8.0+)
        @Permission(alias = "install", strings = {
                android.Manifest.permission.REQUEST_INSTALL_PACKAGES
        })
})
public class ApkFilesPlugin extends Plugin {

    private static final String TAG = "ApkFilesPlugin";
    private String pendingMethodName = "";

    
    private BroadcastReceiver downloadCompleteReceiver = null;
private PluginCall downloadListenerCall = null;
private android.os.Handler permissionCheckHandler = null;
private Runnable permissionCheckRunnable = null;

// Declare these variables at the class level to make them accessible
    // and to store the pending call if the user needs to grant permission.
    private PluginCall currentInstallCall = null;
    private String apkPathForPendingInstall = null;
    
    // A constant for the SharedPreferences key
    private static final String PREF_NAME = "apk_installer_prefs";
    private static final String PREF_PENDING_APK_PATH = "pending_apk_path";
    private static final String PREF_LAST_DOWNLOAD_ID = "last_download_id";


    @PluginMethod
    public void listDownloadApks(PluginCall call) {
        String filterAppId = call.getString("appId", null);
        boolean onlyLatest = call.getBoolean("onlyLatest", false);

        // On Android 11+, check for All Files Access first
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            requestAllFilesAccessFirst(call, "listDownloadApks");
            return;
        }

        if (!checkStoragePermission()) {
            requestStoragePermissions(call, "listDownloadApks");
            return;
        }

        List<JSObject> apkFiles = new ArrayList<>();
        Set<String> foundFiles = new HashSet<>();

        scanDownloadDirectories(apkFiles, foundFiles);
        scanDownloadsMediaStore(apkFiles, foundFiles);

        // Add APK info to the results and filter if needed
        addApkInfoToFiles(apkFiles, filterAppId);

        // Keep only latest versions if requested
        if (onlyLatest) {
            apkFiles = keepOnlyLatestVersions(apkFiles);
        }

        JSArray files = new JSArray(apkFiles);
        JSObject ret = new JSObject();
        ret.put("files", files);
        ret.put("count", apkFiles.size());
        if (filterAppId != null) {
            ret.put("filteredBy", filterAppId);
        }
        if (onlyLatest) {
            ret.put("onlyLatest", true);
        }

        Log.d(TAG, "Downloads scan complete: " + apkFiles.size() + " APK files found");
        call.resolve(ret);
    }

    @PluginMethod
    public void listAllApks(PluginCall call) {
        String filterAppId = call.getString("appId", null);
        boolean onlyLatest = call.getBoolean("onlyLatest", false);

        // On Android 11+, check for All Files Access first
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            requestAllFilesAccessFirst(call, "listAllApks");
            return;
        }

        if (!checkStoragePermission()) {
            requestStoragePermissions(call, "listAllApks");
            return;
        }

        List<JSObject> apkFiles = new ArrayList<>();
        Set<String> foundFiles = new HashSet<>();

        scanDownloadDirectories(apkFiles, foundFiles);
        scanCommonDirectories(apkFiles, foundFiles);
        scanAllMediaStore(apkFiles, foundFiles);

        // Add APK info to the results and filter if needed
        addApkInfoToFiles(apkFiles, filterAppId);

        // Keep only latest versions if requested
        if (onlyLatest) {
            apkFiles = keepOnlyLatestVersions(apkFiles);
        }

        JSArray files = new JSArray(apkFiles);
        JSObject ret = new JSObject();
        ret.put("files", files);
        ret.put("count", apkFiles.size());
        if (filterAppId != null) {
            ret.put("filteredBy", filterAppId);
        }
        if (onlyLatest) {
            ret.put("onlyLatest", true);
        }

        Log.d(TAG, "Full scan complete: " + apkFiles.size() + " APK files found");
        call.resolve(ret);
    }

    @PluginMethod
    public void debugScan(PluginCall call) {
        JSObject result = new JSObject();
        JSArray debugInfo = new JSArray();

        String[] downloadPaths = {
                "/storage/emulated/0/Download",
                "/storage/emulated/0/Downloads",
                "/sdcard/Download",
                "/sdcard/Downloads"
        };

        // Add system Downloads directory
        try {
            File sysDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (sysDownloads != null) {
                String sysPath = sysDownloads.getAbsolutePath();
                String[] allPaths = new String[downloadPaths.length + 1];
                System.arraycopy(downloadPaths, 0, allPaths, 0, downloadPaths.length);
                allPaths[downloadPaths.length] = sysPath;
                downloadPaths = allPaths;
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not get system downloads directory", e);
        }

        for (String path : downloadPaths) {
            JSObject pathInfo = new JSObject();
            pathInfo.put("path", path);

            File dir = new File(path);
            pathInfo.put("exists", dir.exists());
            pathInfo.put("isDirectory", dir.isDirectory());
            pathInfo.put("canRead", dir.canRead());

            if (dir.exists() && dir.isDirectory()) {
                try {
                    File[] files = dir.listFiles();
                    if (files != null) {
                        pathInfo.put("totalFiles", files.length);

                        JSArray filesList = new JSArray();
                        int apkCount = 0;

                        for (File file : files) {
                            JSObject fileInfo = new JSObject();
                            fileInfo.put("name", file.getName());
                            fileInfo.put("isFile", file.isFile());
                            fileInfo.put("size", file.length());
                            fileInfo.put("canRead", file.canRead());

                            if (file.getName().toLowerCase().endsWith(".apk")) {
                                apkCount++;
                                fileInfo.put("isAPK", true);
                                // Add APK info here directly for debug
                                JSObject apkDetails = getApkDetails(file.getAbsolutePath());
                                if (apkDetails != null) {
                                    fileInfo.put("apkVersionName", apkDetails.getString("versionName"));
                                    fileInfo.put("apkVersionCode", apkDetails.getLong("versionCode"));
                                    fileInfo.put("appId", apkDetails.getString("appId"));
                                }
                            }

                            filesList.put(fileInfo);
                        }

                        pathInfo.put("apkCount", apkCount);
                        pathInfo.put("files", filesList);
                    } else {
                        pathInfo.put("error", "Could not list files - permission denied");
                    }
                } catch (Exception e) {
                    pathInfo.put("error", e.getMessage());
                }
            }

            debugInfo.put(pathInfo);
        }

        result.put("downloadDirectories", debugInfo);
        result.put("hasStoragePermission", checkStoragePermission());
        result.put("externalStorageState", Environment.getExternalStorageState());

        call.resolve(result);
    }

    @PluginMethod
    public void scanSpecificPath(PluginCall call) {
        String path = call.getString("path", "/storage/emulated/0/Download");
        String filterAppId = call.getString("appId", null);
        boolean onlyLatest = call.getBoolean("onlyLatest", false);

        if (!checkStoragePermission()) {
            requestStoragePermissions(call, "scanSpecificPath");
            return;
        }

        List<JSObject> apkFiles = new ArrayList<>();
        Set<String> foundFiles = new HashSet<>();

        File directory = new File(path);
        scanDirectory(directory, apkFiles, foundFiles, "Manual", false);

        // Add APK info to the results and filter if needed
        addApkInfoToFiles(apkFiles, filterAppId);

        // Keep only latest versions if requested
        if (onlyLatest) {
            apkFiles = keepOnlyLatestVersions(apkFiles);
        }

        JSArray files = new JSArray(apkFiles);
        JSObject ret = new JSObject();
        ret.put("files", files);
        ret.put("count", apkFiles.size());
        ret.put("scannedPath", path);
        ret.put("pathExists", directory.exists());
        ret.put("pathIsDirectory", directory.isDirectory());
        ret.put("pathCanRead", directory.canRead());
        if (filterAppId != null) {
            ret.put("filteredBy", filterAppId);
        }
        if (onlyLatest) {
            ret.put("onlyLatest", true);
        }

        call.resolve(ret);
    }

    @PluginMethod
    public void requestAllFilesAccess(PluginCall call) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                JSObject result = new JSObject();
                result.put("granted", true);
                result.put("message", "All files access already granted");
                call.resolve(result);
            } else {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(Uri.parse("package:" + getContext().getPackageName()));
                    getActivity().startActivity(intent);

                    JSObject result = new JSObject();
                    result.put("granted", false);
                    result.put("message", "Redirected to settings. Please enable 'Allow management of all files'");
                    call.resolve(result);
                } catch (Exception e) {
                    // Fallback to general all files access settings
                    try {
                        Intent fallbackIntent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                        getActivity().startActivity(fallbackIntent);

                        JSObject result = new JSObject();
                        result.put("granted", false);
                        result.put("message", "Redirected to general all files access settings");
                        call.resolve(result);
                    } catch (Exception e2) {
                        call.reject("Failed to open all files access settings", e2);
                    }
                }
            }
        } else {
            JSObject result = new JSObject();
            result.put("granted", true);
            result.put("message", "All files access not required on Android 10 and below");
            call.resolve(result);
        }
    }

    private void requestAllFilesAccessFirst(PluginCall call, String methodName) {
        JSObject result = new JSObject();
        result.put("needsAllFilesAccess", true);
        result.put("message",
                "This app needs 'Allow management of all files' permission to scan for APK files on Android 11+");
        result.put("nextStep", "Call requestAllFilesAccess() method");

        call.reject("All Files Access permission required", result.toString());
    }

    @PluginMethod
    public void checkPermissions(PluginCall call) {
        JSObject result = new JSObject();

        Context context = getContext();
        boolean hasStoragePermission = checkStoragePermission();
        boolean hasAllFilesAccess = checkAllFilesAccess();
        boolean canRequestInstallPackages = checkRequestInstallPackagesPermission();

        result.put("hasStoragePermission", hasStoragePermission);
        result.put("hasAllFilesAccess", hasAllFilesAccess);
        result.put("canRequestInstallPackages", canRequestInstallPackages);
        result.put("sdkVersion", Build.VERSION.SDK_INT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int readPermission = ActivityCompat.checkSelfPermission(context,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE);
            result.put("readExternalStorage", readPermission == PackageManager.PERMISSION_GRANTED);
        } else {
            result.put("readExternalStorage", true);
        }

        call.resolve(result);
    }

    private boolean checkStoragePermission() {
        // On Android 11+, check for All Files Access first
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            boolean hasAllFiles = Environment.isExternalStorageManager();
            Log.d(TAG, "Android 11+ - All Files Access: " + (hasAllFiles ? "GRANTED" : "DENIED"));
            if (hasAllFiles) {
                return true;
            }
        }

        // Fallback to regular storage permission
        Context context = getContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permission = ActivityCompat.checkSelfPermission(context,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE);

            boolean granted = permission == PackageManager.PERMISSION_GRANTED;
            Log.d(TAG, "Storage permission check: " + (granted ? "GRANTED" : "DENIED"));
            return granted;
        }

        Log.d(TAG, "Pre-M device, permissions granted by default");
        return true;
    }

    private boolean checkAllFilesAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        }
        return true; // Not applicable on older versions
    }

    private boolean checkRequestInstallPackagesPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // Android 8.0 (API 26)
            return getContext().getPackageManager().canRequestPackageInstalls();
        }
        return true; // Older Android versions don't need this explicit permission check here.
    }

    private void requestStoragePermissions(PluginCall call, String methodName) {
        Log.d(TAG, "Requesting storage permissions for method: " + methodName);
        this.pendingMethodName = methodName;
        requestPermissionForAlias("storage", call, "storagePermissionCallback");
    }

    @PermissionCallback
    private void storagePermissionCallback(PluginCall call) {
        String originalMethod = this.pendingMethodName;

        if (checkStoragePermission()) {
            Log.d(TAG, "Permission granted, executing: " + originalMethod);

            if ("listAllApks".equals(originalMethod)) {
                listAllApks(call);
            } else if ("scanSpecificPath".equals(originalMethod)) {
                scanSpecificPath(call);
            } else {
                listDownloadApks(call);
            }
        } else {
            Log.e(TAG, "Storage permission denied");
            call.reject("Storage permission is required to scan for APK files");
        }

        this.pendingMethodName = "";
    }

    /**
     * Keeps only the latest version of each app based on versionCode.
     * If multiple APKs have the same appId, only the one with the highest
     * versionCode is kept.
     * 
     * @param apkFiles List of APK file objects
     * @return Filtered list containing only the latest version of each app
     */
    private List<JSObject> keepOnlyLatestVersions(List<JSObject> apkFiles) {
        // Map to store the latest version of each appId
        java.util.Map<String, JSObject> latestVersionMap = new java.util.HashMap<>();

        for (JSObject apkFile : apkFiles) {
            String appId = apkFile.getString("appId");

            // Skip if appId is missing or N/A
            if (appId == null || "N/A".equals(appId)) {
                continue;
            }

            // Get version code, default to -1 if not found or N/A
            long versionCode = -1;
            try {
                Object versionCodeObj = apkFile.opt("apkVersionCode");
                if (versionCodeObj instanceof Number) {
                    versionCode = ((Number) versionCodeObj).longValue();
                } else if (versionCodeObj instanceof String && !"N/A".equals(versionCodeObj)) {
                    versionCode = Long.parseLong((String) versionCodeObj);
                }
            } catch (Exception e) {
                Log.w(TAG, "Could not parse versionCode for appId: " + appId);
            }

            // Check if we already have this appId
            if (latestVersionMap.containsKey(appId)) {
                JSObject existingApk = latestVersionMap.get(appId);
                long existingVersionCode = -1;

                try {
                    Object existingVersionCodeObj = existingApk.opt("apkVersionCode");
                    if (existingVersionCodeObj instanceof Number) {
                        existingVersionCode = ((Number) existingVersionCodeObj).longValue();
                    } else if (existingVersionCodeObj instanceof String && !"N/A".equals(existingVersionCodeObj)) {
                        existingVersionCode = Long.parseLong((String) existingVersionCodeObj);
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Could not parse existing versionCode for appId: " + appId);
                }

                // Keep the APK with higher version code
                if (versionCode > existingVersionCode) {
                    latestVersionMap.put(appId, apkFile);
                    Log.d(TAG, "Updated latest version for " + appId + " to versionCode: " + versionCode);
                }
            } else {
                // First time seeing this appId
                latestVersionMap.put(appId, apkFile);
                Log.d(TAG, "Added " + appId + " with versionCode: " + versionCode);
            }
        }

        List<JSObject> result = new ArrayList<>(latestVersionMap.values());
        Log.d(TAG, "keepOnlyLatestVersions: Reduced from " + apkFiles.size() + " to " + result.size() + " APKs");
        return result;
    }

    /**
     * Helper method to add APK info and optionally filter by appId
     * 
     * @param apkFiles    List of APK file objects to enrich with metadata
     * @param filterAppId Optional package name to filter by (null = no filter)
     */
    private void addApkInfoToFiles(List<JSObject> apkFiles, String filterAppId) {
        List<JSObject> itemsToRemove = new ArrayList<>();

        for (JSObject apkFile : apkFiles) {
            String path = apkFile.getString("path");
            if (path != null) {
                JSObject apkDetails = getApkDetails(path);
                if (apkDetails != null) {
                    // Safely get appId (package name)
                    String appId = apkDetails.getString("appId");
                    if (appId != null) {
                        apkFile.put("appId", appId);

                        // Check if we should filter this item out
                        if (filterAppId != null && !filterAppId.equals(appId)) {
                            itemsToRemove.add(apkFile);
                            continue; // Skip adding other details for filtered items
                        }
                    } else {
                        apkFile.put("appId", "N/A");
                    }

                    // Safely get versionName
                    String versionName = apkDetails.getString("versionName");
                    if (versionName != null) {
                        apkFile.put("apkVersionName", versionName);
                    } else {
                        apkFile.put("apkVersionName", "N/A");
                    }

                    // Safely get versionCode
                    long versionCode = apkDetails.optLong("versionCode", -1);
                    if (versionCode != -1) {
                        apkFile.put("apkVersionCode", versionCode);
                    } else {
                        apkFile.put("apkVersionCode", "N/A");
                    }

                } else {
                    Log.w(TAG, "Could not get APK details for path: " + path);
                    apkFile.put("appId", "N/A");
                    apkFile.put("apkVersionName", "N/A");
                    apkFile.put("apkVersionCode", "N/A");

                    // If filtering and we can't determine appId, remove it
                    if (filterAppId != null) {
                        itemsToRemove.add(apkFile);
                    }
                }
            } else {
                Log.w(TAG, "APK file path is null for an entry.");
                apkFile.put("appId", "N/A");
                apkFile.put("apkVersionName", "N/A");
                apkFile.put("apkVersionCode", "N/A");

                // If filtering, remove items without valid paths
                if (filterAppId != null) {
                    itemsToRemove.add(apkFile);
                }
            }
        }

        // Remove filtered items
        apkFiles.removeAll(itemsToRemove);

        if (filterAppId != null) {
            Log.d(TAG, "Filtered APKs by appId: " + filterAppId + ", remaining: " + apkFiles.size());
        }
    }

    /**
     * Extracts package name (appId), version name and code from an APK file.
     * 
     * @param apkFilePath The absolute path to the APK file.
     * @return A JSObject containing "appId", "versionName" and "versionCode", or
     *         null if info cannot be extracted.
     */
    private JSObject getApkDetails(String apkFilePath) {
        PackageManager pm = getContext().getPackageManager();
        PackageInfo packageInfo = pm.getPackageArchiveInfo(apkFilePath, 0);

        if (packageInfo != null) {
            JSObject apkDetails = new JSObject();
            apkDetails.put("appId", packageInfo.packageName); // This is the package name / appId
            apkDetails.put("versionName", packageInfo.versionName);
            // versionCode is long in modern Android versions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) { // Android 9 (Pie)
                apkDetails.put("versionCode", packageInfo.getLongVersionCode());
            } else {
                apkDetails.put("versionCode", (long) packageInfo.versionCode);
            }
            return apkDetails;
        }
        return null;
    }

    private void scanDownloadDirectories(List<JSObject> apkFiles, Set<String> foundFiles) {
        List<String> downloadPaths = new ArrayList<>();

        // Add system Downloads directory
        try {
            File sysDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (sysDownloads != null) {
                downloadPaths.add(sysDownloads.getAbsolutePath());
                Log.d(TAG, "System Downloads directory: " + sysDownloads.getAbsolutePath());
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not get system downloads directory", e);
        }

        // Add common download paths
        String[] commonDownloadPaths = {
                "/storage/emulated/0/Download",
                "/storage/emulated/0/Downloads",
                "/sdcard/Download",
                "/sdcard/Downloads"
        };

        for (String path : commonDownloadPaths) {
            downloadPaths.add(path);
        }

        Log.d(TAG, "Scanning " + downloadPaths.size() + " download directories");
        for (String path : downloadPaths) {
            Log.d(TAG, "Checking path: " + path);
            File dir = new File(path);
            Log.d(TAG, "  Exists: " + dir.exists() + ", IsDirectory: " + dir.isDirectory() + ", CanRead: "
                    + dir.canRead());

            scanDirectory(dir, apkFiles, foundFiles, "Downloads", false);
        }

        Log.d(TAG, "Download directories scan complete. Found " + apkFiles.size() + " APK files so far.");
    }

    private void scanCommonDirectories(List<JSObject> apkFiles, Set<String> foundFiles) {
        String[] commonPaths = {
                "/storage/emulated/0/Documents",
                "/storage/emulated/0/DCIM",
                "/storage/emulated/0/Pictures",
                "/storage/emulated/0/Music",
                "/storage/emulated/0/Movies",
                "/sdcard/Documents",
                "/sdcard"
        };

        for (String path : commonPaths) {
            scanDirectory(new File(path), apkFiles, foundFiles, "Storage", false);
        }

        // Recursive scan of external storage root (limited depth)
        File externalStorage = Environment.getExternalStorageDirectory();
        if (externalStorage != null && externalStorage.exists()) {
            scanDirectory(externalStorage, apkFiles, foundFiles, "External", true);
        }
    }

    private void scanDirectory(File directory, List<JSObject> apkFiles, Set<String> foundFiles, String source,
            boolean recursive) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return;
        }

        try {
            File[] files = directory.listFiles();
            if (files == null) {
                return;
            }

            Log.d(TAG, "Scanning: " + directory.getAbsolutePath() + " (" + files.length + " items)");

            for (File file : files) {
                if (file.isFile() && file.getName().toLowerCase().endsWith(".apk")) {
                    String filePath = file.getAbsolutePath();

                    if (!foundFiles.contains(filePath)) {
                        foundFiles.add(filePath);

                        JSObject fileObj = new JSObject();
                        fileObj.put("name", file.getName());
                        fileObj.put("uri", Uri.fromFile(file).toString());
                        fileObj.put("path", filePath);
                        fileObj.put("size", file.length());
                        fileObj.put("lastModified", file.lastModified());
                        fileObj.put("source", source);
                        apkFiles.add(fileObj);

                        Log.d(TAG, "Found APK: " + file.getName() + " in " + filePath);
                    }
                } else if (file.isDirectory() && recursive) {
                    // Limited recursive scan
                    String dirName = file.getName().toLowerCase();
                    if (!dirName.startsWith(".") &&
                            !dirName.equals("android") &&
                            !dirName.equals("data") &&
                            !file.getAbsolutePath().contains("/Android/data/") &&
                            directory.getAbsolutePath().split("/").length < 6) {
                        scanDirectory(file, apkFiles, foundFiles, source, true);
                    }
                }
            }
        } catch (SecurityException e) {
            Log.w(TAG, "Permission denied: " + directory.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "Error scanning: " + directory.getAbsolutePath(), e);
        }
    }

    private void scanDownloadsMediaStore(List<JSObject> apkFiles, Set<String> foundFiles) {
        try {
            Uri collection;
            String[] projection;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
                projection = new String[] {
                        MediaStore.Downloads._ID,
                        MediaStore.Downloads.DISPLAY_NAME,
                        MediaStore.Downloads.SIZE,
                };
            } else {
                collection = MediaStore.Files.getContentUri("external");
                projection = new String[] {
                        MediaStore.Files.FileColumns._ID,
                        MediaStore.Files.FileColumns.DISPLAY_NAME,
                        MediaStore.Files.FileColumns.DATA,
                        MediaStore.Files.FileColumns.SIZE
                };
            }

            String selection = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    ? MediaStore.Downloads.DISPLAY_NAME + " LIKE ?"
                    : MediaStore.Files.FileColumns.DISPLAY_NAME + " LIKE ?";
            String[] selectionArgs = new String[] { "%.apk" };

            Cursor cursor = getContext().getContentResolver().query(
                    collection, projection, selection, selectionArgs, null);

            if (cursor != null) {
                int nameColumn = cursor.getColumnIndex(
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ? MediaStore.Downloads.DISPLAY_NAME
                                : MediaStore.Files.FileColumns.DISPLAY_NAME);

                int idColumn = cursor
                        .getColumnIndex(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ? MediaStore.Downloads._ID
                                : MediaStore.Files.FileColumns._ID);

                int dataColumn = (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                        ? cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)
                        : -1;

                int sizeColumn = cursor
                        .getColumnIndex(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ? MediaStore.Downloads.SIZE
                                : MediaStore.Files.FileColumns.SIZE);

                if (nameColumn == -1 || idColumn == -1) {
                    Log.w(TAG, "Required columns not found in MediaStore Downloads");
                    cursor.close();
                    return;
                }

                while (cursor.moveToNext()) {
                    String fileName = cursor.getString(nameColumn);
                    long id = cursor.getLong(idColumn);
                    long size = (sizeColumn != -1) ? cursor.getLong(sizeColumn) : 0;
                    String filePath = (dataColumn != -1) ? cursor.getString(dataColumn) : null;

                    if (fileName != null && fileName.toLowerCase().endsWith(".apk")) {
                        String uniqueKey = filePath != null ? filePath : ("mediastore_downloads_" + id);
                        if (!foundFiles.contains(uniqueKey)) {
                            foundFiles.add(uniqueKey);

                            Uri fileUri = Uri.withAppendedPath(collection, String.valueOf(id));

                            JSObject file = new JSObject();
                            file.put("name", fileName);
                            file.put("uri", fileUri.toString());
                            file.put("size", size);
                            file.put("source", "MediaStore_Downloads");
                            if (filePath != null) {
                                file.put("path", filePath);
                            } else {
                                Log.w(TAG, "Absolute path not available from MediaStore for: " + fileName + ". URI: "
                                        + fileUri);
                            }
                            apkFiles.add(file);
                        }
                    }
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error scanning Downloads with MediaStore", e);
        }
    }

    private void scanAllMediaStore(List<JSObject> apkFiles, Set<String> foundFiles) {
        try {
            Uri collection = MediaStore.Files.getContentUri("external");
            String[] projection = {
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
                    MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.SIZE,
                    MediaStore.Files.FileColumns.DATE_MODIFIED
            };

            String selection = MediaStore.Files.FileColumns.DISPLAY_NAME + " LIKE ?";
            String[] selectionArgs = new String[] { "%.apk" };

            Cursor cursor = getContext().getContentResolver().query(
                    collection, projection, selection, selectionArgs, null);

            if (cursor != null) {
                int nameColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME);
                int dataColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                int sizeColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE);
                int idColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID);
                int dateColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED);

                if (nameColumn == -1 || idColumn == -1) {
                    Log.w(TAG, "Required columns not found in MediaStore Files");
                    cursor.close();
                    return;
                }

                while (cursor.moveToNext()) {
                    String fileName = cursor.getString(nameColumn);
                    String filePath = (dataColumn != -1) ? cursor.getString(dataColumn) : null;
                    long fileSize = (sizeColumn != -1) ? cursor.getLong(sizeColumn) : 0;
                    long id = cursor.getLong(idColumn);
                    long dateModified = (dateColumn != -1) ? cursor.getLong(dateColumn) : 0;

                    if (fileName != null && fileName.toLowerCase().endsWith(".apk")) {
                        String uniqueKey = filePath != null ? filePath : ("mediastore_" + id);

                        if (!foundFiles.contains(uniqueKey)) {
                            foundFiles.add(uniqueKey);

                            Uri fileUri = Uri.withAppendedPath(collection, String.valueOf(id));

                            JSObject file = new JSObject();
                            file.put("name", fileName);
                            file.put("uri", fileUri.toString());
                            file.put("path", filePath);
                            file.put("size", fileSize);
                            file.put("lastModified", dateModified * 1000);
                            file.put("source", "MediaStore_All");
                            apkFiles.add(file);
                        }
                    }
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error scanning all files with MediaStore", e);
        }
    }

    // --- Install APK Method ---
private static final int INSTALL_REQUEST_CODE = 1234;
private static final int INSTALL_PERMISSION_REQUEST_CODE = 5678;
private static final String PREF_WAITING_FOR_PERMISSION = "waiting_for_permission";

// Store the pending call and file path
private PluginCall pendingInstallCall;
private String pendingApkPath;
private static final int REQUEST_INSTALL_PERMISSION = 9999;


@PluginMethod
public void installApk(PluginCall call) {
    String apkFilePath = call.getString("path");
    if (apkFilePath == null) {
        call.reject("Must provide 'path'");
        return;
    }

    File apkFile = new File(apkFilePath);
    if (!apkFile.exists()) {
        call.reject("APK not found: " + apkFilePath);
        return;
    }

    Log.d(TAG, "installApk called with path: " + apkFilePath);

    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
        !getContext().getPackageManager().canRequestPackageInstalls()) {

        Log.d(TAG, "Permission REQUEST_INSTALL_PACKAGES required");

        try {
            SharedPreferences prefs = getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString(PREF_PENDING_APK_PATH, apkFilePath).apply();

            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
            intent.setData(Uri.parse("package:" + getContext().getPackageName()));
            getActivity().startActivity(intent);

            JSObject ret = new JSObject();
            ret.put("status", "permission_required");
            ret.put("message", "User redirected to settings");
            call.resolve(ret);
            return;

        } catch (Exception e) {
            Log.e(TAG, "Failed to open settings: " + e.getMessage());
            call.reject("Failed to open settings: " + e.getMessage());
            return;
        }
    }

    
    Log.d(TAG, "Permission OK, installing");
    startInstallIntent(apkFile, call);
}



@Override
protected void handleOnResume() {
    super.handleOnResume();
    Log.d(TAG, "handleOnResume called");

    SharedPreferences prefs = getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    String savedApkPath = prefs.getString(PREF_PENDING_APK_PATH, null);
    
    if (savedApkPath == null) {
        Log.d(TAG, "No pending APK");
        return;
    }

    Log.d(TAG, "Found pending APK: " + savedApkPath);
    
   
    startPermissionPolling(savedApkPath);
}

@Override
protected void handleOnPause() {
    super.handleOnPause();
    Log.d(TAG, "handleOnPause called");
    
    
    stopPermissionPolling();
}

@Override
protected void handleOnDestroy() {
    super.handleOnDestroy();
    Log.d(TAG, "handleOnDestroy called");
    
    
    stopPermissionPolling();
    stopDownloadProgressListener(null);
}

private void startPermissionPolling(final String apkPath) {
    if (permissionCheckHandler != null) {
        Log.d(TAG, "Polling already active");
        return;
    }

    permissionCheckHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    final int[] attemptCount = {0};
    final int maxAttempts = 10;

    permissionCheckRunnable = new Runnable() {
        @Override
        public void run() {
            attemptCount[0]++;
            Log.d(TAG, "Permission check attempt: " + attemptCount[0] + "/" + maxAttempts);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (getContext().getPackageManager().canRequestPackageInstalls()) {
                    Log.d(TAG, "✅ Permission granted!");
                    
                   
                    stopPermissionPolling();
                    
                   
                    SharedPreferences prefs = getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                    prefs.edit().remove(PREF_PENDING_APK_PATH).apply();
                    
                    
                    File apkFile = new File(apkPath);
                    if (apkFile.exists()) {
                        startInstallIntentWithNotification(apkFile);
                    } else {
                        Log.e(TAG, "APK not found: " + apkPath);
                        notifyInstallationError("APK file not found");
                    }
                    
                    return;
                }
            }

            
            if (attemptCount[0] < maxAttempts) {
                permissionCheckHandler.postDelayed(this, 500);
            } else {
                Log.d(TAG, "❌ Max attempts reached, permission denied");
                stopPermissionPolling();
                
                SharedPreferences prefs = getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                prefs.edit().remove(PREF_PENDING_APK_PATH).apply();
                
                notifyInstallationError("Installation permission denied by user");
            }
        }
    };

   
    permissionCheckHandler.post(permissionCheckRunnable);
}

private void stopPermissionPolling() {
    if (permissionCheckHandler != null && permissionCheckRunnable != null) {
        Log.d(TAG, "Stopping permission polling");
        permissionCheckHandler.removeCallbacks(permissionCheckRunnable);
        permissionCheckHandler = null;
        permissionCheckRunnable = null;
    }
}




private void startInstallIntentWithNotification(File apkFile) {
    try {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        
        Uri fileUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            fileUri = FileProvider.getUriForFile(
                getContext(),
                getContext().getPackageName() + ".fileprovider",
                apkFile
            );
        } else {
            fileUri = Uri.fromFile(apkFile);
        }

        intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
        getActivity().startActivity(intent);

        Log.d(TAG, "Installation launched: " + apkFile.getAbsolutePath());

        JSObject result = new JSObject();
        result.put("event", "installation_started");
        result.put("status", "installation_started");
        result.put("message", "Installation dialog opened");
        result.put("path", apkFile.getAbsolutePath());
        notifyListeners("installationStarted", result);

    } catch (Exception e) {
        Log.e(TAG, "Failed to launch installation: " + e.getMessage(), e);
        notifyInstallationError("Failed to launch installation: " + e.getMessage());
    }
}

private void startInstallIntent(File apkFile, PluginCall call) {
    try {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        
        Uri fileUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            fileUri = FileProvider.getUriForFile(
                getContext(),
                getContext().getPackageName() + ".fileprovider",
                apkFile
            );
        } else {
            fileUri = Uri.fromFile(apkFile);
        }

        intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
        getActivity().startActivity(intent);

        Log.d(TAG, "Installation launched: " + apkFile.getAbsolutePath());

        JSObject ret = new JSObject();
        ret.put("status", "installation_started");
        ret.put("message", "Installation dialog opened");
        call.resolve(ret);

    } catch (Exception e) {
        Log.e(TAG, "Failed to launch installation: " + e.getMessage(), e);
        call.reject("Failed to launch installation: " + e.getMessage());
    }
}

private void notifyInstallationError(String errorMessage) {
    Log.e(TAG, "Installation error: " + errorMessage);
    JSObject result = new JSObject();
    result.put("event", "installation_error");
    result.put("status", "error");
    result.put("message", errorMessage);
    notifyListeners("installationStarted", result);
}




@PluginMethod(returnType = PluginMethod.RETURN_CALLBACK)
public void startDownloadListener(PluginCall call) {
    call.setKeepAlive(true);
    this.downloadListenerCall = call;

    if (downloadCompleteReceiver == null) {
        downloadCompleteReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                    
                    Log.d(TAG, "Download complete: " + downloadId);
                    
                    if (downloadId != -1) {
                        String apkPath = getDownloadedApkPath(downloadId);
                        
                        JSObject result = new JSObject();
                        result.put("event", "download_complete");
                        result.put("downloadId", downloadId);
                        result.put("timestamp", System.currentTimeMillis());
                        if (apkPath != null) {
                            result.put("apkPath", apkPath);
                        }
                        
                        notifyListeners("downloadComplete", result);
                        
                        if (downloadListenerCall != null) {
                            downloadListenerCall.resolve(result);
                        }
                    }
                }
            }
        };
        
        try {
            IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
            
            // ✅ SOLUTION: Use ContextCompat.registerReceiver() for all versions
            ContextCompat.registerReceiver(
                getContext(),
                downloadCompleteReceiver,
                filter,
                ContextCompat.RECEIVER_EXPORTED  // ✅ For system broadcasts like DOWNLOAD_COMPLETE
            );
            
            Log.d(TAG, "Download listener registered successfully");
            
            JSObject result = new JSObject();
            result.put("listening", true);
            result.put("message", "Download listener started");
            call.resolve(result);
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to register download listener", e);
            call.reject("Failed to register download listener: " + e.getMessage());
        }
    } else {
        JSObject result = new JSObject();
        result.put("listening", true);
        result.put("message", "Download listener already active");
        call.resolve(result);
    }
}


private String getDownloadedApkPath(long downloadId) {
    DownloadManager downloadManager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
    if (downloadManager == null) {
        return null;
    }
    
    DownloadManager.Query query = new DownloadManager.Query();
    query.setFilterById(downloadId);
    
    Cursor cursor = downloadManager.query(query);
    if (cursor != null && cursor.moveToFirst()) {
        try {
            int uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
            if (uriIndex != -1) {
                String uriString = cursor.getString(uriIndex);
                if (uriString != null) {
                    String path = uriString.replace("file://", "");
                    Log.d(TAG, "Downloaded APK path: " + path);
                    return path;
                }
            }
        } finally {
            cursor.close();
        }
    }
    
    return null;
}

@PluginMethod
public void stopDownloadListener(PluginCall call) {
    if (downloadCompleteReceiver != null) {
        try {
            getContext().unregisterReceiver(downloadCompleteReceiver);
            downloadCompleteReceiver = null;
            downloadListenerCall = null;
            
            Log.d(TAG, "Download listener stopped");
            
            JSObject result = new JSObject();
            result.put("listening", false);
            result.put("message", "Download listener stopped");
            call.resolve(result);
            
        } catch (Exception e) {
            Log.e(TAG, "Error stopping listener", e);
            call.reject("Error stopping listener: " + e.getMessage());
        }
    } else {
        JSObject result = new JSObject();
        result.put("listening", false);
        result.put("message", "Listener was not active");
        call.resolve(result);
    }
}




// public void downloadApk(PluginCall call) {
//     String url = call.getString("path");

//     if (url == null || url.isEmpty()) {
//         call.reject("path parameter is required");
//         return;
//     }

//     try {
//         Log.d(TAG, "Starting download from: " + url);

//         DownloadManager downloadManager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
//         if (downloadManager == null) {
//             call.reject("DownloadManager not available");
//             return;
//         }

//         String fileName = Uri.parse(url).getLastPathSegment();
//         if (fileName == null) fileName = "update.apk";

//         DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
//         request.setTitle(fileName);
//         request.setDescription("Downloading APK");
//         request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//         request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
//         request.setMimeType("application/vnd.android.package-archive");
//         request.setAllowedNetworkTypes(
//             DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE
//         );

//         long downloadId = downloadManager.enqueue(request);

//         Log.d(TAG, "Download started - ID: " + downloadId);

//         JSObject result = new JSObject();
//         result.put("downloadId", downloadId);
//         result.put("fileName", fileName);
//         result.put("url", url);
//         call.resolve(result);
//     } catch (Exception e) {
//         Log.e(TAG, "Error downloading APK", e);
//         call.reject("Failed to download: " + e.getMessage());
//     }
// }
@PluginMethod
public void downloadApk(PluginCall call) {
    String url = call.getString("path");
    Boolean forceDownload = call.getBoolean("force", false);

    if (url == null || url.isEmpty()) {
        call.reject("path parameter is required");
        return;
    }

    try {
        Log.d(TAG, "downloadApk called with URL: " + url);

        // Extract the filename from the URL
        String fileName = URLUtil.guessFileName(url, null, "application/vnd.android.package-archive");
        if (fileName == null || fileName.isEmpty()) {
            String lastSegment = Uri.parse(url).getLastPathSegment();
            fileName = (lastSegment != null && lastSegment.endsWith(".apk")) 
                ? lastSegment 
                : "update.apk";
        }

        Log.d(TAG, "Target filename: " + fileName);

        // Check if the file already exists in Downloads
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File existingFile = new File(downloadsDir, fileName);

        if (existingFile.exists() && !forceDownload) {
            long fileSize = existingFile.length();
            
            // Check that the file is not corrupted (size > 0)
            if (fileSize > 0) {
                Log.d(TAG, "APK already exists: " + existingFile.getAbsolutePath() + " (size: " + fileSize + " bytes)");
                
                // Optionally check that it's a valid APK
                PackageManager pm = getContext().getPackageManager();
                PackageInfo packageInfo = pm.getPackageArchiveInfo(existingFile.getAbsolutePath(), 0);
                
                if (packageInfo != null) {
                    // ✅ The file exists and is valid
                    Log.d(TAG, "Existing APK is valid. Launching installation directly.");
                    
                    // 🔥 Launch installation immediately WITHOUT changing the response type
                    launchInstallation(existingFile);
                    
                    // ✅ Return a response identical to a normal download
                    // downloadId = -1 indicates that it was already downloaded
                    JSObject result = new JSObject();
                    result.put("downloadId", -1);
                    result.put("fileName", fileName);
                    result.put("url", url);
                    call.resolve(result);
                    return;
                    
                } else {
                    // The file exists but is corrupted, delete it
                    Log.w(TAG, "Existing APK is corrupted. Deleting and re-downloading.");
                    existingFile.delete();
                }
            } else {
                // Empty file, delete it
                Log.w(TAG, "Existing file is empty. Deleting and re-downloading.");
                existingFile.delete();
            }
        } else if (existingFile.exists() && forceDownload) {
            // Force download requested, delete the old file
            Log.d(TAG, "Force download requested. Deleting existing file.");
            existingFile.delete();
        }

        // Proceed with download
        DownloadManager downloadManager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager == null) {
            call.reject("DownloadManager not available");
            return;
        }

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle(fileName);
        request.setDescription("Downloading APK");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        request.setMimeType("application/vnd.android.package-archive");
        request.setAllowedNetworkTypes(
            DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE
        );

        long downloadId = downloadManager.enqueue(request);

        Log.d(TAG, "Download started - ID: " + downloadId + ", File: " + fileName);

        JSObject result = new JSObject();
        result.put("downloadId", downloadId);
        result.put("fileName", fileName);
        result.put("url", url);
        call.resolve(result);
        
    } catch (Exception e) {
        Log.e(TAG, "Error downloading APK", e);
        call.reject("Failed to download: " + e.getMessage());
    }
}

private void launchInstallation(File apkFile) {
    try {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        
        Uri fileUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            fileUri = FileProvider.getUriForFile(
                getContext(),
                getContext().getPackageName() + ".fileprovider",
                apkFile
            );
        } else {
            fileUri = Uri.fromFile(apkFile);
        }

        intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
        getActivity().startActivity(intent);
        
        Log.d(TAG, "Installation launched for existing APK: " + apkFile.getAbsolutePath());
        
        // Notify listeners
        JSObject event = new JSObject();
        event.put("event", "installation_started");
        event.put("status", "installation_started");
        event.put("message", "Installation dialog opened");
        event.put("path", apkFile.getAbsolutePath());
        notifyListeners("installationStarted", event);
        
    } catch (Exception e) {
        Log.e(TAG, "Failed to launch installation: " + e.getMessage(), e);
        
        // Notify error
        JSObject errorEvent = new JSObject();
        errorEvent.put("event", "installation_error");
        errorEvent.put("status", "error");
        errorEvent.put("message", "Failed to launch installation: " + e.getMessage());
        notifyListeners("installationStarted", errorEvent);
    }
}

@PluginMethod
public void getDownloadStatus(PluginCall call) {
    Long downloadId = call.getLong("downloadId");
    
    if (downloadId == null) {
        call.reject("downloadId parameter is required");
        return;
    }
    
    try {
        DownloadManager downloadManager = (DownloadManager) getContext()
            .getSystemService(Context.DOWNLOAD_SERVICE);
        
        if (downloadManager == null) {
            call.reject("DownloadManager not available");
            return;
        }
        
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);
        
        Cursor cursor = downloadManager.query(query);
        
        if (cursor != null && cursor.moveToFirst()) {
            int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            int reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
            int bytesDownloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
            int bytesTotalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
            int uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
            int titleIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TITLE);
            
            int status = statusIndex != -1 ? cursor.getInt(statusIndex) : -1;
            int reason = reasonIndex != -1 ? cursor.getInt(reasonIndex) : -1;
            long bytesDownloaded = bytesDownloadedIndex != -1 ? cursor.getLong(bytesDownloadedIndex) : 0;
            long bytesTotal = bytesTotalIndex != -1 ? cursor.getLong(bytesTotalIndex) : 0;
            String uri = uriIndex != -1 ? cursor.getString(uriIndex) : null;
            String title = titleIndex != -1 ? cursor.getString(titleIndex) : null;
            
            JSObject result = new JSObject();
            result.put("downloadId", downloadId);
            result.put("status", getStatusString(status));
            result.put("statusCode", status);
            result.put("reason", reason);
            result.put("bytesDownloaded", bytesDownloaded);
            result.put("bytesTotal", bytesTotal);
            result.put("uri", uri);
            result.put("title", title);
            
            if (bytesTotal > 0) {
                int progress = (int) ((bytesDownloaded * 100) / bytesTotal);
                result.put("progress", progress);
            }
            
            cursor.close();
            call.resolve(result);
            
        } else {
            if (cursor != null) {
                cursor.close();
            }
            call.reject("Download not found with ID: " + downloadId);
        }
        
    } catch (Exception e) {
        Log.e(TAG, "Error getting download status", e);
        call.reject("Failed to get download status: " + e.getMessage());
    }
}

@PluginMethod
public void cancelDownload(PluginCall call) {
    Long downloadId = call.getLong("downloadId");
    
    if (downloadId == null) {
        call.reject("downloadId parameter is required");
        return;
    }
    
    try {
        DownloadManager downloadManager = (DownloadManager) getContext()
            .getSystemService(Context.DOWNLOAD_SERVICE);
        
        if (downloadManager == null) {
            call.reject("DownloadManager not available");
            return;
        }
        
        int removed = downloadManager.remove(downloadId);
        
        JSObject result = new JSObject();
        result.put("removed", removed > 0);
        result.put("downloadId", downloadId);
        call.resolve(result);
        
    } catch (Exception e) {
        Log.e(TAG, "Error canceling download", e);
        call.reject("Failed to cancel download: " + e.getMessage());
    }
}


private android.os.Handler progressHandler = null;
private Runnable progressRunnable = null;
private long currentDownloadId = -1;

@PluginMethod(returnType = PluginMethod.RETURN_CALLBACK)  
public void startDownloadProgressListener(PluginCall call) {
  
    Integer downloadIdInt = call.getInt("downloadId");

    if (downloadIdInt == null) {
        Log.e("DEBUG_APK", "downloadId is null");
        call.reject("downloadId parameter is required");
        return;
    }

    long downloadId = downloadIdInt.longValue();
    android.util.Log.e("DEBUG_APK", "Call data: " + downloadId);
    Log.d(TAG, "📊 Starting progress listener for download ID: " + downloadId);
    
    call.setKeepAlive(true);  
    currentDownloadId = downloadId;
    
    if (progressHandler != null) {
        stopDownloadProgressListener(null);
    }
    
    progressHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    
    progressRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                DownloadManager downloadManager = (DownloadManager) getContext()
                    .getSystemService(Context.DOWNLOAD_SERVICE);
                
                if (downloadManager == null) {
                    Log.e(TAG, "❌ DownloadManager is null");
                    return;
                }
                
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(currentDownloadId);
                
                Cursor cursor = downloadManager.query(query);
                
                if (cursor != null && cursor.moveToFirst()) {
                    int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    int bytesDownloadedIndex = cursor.getColumnIndex(
                        DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR
                    );
                    int bytesTotalIndex = cursor.getColumnIndex(
                        DownloadManager.COLUMN_TOTAL_SIZE_BYTES
                    );
                    
                    int status = statusIndex != -1 ? cursor.getInt(statusIndex) : -1;
                    long bytesDownloaded = bytesDownloadedIndex != -1 
                        ? cursor.getLong(bytesDownloadedIndex) : 0;
                    long bytesTotal = bytesTotalIndex != -1 
                        ? cursor.getLong(bytesTotalIndex) : 0;
                    
                    cursor.close();
                    
                    JSObject progress = new JSObject();
                    progress.put("downloadId", currentDownloadId);
                    progress.put("bytesDownloaded", bytesDownloaded);
                    progress.put("bytesTotal", bytesTotal);
                    progress.put("status", getStatusString(status));
                    progress.put("statusCode", status);
                    
                    if (bytesTotal > 0) {
                        int percentage = (int) ((bytesDownloaded * 100) / bytesTotal);
                        progress.put("progress", percentage);
                        Log.d(TAG, "📊 PROGRESS: " + percentage + "% (" + bytesDownloaded + "/" + bytesTotal + ")");
                    } else if (bytesDownloaded > 0) {
                        progress.put("progress", -1);
                        Log.d(TAG, "📊 PROGRESS: -1 (unknown total, downloaded: " + bytesDownloaded + ")");
                    } else {
                        progress.put("progress", 0);
                        Log.d(TAG, "📊 PROGRESS: 0%");
                    }

                    // ✅ SEULEMENT notifyListeners
                    notifyListeners("downloadProgress", progress);
                    
                    if (status == DownloadManager.STATUS_RUNNING || 
                        status == DownloadManager.STATUS_PENDING) {
                        progressHandler.postDelayed(this, 100);  // ✅ 100ms pour plus de réactivité
                    } else if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        Log.d(TAG, "✅ Download SUCCESSFUL");
                        progress.put("progress", 100);
                        notifyListeners("downloadProgress", progress);
                        stopDownloadProgressListener(null);
                    } else if (status == DownloadManager.STATUS_FAILED) {
                        Log.e(TAG, "❌ Download FAILED");
                        progress.put("error", "Download failed");
                        notifyListeners("downloadProgress", progress);
                        stopDownloadProgressListener(null);
                    }
                } else {
                    if (cursor != null) {
                        cursor.close();
                    }
                    Log.w(TAG, "⚠️ Download not found");
                    stopDownloadProgressListener(null);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "❌ Error in progress monitoring", e);
                stopDownloadProgressListener(null);
            }
        }
    };
    
    // Start immediately
    progressHandler.post(progressRunnable);

    // ✅ CRITICAL ADDITION: Resolve the initial call
    JSObject result = new JSObject();
    result.put("listening", true);
    result.put("downloadId", downloadId);
    call.resolve(result);  // ← ADD THIS LINE !
}


@PluginMethod
public void stopDownloadProgressListener(PluginCall call) {
    if (progressHandler != null && progressRunnable != null) {
        progressHandler.removeCallbacks(progressRunnable);
        progressHandler = null;
        progressRunnable = null;
        currentDownloadId = -1;
        
        Log.d(TAG, "Download progress listener stopped");
        
        if (call != null) {
            JSObject result = new JSObject();
            result.put("listening", false);
            call.resolve(result);
        }
    } else {
        if (call != null) {
            JSObject result = new JSObject();
            result.put("listening", false);
            result.put("message", "Progress listener was not active");
            call.resolve(result);
        }
    }
}

@PluginMethod
public void deleteApk(PluginCall call) {
    String path = call.getString("path");
    
    if (path == null || path.isEmpty()) {
        call.reject("path parameter is required");
        return;
    }
    
    try {
        File file = new File(path);
        
        if (!file.exists()) {
            call.reject("File does not exist: " + path);
            return;
        }
        
        boolean deleted = file.delete();
        
        JSObject result = new JSObject();
        result.put("deleted", deleted);
        result.put("path", path);
        
        if (deleted) {
            Log.d(TAG, "Successfully deleted: " + path);
            call.resolve(result);
        } else {
            call.reject("Failed to delete file: " + path);
        }
        
    } catch (Exception e) {
        Log.e(TAG, "Error deleting file", e);
        call.reject("Failed to delete file: " + e.getMessage());
    }
}

// Helper method to convert status code to readable string
private String getStatusString(int status) {
    switch (status) {
        case DownloadManager.STATUS_PENDING:
            return "PENDING";
        case DownloadManager.STATUS_RUNNING:
            return "RUNNING";
        case DownloadManager.STATUS_PAUSED:
            return "PAUSED";
        case DownloadManager.STATUS_SUCCESSFUL:
            return "SUCCESSFUL";
        case DownloadManager.STATUS_FAILED:
            return "FAILED";
        default:
            return "UNKNOWN";
    }
}
}