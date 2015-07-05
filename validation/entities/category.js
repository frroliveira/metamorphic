var Util = require('../util.js');
var Entity = require('./entity.js');
  
function Category(endpoint) {

  this.endpoint = endpoint;
  this.name = 'category';
  this.path = '/categories/';
  this.type = Entity.Type.simple;
  
  var _entity = Entity.create(
    this.name,
    endpoint,
    this.path,
    [
      function() {
        return {
          name: 'Cameras',
          description: 'High quality cameras'
        }
      },
      function() {
        return {
          name: 'Cameras',
          description: 'High quality video cameras'
        }
      }
    ]
  );

  this.entity = function() { return _entity.entity() }
  this.create = function() { return _entity.create() }
  this.createId = function() { return _entity.createId() }
  this.createList = function(length) { return _entity.createList(length) }
  this.createIdList = function(length) { return _entity.createIdList(length) }
  this.get = function(id) { return _entity.get(id) }
  this.getList = function(ids) { return _entity.getList(ids) }
  this.delete = function(id) { return _entity.delete(id) }
  this.deleteList = function(ids) { return _entity.deleteList(ids) }
  this.clean = function(entity) { return; }
  this.cleanList = Util.listFunction(this.clean);
}

module.exports = Category
