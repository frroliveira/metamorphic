var Util = require('../util.js');
var Entity = require('./entity.js');

function Order(endpoint) {

  var ProductModule = require('./product.js');
  var Product = new ProductModule(endpoint);

  var CustomerModule = require('./customer.js');
  var Customer = new CustomerModule(endpoint);

  this.endpoint = endpoint;
  this.name = 'order';
  this.path = '/orders/';
  this.type = Entity.Type.hasBoth;
  
  var _entity = Entity.create(
    this.name,
    endpoint,
    this.path,
    [
      function() {
        return {
          reference: 'OR00001',
          datetime: '2015-12-03T15:23:00Z',
          shippingCost: 9.5,
          state: 1,
          products: Product.createIdList(3),
          customer: Customer.createId()
        }
      },
      function() {
        return {
          reference: 'OR00001',
          datetime: '2015-12-03T15:23:00Z',
          shippingCost: 9.5,
          state: 2,
          products: Product.createIdList(3),
          customer: Customer.createId()
        }
      }
    ]
  );

  this.entity = function(providers) { return _entity.entity(providers) }
  this.create = function() { return _entity.create() }
  this.createId = function() { return _entity.createId() }
  this.createList = function(length) { return _entity.createList(length) }
  this.createIdList = function(length) { return _entity.createIdList(length) }
  this.get = function(id) { return _entity.get(id) }
  this.getList = function(ids) { return _entity.getList(ids) }
  this.delete = function(id) { return _entity.delete(id) }
  this.deleteList = function(ids) { return _entity.deleteList(ids) }
  this.clean = function(order) {
    var products = Product.getList(order.products);
    var customer = Customer.get(order.customer);
    
    Product.deleteList(order.products);
    Customer.delete(order.customer);
    
    Product.cleanList(products);
    Customer.clean(customer);
    return;
  }
  this.cleanList = Util.listFunction(this.clean);
}

module.exports = Order
