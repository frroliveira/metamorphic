var benchrest = require('bench-rest');
var fs = require('fs');

var Util = require('../util.js');
var Endpoint = require('../endpoint.js');
var Entity = require('../entities/entity.js');
var BenchIO = require('./bench-io.js');


var Operation = {
  none: 'None',
  create: 'Create',
  getAll: 'GetAll',
  get: 'Get',
  update: 'Update',
  delete: 'Delete'
}

var getAllCount = 100;
var defaultLimitCount = 10;
var defaultIterationsCount = 1000;


var filterByType = function(operations, type) {
  return operations.filter(function(operation) {
    return operation.type == type;
  });
}

var filterByEntityType = function(operations, entityType) {
  return operations.filter(function(operation) {
    return operation.entity.type == entityType;
  });
}

var buildTimes = function(operations, type) {
  var times = [];
  var typeOperations = filterByType(operations, type);
      
  var resolve = function(entityType) {
    var entityOperations = filterByEntityType(typeOperations, entityType);
    
    if (entityOperations.length == 0) return;
    
    var frameworkCount = entityOperations.length;
    var registerCount = entityOperations[0].stats.main.histogram.sample._elements._elements.length;
    
    for (var i = 0; i < registerCount; i++) {
      var obj = { 'Entity Type': entityType };

      for (var j = 0; j < frameworkCount; j++) {
        obj[entityOperations[j].entity.endpoint.framework] =
          entityOperations[j].stats.main.histogram.sample._elements._elements[i].value;
      }
      
      times.push(obj);
    }
  }

  resolve(Entity.Type.simple);
  resolve(Entity.Type.hasObject);
  resolve(Entity.Type.hasList);

  return times;
}

var buildStats = function(operations, type) {
  var stats = [];
  var typeOperations = filterByType(operations, type);
      
  var resolve = function(entityType) {
    var entityOperations = filterByEntityType(typeOperations, entityType);
    
    if (entityOperations.length == 0) return;
    
    var frameworkCount = entityOperations.length;
    for (var j = 0; j < frameworkCount; j++) {
      var obj = {};
      
      obj['Framework'] = entityOperations[j].entity.endpoint.framework;
      obj['Mean'] = entityOperations[j].stats.main.histogram.mean;
      obj['Std'] = entityOperations[j].stats.main.histogram.stddev;
      obj['Min'] = entityOperations[j].stats.main.histogram.min;
      obj['p25'] = entityOperations[j].stats.main.histogram.p25;
      obj['Median'] = entityOperations[j].stats.main.histogram.median;
      obj['p75'] = entityOperations[j].stats.main.histogram.p75;
      obj['Max'] = entityOperations[j].stats.main.histogram.max;
      
      stats.push(obj);
    }
  }

  resolve(Entity.Type.simple);
  resolve(Entity.Type.hasObject);
  resolve(Entity.Type.hasList);

  return stats;
}

var exportTimes = function(operations, type, dir) {
  fs.mkdir(dir, function() {
    var times = buildTimes(operations, type);
    var timesFilename = dir + '/' + dir + type + '.csv';
    BenchIO.exportCsv(timesFilename, times);

    var stats = buildStats(operations, type);
    var statsFilename = dir + '/' + dir + type + 'Stats.csv';
    BenchIO.exportCsv(statsFilename, stats);
  });
}

var operations = [];

var addOperations = function(entity) {
  var limits = [defaultLimitCount];
  limits.forEach(function(limit) {
    addOperationsWithLimit(entity, limit);
  });
}

