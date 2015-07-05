var Util = require('../util.js');
var Entity = require('./entity.js');

function Address(endpoint) {

  this.endpoint = endpoint;
  this.name = 'address';
  this.path = '/addresses/';
  this.type = Entity.Type.simple;
  
  var _entity = Entity.create(
    this.name,
    endpoint,
    this.path,
    [
      function() {
        return {
          destinatary: 'John Doe',
          street: '342 Madison Avenue',
          zipCode: '10173',
          city: 'New York',
          country: 'Portugal'
        }
      },
      function() {
        return {
          destinatary: 'John Doe',
          street: '342 Madison Avenue',
          zipCode: '10173',
          city: 'New York',
          country: 'USA'
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

module.exports = Address
