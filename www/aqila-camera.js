var exec = require('cordova/exec'),
    argscheck = require('cordova/argscheck'),
    utils = require('cordova/utils');

module.exports = (function() {
  var _camera = {};


  _camera.open = function(successCallback, errorCallback, dstPath) {
    exec(successCallback, errorCallback, 'AqilaCamera', 'takePicture', [dstPath]);
  }

  return _camera;
}());
