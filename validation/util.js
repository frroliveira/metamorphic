var syncRequest = require('sync-request');
var request = require('request');

var _clone = function(obj) {
  if (null == obj) {
    return obj;
  }
  
  var copy = obj.constructor();
  for (var attr in obj) {
    if (obj.hasOwnProperty(attr)) {
      copy[attr] = obj[attr];
    }
  }
  return copy;
}

var _listFunction = function(f) {
  return function(list) {
    for (var i = 0; i < list.length; i++) {
      f(list[i]);
    }
  };
}

var _mapListFunction = function(f) {
  return function(list) {
    return list.map(f);
  };
}

var _applyFunction = function(objs, f) {
  if (objs.constructor === Array) {
    objs.forEach(f);
  } else {
    f(objs);
  }
}

var _instance = function(objs) {
  if (objs.constructor === Array) {
    return objs[0];
  } else {
    return objs;
  }
}

var _connect = function(url) {
  console.log('Connecting...');

  var connected = false;
  while (!connected) {
    try {
      var result = syncRequest('GET', url);
      connected = true;
    } catch (err) {
      if (err.message !== 'connect ECONNREFUSED') {
        connected = true;
      }
    }
  }

  console.log('Initializing...');
}

var _syncpost = function(url, body) {
  try {
    var result = syncRequest('POST', url, {
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      },
      body: JSON.stringify(body)
    });
  
    var str = result.getBody().toString();
    return JSON.parse(str);
  } catch (err) {
    return {};
  }
}

var _syncget = function(url) {
  try {
    var result = syncRequest('GET', url, {
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      }
    });
  
    var str = result.getBody().toString();
    return JSON.parse(str);
  } catch (err) {
    return {};
  }
}

var _delete = function(url) {
  try {
    request.del(url);
  } catch (err) {
  }
  return;
}

module.exports.clone = _clone
module.exports.listFunction = _listFunction
module.exports.mapListFunction = _mapListFunction
module.exports.applyFunction = _applyFunction
module.exports.instance = _instance
module.exports.connect = _connect
module.exports.syncpost = _syncpost
module.exports.syncget = _syncget
module.exports.delete = _delete
