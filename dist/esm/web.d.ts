import { WebPlugin } from '@capacitor/core';
import type { ApkFilesPlugin, ListApksOptions, ApkListResult, DebugScanResult, ScanPathOptions, ScanPathResult, PermissionResult, PermissionsStatus, InstallApkOptions, InstallResult, DownloadListenerResult, DownloadApkOptions, DownloadResult, DownloadStatusOptions, DownloadStatus, CancelDownloadOptions, CancelDownloadResult, DeleteApkOptions, DeleteApkResult, DownloadProgressOptions, DownloadProgressListenerResult } from './definitions';
export declare class ApkFilesWeb extends WebPlugin implements ApkFilesPlugin {
    listDownloadApks(options?: ListApksOptions): Promise<ApkListResult>;
    listAllApks(options?: ListApksOptions): Promise<ApkListResult>;
    debugScan(): Promise<DebugScanResult>;
    scanSpecificPath(options: ScanPathOptions): Promise<ScanPathResult>;
    requestAllFilesAccess(): Promise<PermissionResult>;
    checkPermissions(): Promise<PermissionsStatus>;
    installApk(options: InstallApkOptions): Promise<InstallResult>;
    startDownloadListener(): Promise<DownloadListenerResult>;
    stopDownloadListener(): Promise<DownloadListenerResult>;
    startDownloadProgressListener(options: DownloadProgressOptions): Promise<DownloadProgressListenerResult>;
    stopDownloadProgressListener(): Promise<DownloadProgressListenerResult>;
    downloadApk(options: DownloadApkOptions): Promise<DownloadResult>;
    getDownloadStatus(options: DownloadStatusOptions): Promise<DownloadStatus>;
    cancelDownload(options: CancelDownloadOptions): Promise<CancelDownloadResult>;
    deleteApk(options: DeleteApkOptions): Promise<DeleteApkResult>;
}
