var Test = require('./test.js');

var schema = {
  id: Test.Ignore,
  reference: String,
  datetime: String,
  shippingCost: Number,
  state: Number,
  products: Test.NumberArray,
  customer: Number
}

Test.create(function(frisby, endpoint) {

  var EntityModule = require('../entities/order.js');
  var Entity = new EntityModule(endpoint);

  var CustomerModule = require('../entities/customer.js');
  var Customer = new CustomerModule(endpoint);
  
  var first = Entity.entity();
  var second = Entity.entity();
  var invalid = Entity.entity([function() {
    return {
      reference: 'OR00002',
      datetime: '2015-12-03T15:23:00Z',
      shippingCost: 9.5,
      state: 2,
      products: [],
      customer: Customer.createId()
    }
  }]);
  
  frisby.create('Get all ' + Entity.name)
    .get(Entity.path)
    .expectStatus(405) // Method Not Allowed
  .toss();
  
  frisby.create('Create a ' + Entity.name)
    .post(Entity.path, first)
    .expectStatus(201) // Created
    .expectJSONTypes(schema)
    .expectJSON(Test.addId(first))
    .afterJSON(function(created) {
      
      frisby.create('Delete a ' + Entity.name)
        .delete(Entity.path + created.id + '/')
        .expectStatus(405) // Method Not Allowed
      .toss()

      frisby.create('Get a ' + Entity.name)
        .get(Entity.path + created.id + '/')
        .expectStatus(200) // Ok
        .expectJSONTypes(schema)
        .expectJSON(Test.addId(first, created.id))
        .afterJSON(function(retrieved) {
        
          frisby.create('Update a ' + Entity.name)
            .put(Entity.path + created.id + '/', second)
            .expectStatus(200) // Ok
            .expectJSONTypes(schema)
            .expectJSON(Test.addId(second, created.id))
          .toss()
        })
      .toss()
    })
  .toss();
  
  frisby.create('Create a invalid ' + Entity.name)
    .post(Entity.path, invalid)
    .expectStatus(400) // BadRequest
    .after(function(err, res, body) {
      var message = "Order must have at least one product";
      
      if (body.non_field_errors === undefined) {
        expect(body).toBe(message);
      } else {
        expect(body.non_field_errors[0]).toBe(message);
      }  
    })
  .toss();
});
