import { registerPlugin } from '@capacitor/core';

import { ApkFilesPlugin } from './definitions';

const ApkFiles = registerPlugin<ApkFilesPlugin>('ApkFiles', {
  web: () => import('./web').then((m) => new m.ApkFilesWeb()),
});

export * from './definitions';
export { ApkFiles };
