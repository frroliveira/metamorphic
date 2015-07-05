var Util = require('../util.js');
var Entity = require('./entity.js');

function Product(endpoint) {

  var CategoryModule = require('./category.js');
  var Category = new CategoryModule(endpoint);

  this.endpoint = endpoint;
  this.name = 'product';
  this.path = '/products/';
  this.type = Entity.Type.hasObject;
  
  var _entity = Entity.create(
    this.name,
    endpoint,
    this.path,
    [
      function() {
        return {
          name: 'Canon 550D',
          description: 'Short description.',
          cost: 699.5,
          isAvailable: Math.random() < .5,
          brand: 'Canon',
          category: Category.createId()
        }
      },
      function() {
        return {
          name: 'Canon EOS 550D',
          description: 'Longer Canon 550D description.',
          cost: 689.5,
          isAvailable: Math.random() < .5,
          brand: 'Canon',
          category: Category.createId()
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
  this.clean = function(product) {
    var category = Category.get(product.category);
    Category.delete(product.category);
    Category.clean(category);
    return;
  }
  this.cleanList = Util.listFunction(this.clean);
}

module.exports = Product