var addOperationsWithLimit = function(entity, limit) {
  
  var ids = [];
  var baseUrl = entity.endpoint.baseUri + entity.path  
  var first = entity.entity();
  var second = entity.entity();
  
  var defaultLimit = function() {
    return limit === undefined ? defaultLimitCount : limit;
  }
  
  var defaultIterations = function() {
    return defaultIterationsCount;
  }
  
  var defaultOptions = function() {
    return {
      limit: defaultLimit(),
      iterations: defaultIterations()
    }  
  }
    
  var collectId = function(all) {
    ids.push(all.body[entity.endpoint.options.idField]);
    return all;
  }
      
  var addId = function(all) {
    all.requestOptions.uri += ids[all.env.index] + '/';
    return all;
  }

  // Benchmark Create(POST)
  operations.push({
    type: Operation.create,
    entity: entity,
    options: defaultOptions(),
    flow: {
      post: baseUrl,
      json: first,
      afterHooks: [ collectId ]
    }
  });

  // Benchmark Get(GET)
  operations.push({
    type: Operation.get,
    entity: entity,
    options: defaultOptions(),
    flow: {
      get: baseUrl,
      beforeHooks: [ addId ]
    }
  });
    
  // Benchmark Update(PUT)
  operations.push({
    type: Operation.update,
    entity: entity,
    options: defaultOptions(),
    flow: {
      put: baseUrl,
      json: second,
      beforeHooks: [ addId ]
    }
  });
  
  // Benchmark Delete(DELETE)
  operations.push({
    type: Operation.delete,
    entity: entity,
    options: defaultOptions(),
    flow: {
      del: baseUrl,
      beforeHooks: [ addId ],
      headers: { 'Content-Length': 0 }
    },
    after: function() {
      ids = [];
    }
  });

  // Prepare GetAll(GET)
  operations.push({
    type: Operation.none,
    entity: entity,
    options: {
      limit: defaultLimit(),
      iterations: getAllCount
    },
    flow: {
      post: baseUrl,
      json: first,
      afterHooks: [ collectId ]
    }
  });
  
  // Benchmark GetAll(GET)
  operations.push({
    type: Operation.getAll,
    entity: entity,
    options: defaultOptions(),
    flow: {
      get: baseUrl
    }
  });
  
  // Resolve GetAll(GET)
  operations.push({
    type: Operation.none,
    entity: entity,
    options: {
      limit: defaultLimit(),
      iterations: getAllCount
    },
    flow: {
      del: baseUrl,
      beforeHooks: [ addId ],
      headers: { 'Content-Length': 0 }
    },
    after: function() {
      entity.clean(first);
      entity.clean(second);
    }
  });
}

var addOperationsFromFile = function(filepath) {
  var EntityModule = require(filepath);
  var Entity = new EntityModule(Endpoint.Default);
  addOperations(Entity);
}

var dir = null;
var filename = null;
var argCount = process.argv.length;

if (argCount >= 3) {
  defaultIterationsCount = parseInt(process.argv[2]);
}
if (argCount >= 4) {
  filename = process.argv[3];
}
if (argCount >= 5) {
  dir = process.argv[4];
}

var run = function() {
  if (operations.length == 0) return;

  var operation = operations.shift();
  var points = 0;
  
  operation.options.progress = 5000;
  process.stdout.write(operation.entity.name + ': ' + operation.type + ' ');

  benchrest(operation.flow, operation.options)
    .on('error', function (err, ctxName) {
      console.error('Failed in %s with err: ', ctxName, err);
    })
    .on('progress', function (stats, percent, concurrent) {
      if (percent / 10 > points) {
        points += 1;
        process.stdout.write('.');
      }
    })
    .on('end', function (stats, errorCount) {
      operation.stats = stats;
      operation.datetime = new Date(Date.now());

      if (operation.after !== undefined) operation.after();
      
      if ((operation.type != Operation.none) && argCount >= 4) {
        BenchIO.exportJson(filename, operation, function() {
          process.stdout.write('\n');
          run();
        });
      } else {
        process.stdout.write('\n');
        run();
      }
    });
}

if ((argCount == 3) || (argCount == 4)) {
  Util.connect(Endpoint.Default.baseUri);

  addOperationsFromFile('../entities/address.js');
  addOperationsFromFile('../entities/product.js');
  addOperationsFromFile('../entities/shop.js');

  run();
} else if (argCount == 5) {

  BenchIO.importJson(filename, function(operations) {
    exportTimes(operations, Operation.create, dir);
    exportTimes(operations, Operation.getAll, dir);
    exportTimes(operations, Operation.get, dir);
    exportTimes(operations, Operation.update, dir);
    exportTimes(operations, Operation.delete, dir);
  });
} else {
  console.log('Usage: node benchmark.js <iterations> [filename] [dir]');
}

