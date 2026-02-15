import { WebPlugin } from '@capacitor/core';

import type {
  ApkFilesPlugin,
  ListApksOptions,
  ApkListResult,
  DebugScanResult,
  ScanPathOptions,
  ScanPathResult,
  PermissionResult,
  PermissionsStatus,
  InstallApkOptions,
  InstallResult,
  DownloadListenerResult,
  DownloadApkOptions,
  DownloadResult,
  DownloadStatusOptions,
  DownloadStatus,
  CancelDownloadOptions,
  CancelDownloadResult,
  DeleteApkOptions,
  DeleteApkResult,
  DownloadProgressOptions,
  DownloadProgressListenerResult,
} from './definitions';

export class ApkFilesWeb extends WebPlugin implements ApkFilesPlugin {
  async listDownloadApks(options?: ListApksOptions): Promise<ApkListResult> {
    console.log('listDownloadApks called with options:', options);
    console.warn('ApkFiles.listDownloadApks is not supported on web');
    return {
      files: [],
      count: 0,
      ...(options?.appId && { filteredBy: options.appId }),
      ...(options?.onlyLatest && { onlyLatest: true }),
    };
  }

  async listAllApks(options?: ListApksOptions): Promise<ApkListResult> {
    console.log('listAllApks called with options:', options);
    console.warn('ApkFiles.listAllApks is not supported on web');
    return {
      files: [],
      count: 0,
      ...(options?.appId && { filteredBy: options.appId }),
      ...(options?.onlyLatest && { onlyLatest: true }),
    };
  }

  async debugScan(): Promise<DebugScanResult> {
    console.warn('ApkFiles.debugScan is not supported on web');
    throw new Error('Method not implemented on web platform.');
  }

  async scanSpecificPath(options: ScanPathOptions): Promise<ScanPathResult> {
    console.log('scanSpecificPath called with options:', options);
    console.warn('ApkFiles.scanSpecificPath is not supported on web');
    return {
      files: [],
      count: 0,
      scannedPath: options.path,
      pathExists: false,
      pathIsDirectory: false,
      pathCanRead: false,
      ...(options?.appId && { filteredBy: options.appId }),
      ...(options?.onlyLatest && { onlyLatest: true }),
    };
  }

  async requestAllFilesAccess(): Promise<PermissionResult> {
    console.warn('ApkFiles.requestAllFilesAccess is not supported on web');
    return {
      granted: false,
      message: 'Not supported on web platform',
    };
  }

  async checkPermissions(): Promise<PermissionsStatus> {
    console.warn('ApkFiles.checkPermissions is not supported on web');
    return {
      hasStoragePermission: false,
      hasAllFilesAccess: false,
      canRequestInstallPackages: false,
      sdkVersion: 0,
      readExternalStorage: false,
    };
  }

  async installApk(options: InstallApkOptions): Promise<InstallResult> {
    console.log('installApk called with options:', options);
    console.warn('ApkFiles.installApk is not supported on web');
    throw new Error('Method not implemented on web platform.');
  }

  async startDownloadListener(): Promise<DownloadListenerResult> {
    console.warn('ApkFiles.startDownloadListener is not supported on web');
    return {
      listening: false,
      message: 'Download listener is only available on Android',
    };
  }

  async stopDownloadListener(): Promise<DownloadListenerResult> {
    console.warn('ApkFiles.stopDownloadListener is not supported on web');
    return {
      listening: false,
      message: 'Download listener is only available on Android',
    };
  }

  async startDownloadProgressListener(options: DownloadProgressOptions): Promise<DownloadProgressListenerResult> {
    console.warn('ApkFiles.startDownloadProgressListener is not supported on web', options);
    return {
      listening: false,
      message: 'Download progress listener is only available on Android',
      downloadId: options.downloadId,
    };
  }

  async stopDownloadProgressListener(): Promise<DownloadProgressListenerResult> {
    console.warn('ApkFiles.stopDownloadProgressListener is not supported on web');
    return {
      listening: false,
      message: 'Download progress listener is only available on Android',
    };
  }

  async downloadApk(options: DownloadApkOptions): Promise<DownloadResult> {
    console.log('downloadApk called with options:', options);
    console.warn('ApkFiles.downloadApk is not supported on web');
    throw new Error('Method not implemented on web platform.');
  }

  async getDownloadStatus(options: DownloadStatusOptions): Promise<DownloadStatus> {
    console.log('getDownloadStatus called with options:', options);
    console.warn('ApkFiles.getDownloadStatus is not supported on web');
    throw new Error('Method not implemented on web platform.');
  }

  async cancelDownload(options: CancelDownloadOptions): Promise<CancelDownloadResult> {
    console.log('cancelDownload called with options:', options);
    console.warn('ApkFiles.cancelDownload is not supported on web');
    throw new Error('Method not implemented on web platform.');
  }

  async deleteApk(options: DeleteApkOptions): Promise<DeleteApkResult> {
    console.log('deleteApk called with options:', options);
    console.warn('ApkFiles.deleteApk is not supported on web');
    throw new Error('Method not implemented on web platform.');
  }
}
