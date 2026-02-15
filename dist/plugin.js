import { registerPlugin, WebPlugin } from '@capacitor/core';

const ApkFiles = registerPlugin('ApkFiles', {
    web: () => Promise.resolve().then(function () { return web; }).then((m) => new m.ApkFilesWeb()),
});

class ApkFilesWeb extends WebPlugin {
    async listDownloadApks(options) {
        console.log('listDownloadApks called with options:', options);
        console.warn('ApkFiles.listDownloadApks is not supported on web');
        return Object.assign(Object.assign({ files: [], count: 0 }, ((options === null || options === void 0 ? void 0 : options.appId) && { filteredBy: options.appId })), ((options === null || options === void 0 ? void 0 : options.onlyLatest) && { onlyLatest: true }));
    }
    async listAllApks(options) {
        console.log('listAllApks called with options:', options);
        console.warn('ApkFiles.listAllApks is not supported on web');
        return Object.assign(Object.assign({ files: [], count: 0 }, ((options === null || options === void 0 ? void 0 : options.appId) && { filteredBy: options.appId })), ((options === null || options === void 0 ? void 0 : options.onlyLatest) && { onlyLatest: true }));
    }
    async debugScan() {
        console.warn('ApkFiles.debugScan is not supported on web');
        throw new Error('Method not implemented on web platform.');
    }
    async scanSpecificPath(options) {
        console.log('scanSpecificPath called with options:', options);
        console.warn('ApkFiles.scanSpecificPath is not supported on web');
        return Object.assign(Object.assign({ files: [], count: 0, scannedPath: options.path, pathExists: false, pathIsDirectory: false, pathCanRead: false }, ((options === null || options === void 0 ? void 0 : options.appId) && { filteredBy: options.appId })), ((options === null || options === void 0 ? void 0 : options.onlyLatest) && { onlyLatest: true }));
    }
    async requestAllFilesAccess() {
        console.warn('ApkFiles.requestAllFilesAccess is not supported on web');
        return {
            granted: false,
            message: 'Not supported on web platform',
        };
    }
    async checkPermissions() {
        console.warn('ApkFiles.checkPermissions is not supported on web');
        return {
            hasStoragePermission: false,
            hasAllFilesAccess: false,
            canRequestInstallPackages: false,
            sdkVersion: 0,
            readExternalStorage: false,
        };
    }
    async installApk(options) {
        console.log('installApk called with options:', options);
        console.warn('ApkFiles.installApk is not supported on web');
        throw new Error('Method not implemented on web platform.');
    }
    async startDownloadListener() {
        console.warn('ApkFiles.startDownloadListener is not supported on web');
        return {
            listening: false,
            message: 'Download listener is only available on Android',
        };
    }
    async stopDownloadListener() {
        console.warn('ApkFiles.stopDownloadListener is not supported on web');
        return {
            listening: false,
            message: 'Download listener is only available on Android',
        };
    }
    async startDownloadProgressListener(options) {
        console.warn('ApkFiles.startDownloadProgressListener is not supported on web', options);
        return {
            listening: false,
            message: 'Download progress listener is only available on Android',
            downloadId: options.downloadId,
        };
    }
    async stopDownloadProgressListener() {
        console.warn('ApkFiles.stopDownloadProgressListener is not supported on web');
        return {
            listening: false,
            message: 'Download progress listener is only available on Android',
        };
    }
    async downloadApk(options) {
        console.log('downloadApk called with options:', options);
        console.warn('ApkFiles.downloadApk is not supported on web');
        throw new Error('Method not implemented on web platform.');
    }
    async getDownloadStatus(options) {
        console.log('getDownloadStatus called with options:', options);
        console.warn('ApkFiles.getDownloadStatus is not supported on web');
        throw new Error('Method not implemented on web platform.');
    }
    async cancelDownload(options) {
        console.log('cancelDownload called with options:', options);
        console.warn('ApkFiles.cancelDownload is not supported on web');
        throw new Error('Method not implemented on web platform.');
    }
    async deleteApk(options) {
        console.log('deleteApk called with options:', options);
        console.warn('ApkFiles.deleteApk is not supported on web');
        throw new Error('Method not implemented on web platform.');
    }
}

var web = /*#__PURE__*/Object.freeze({
    __proto__: null,
    ApkFilesWeb: ApkFilesWeb
});

export { ApkFiles };
//# sourceMappingURL=plugin.js.map
