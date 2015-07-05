var Util = require('../util.js');

function Entity(name, endpoint, path, defaultGenerators) {

  this.entity = function(generators) {
    if (generators === undefined) {
      generators = defaultGenerators;
    }
    var index = Math.random() * (generators.length - 1);
    var generator = generators[Math.round(index)];
    return generator();
  }
  
  this.create = function() {
    var value = this.entity();
    return Util.syncpost(endpoint.baseUri + path, value);
  }
  
  this.createId = function() {
    return this.create()[endpoint.options.idField];
  }

  this.createList = function(length) {
    var result = [];
    for (var i = 0; i < length; i++) {
      result[i] = this.create()
    }
    return result;
  }
  
  this.createIdList = function(length) {
    return this.createList(length).map(function(value) { 
      return value[endpoint.options.idField];
    });
  }
  
  this.get = function(id) {
    return Util.syncget(endpoint.baseUri + path + id + '/');
  }
  
  this.getList = Util.mapListFunction(this.get);
  
  this.delete = function(id) {
    Util.delete(endpoint.baseUri + path + id + '/');
  }
  
  this.deleteList = Util.listFunction(this.delete);

  return this;
}

module.exports.Type = {
  simple: 'Simple',
  hasObject: 'Has Object',
  hasList: 'Has List',
  hasBoth: 'Has Both'
}

module.exports.create = function(name, endpoint, path, defaultGenerators) {
  return new Entity(name, endpoint, path, defaultGenerators);
}
