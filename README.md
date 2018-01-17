# Alternative camera plugin for apache cordova

The main feature of this camera is not to close the APP by low memory.

This plugin is just a wrapper of the https://github.com/commonsguy/cwac-cam2

## Platforms

Android

## Installation
`cordova plugin add https://github.com/mateusnava/aqila-camera.git`

## How can I Use?

```javascript
// this.diagnostic is Diagnostic from '@ionic-native/diagnostic'
// https://ionicframework.com/docs/native/diagnostic/

const takePictureFromAqilaCamera = () => {
  return new Promise((resolve, reject) => {
    this.diagnostic.requestRuntimePermission(cordova.plugins.diagnostic.runtimePermission.WRITE_EXTERNAL_STORAGE).then(() => {
      window.aqilaCamera.open((path) => {
        resolve('file://' + path);
      }, reject, this.PICTURES_APPLICATION_FOLDER);
    }, reject);
  });
}
```
