import { registerPlugin } from '@capacitor/core';
const ApkFiles = registerPlugin('ApkFiles', {
    web: () => import('./web').then((m) => new m.ApkFilesWeb()),
});
export * from './definitions';
export { ApkFiles };
//# sourceMappingURL=index.js.map