var Test = require('./test.js');

var schema = {
  id: Test.Ignore,
  name: String,
  description: String
}

Test.create(function(frisby, endpoint) {

  var EntityModule = require('../entities/category.js');
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
        .expectJSONTypes('*', schema)
        .afterJSON(function(all) {

          frisby.create('Get a ' + Entity.name)
            .get(Entity.path + created.id + '/')
            .expectStatus(405) // Method Not Allowed
          .toss()
          
          frisby.create('Update a ' + Entity.name)
            .put(Entity.path + created.id + '/', second)
            .expectStatus(405) // Method Not Allowed
          .toss()
          
          frisby.create('Delete a ' + Entity.name)
            .delete(Entity.path + created.id + '/')
            .expectStatus(204) // No Content
          .toss()
        })
      .toss()
    })
  .toss()
});
