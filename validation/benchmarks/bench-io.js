var fs = require('fs');

var Util = require('../util.js');

var lineSeparator = '\n';
var attrSeparator = ';';

var _writeCallback= function(callback) {

  return function(err) {
    if (err) throw err;
    if (callback !== undefined) callback();
  }
}

var _toJson = function(operations) {
  var result = '';

  if (operations.constructor === Array) {
    operations.forEach(function(operation) {
      result += JSON.stringify(operation);
    });
  } else  {
    result += JSON.stringify(operations);  
  }
  
  return result;
}

var _exportJson = function(filename, operations, callback) {  
  fs.exists(filename, function(exists) {
  
    if (!exists) {
      fs.writeFile(filename, _toJson(operations), _writeCallback(callback));
    } else {
      var text = '\n' + _toJson(operations);
      fs.appendFile(filename, text, _writeCallback(callback));
    }
  });
}

var _importJson = function(filename, callback) {
  fs.readFile(filename, { encoding: 'utf8' }, function (err, data) {
    if (err) throw err;

    var operationsStr = data.split('\n');
    if (operationsStr[operationsStr.length - 1] === '') {
      operationsStr.pop();
    }
    var operations = operationsStr.map(function(str) {
      return JSON.parse(str);
    });
    callback(operations);
  });
}

/*var _toCsv = function(operations, withHeaders) {

  var lineSeparator = '\n';
  var attrSeparator = ';';

  var headers =
    'Date' + attrSeparator +
    'Framework' + attrSeparator +
    'Entity' + attrSeparator +
    'Entity Type' + attrSeparator +
    'Operation' + attrSeparator +
    'Total Time' + attrSeparator +
    'Load' + attrSeparator +
    'Count' + attrSeparator +
    'Meter Mean' + attrSeparator +
    'CurrentRate' + attrSeparator +
    '1MinuteRate' + attrSeparator +
    '5MinuteRate' + attrSeparator +
    '15MinuteRate' + attrSeparator +
    'Min' + attrSeparator +
    'Max' + attrSeparator +
    'Sum' + attrSeparator +
    'Variance' + attrSeparator +
    'Histogram Mean' + attrSeparator +
    'Std' + attrSeparator +
    'p25' + attrSeparator +
    'median' + attrSeparator +
    'p75' + attrSeparator +
    'p95' + attrSeparator +
    'p99' + attrSeparator +
    'p999' + attrSeparator +
    lineSeparator;
  
  var __toCsv = function(operation) {
    var meter = operation.stats.main.meter;
    var histogram = operation.stats.main.histogram;

    return
      new Date(Date.now()).toJSON() + attrSeparator +
      operation.entity.endpoint.framework + attrSeparator +
      operation.entity.name + attrSeparator +
      operation.entity.type + attrSeparator +
      operation.type + attrSeparator +
      operation.stats.totalElapsed + attrSeparator +
      operation.options.limit + attrSeparator +
      meter.count + attrSeparator +
      meter.mean + attrSeparator +
      meter.currentRate + attrSeparator +
      meter['1MinuteRate'] + attrSeparator +
      meter['5MinuteRate'] + attrSeparator +
      meter['15MinuteRate'] + attrSeparator +
      histogram.min + attrSeparator +
      histogram.max + attrSeparator +
      histogram.sum + attrSeparator +
      histogram.variance + attrSeparator +
      histogram.mean + attrSeparator +
      histogram.stddev + attrSeparator +
      histogram.p25 + attrSeparator +
      histogram.median + attrSeparator +
      histogram.p75 + attrSeparator +
      histogram.p95 + attrSeparator +
      histogram.p99 + attrSeparator +
      histogram.p999 + attrSeparator +
      lineSeparator;
  }
  
  var result = '';

  if (operations.constructor === Array) {
    operations.forEach(function(operation) {
      result += __toCsv(operation);
    });
  } else  {
    result += __toCsv(operations);  
  }
  
  return withHeaders ? headers + result : result;
}*/

var _toTimes = function() {

  this.headers = function(objs) {
    return
      'Framework' + attrSeparator +
      'Entity Type' + attrSeparator +
      'Time' + attrSeparator +
      lineSeparator;
  }
  
  this.execute = function(operation) {
    var result = '';

    operation.stats.main.histogram.sample._elements._elements.forEach(function(register) {
      result +=
        operation.entity.endpoint.framework + attrSeparator +
        operation.entity.type + attrSeparator +
        register.value + attrSeparator +
        lineSeparator;
    });

    return result;
  }
  
  return this;
}

var _defaultCsv = function() {

  this.headers = function(objs) {
    var result = '';
    
    var obj = Util.instance(objs);
    for (var attr in obj) {
      if (obj.hasOwnProperty(attr)) {
        result += attr + attrSeparator;
      }
    }

    return result + lineSeparator;
  }
  
  this.execute = function(obj) {
    var result = '';

    for (var attr in obj) {
      if (obj.hasOwnProperty(attr)) {
        result += obj[attr] + attrSeparator;
      }
    }

    return result + lineSeparator;
  }
  
  return this;
}

var _exportCsv = function(exporter) {

  return function(filename, objs, withHeaders, callback) {
    
    var _export = function() {
      var result = '';

      Util.applyFunction(objs, function(obj) {
        result += exporter.execute(obj);      
      });
      
      return withHeaders ? exporter.headers(objs) + result : result;
    }
  
    fs.exists(filename, function(exists) {
    
      if (withHeaders === undefined) withHeaders = true;
      if (!exists) {
        fs.writeFile(filename, _export(), _writeCallback(callback));
      } else {
        fs.appendFile(filename, _export(), _writeCallback(callback));
      }
    });
  };
}

module.exports.exportJson = _exportJson
module.exports.importJson = _importJson
module.exports.exportCsv = _exportCsv(new _defaultCsv())
module.exports.exportTimes = _exportCsv(new _toTimes())
