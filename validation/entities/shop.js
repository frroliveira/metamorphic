var Util = require('../util.js');
var Entity = require('./entity.js');

function Shop(endpoint) {

  var ProductModule = require('./product.js');
  var Product = new ProductModule(endpoint);

  this.endpoint = endpoint;
  this.name = 'shop';
  this.path = '/shops/';
  this.type = Entity.Type.hasList;
  
  var productsField = endpoint.framework === 'LoopBack' ? 'productIds' : 'products'
  
  var _entity = Entity.create(
    this.name,
    endpoint,
    this.path,
    [
      function() {
        var product = {
          opens: '2000-01-01T08:00:00Z',
          closes: '2000-01-01T21:00:00Z'
        }
        product[productsField] = Product.createIdList(3);
        return product;
      },
      function() {
        var product = {
          opens: '2000-01-01T08:00:00Z',
          closes: '2000-01-01T21:00:00Z'
        }
        product[productsField] = Product.createIdList(3);
        return product;
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
  this.clean = function(shop) {
    var products = Product.getList(shop[productsField]);
    Product.deleteList(shop[productsField]);
    Product.cleanList(products);
    return;
  }
  this.cleanList = Util.listFunction(this.clean);
}

module.exports = Shop
