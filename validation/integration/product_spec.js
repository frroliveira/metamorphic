var Test = require('./test.js');
var Util = require('../util.js');

var schema = {
  id: Test.Ignore,
  name: String,
  description: String,
  cost: Number,
  isAvailable: Boolean,
  brand: String,
  category: Number
}

var getAllSchema = Util.clone(schema)
getAllSchema.isAvailable = function(value) {
  return value == true;
}

Test.create(function(frisby, endpoint) {

  var EntityModule = require('../entities/product.js');
  var Entity = new EntityModule(endpoint);
  
  var first = Entity.entity()
  var second = Entity.entity()

  frisby.create('Create a ' + Entity.name)
    .post(Entity.path, first)
    .expectStatus(201) // Created
    .expectJSONTypes(schema)
    .expectJSON(Test.addId(first))
    .afterJSON(function(created) {

      frisby.create('Get all ' + Entity.name)
        .get(Entity.path)
        .expectStatus(200) // Ok
        .expectJSONTypes('*', getAllSchema)
        .afterJSON(function(all) {

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
                .afterJSON(function(updated) {
              
                  frisby.create('Delete a ' + Entity.name)
                    .delete(Entity.path + created.id + '/')
                    .expectStatus(204) // No Content
                  .toss()
                })
              .toss()
            })
          .toss()
        })
      .toss()
    })
  .toss()
});
