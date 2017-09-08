cordova.define("com.plugin.myPlugin.MyPlugin", function(require, exports, module) {
var exec = require('cordova/exec');

exports.coolMethod = function(arg0, success, error) {
    exec(success, error, "MyPlugin", "coolMethod", [arg0]);
};

exports.upImgMethod = function(arg0, success, error) {
  exec(success, error, "MyPlugin", "upImgMethod", [arg0]);
};

exports.positionMethod = function(arg0, success, error) {
  exec(success, error, "MyPlugin", "positionMethod", [arg0]);
};

exports.shareUrlMethod = function(arg0, success, error) {
  exec(success, error, "MyPlugin", "shareUrlMethod", [arg0]);
};

exports.extLoginMethod = function(arg0, success, error) {
  exec(success, error, "MyPlugin", "extLoginMethod", [arg0]);
};

exports.payMethod = function(arg0, success, error) {
  exec(success, error, "MyPlugin", "payMethod", [arg0]);
};

exports.getPushTokenMethod = function(arg0, success, error) {
  exec(success, error, "MyPlugin", "getPushTokenMethod", [arg0]);
};

exports.getLatLngMethod = function(arg0, success, error) {
  exec(success, error, "MyPlugin", "getLatLngMethod", [arg0]);
};

exports.navigationMethod = function(arg0, success, error) {
  exec(success, error, "MyPlugin", "navigationMethod", [arg0]);
};

});
