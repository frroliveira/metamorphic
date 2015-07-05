var Util = require('../util.js');
var Entity = require('./entity.js');

function Customer(endpoint) {

  var AddressModule = require('./address.js');
  var Address = new AddressModule(endpoint);

  this.endpoint = endpoint;
  this.name = 'customer';
  this.path = '/customers/';
  this.type = Entity.Type.hasObject;
  
  var _entity = Entity.create(
    this.name,
    endpoint,
    this.path,
    [
      function() {
        return {
          email: 'john.doe@mail.com',
          name: 'Jonh Doe',
          birthdate: '2015-12-03',
          shipTo: Address.createId(),
          invoiceTo: Address.createId()
        }
      },
      function() {
        return {
          email: 'john.doe@mail.com',
          name: 'John Doe',
          birthdate: '2015-12-03',
          shipTo: Address.createId(),
          invoiceTo: Address.createId()
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
  this.clean = function(customer) {
    var shipTo = Address.get(customer.shipTo);
    var invoiceTo = Address.get(customer.invoiceTo);
    
    Address.delete(customer.shipTo);
    Address.delete(customer.invoiceTo);
    
    Address.clean(shipTo);
    Address.clean(invoiceTo);    
    return;
  }
  this.cleanList = Util.listFunction(this.clean);
}

module.exports = Customer
