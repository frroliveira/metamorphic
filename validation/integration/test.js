var Endpoint = require('../endpoint.js');
var Util = require('../util.js');
var Request = require('sync-request');

Util.connect(Endpoint.Default.baseUri);

var _ignoreField = function(val) { true }

var _numberArray = function(val) {

  if (expect(val).toBeType(Array)) {
    var newVal = val.map(function(value) {
      return { id: value };
    });
    return expect(newVal).toContainJsonTypes('*', {id: Number});
  } else {
    false
  }
}

var _addId = function(obj, id) {
  var newObj = Util.clone(obj);
  
  if (id === undefined) {
    newObj['id'] = _ignoreField;
  } else {
    newObj['id'] = id;
  }
  
  return newObj;
}

var _create = function(creator) {
  var endpoints = [Endpoint.Default];
  
  endpoints.forEach(function(endpoint) {
    var Frisby = new require('frisby');
    
    Frisby.globalSetup({
      request: {
        headers: {
          'Content-Type': 'application/json'
        },
        json: true,
        baseUri: endpoint.baseUri
      }
    });

    creator(Frisby, endpoint);
  });
}

module.exports.NumberArray = _numberArray
module.exports.Ignore = _ignoreField
module.exports.addId = _addId
module.exports.create = _create
