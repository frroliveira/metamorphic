function Endpoint(baseUri, framework, options) {
  this.baseUri = baseUri;
  this.framework = framework;
  this.options = options;
}

var _create = function(baseUri, framework, options) {
  return new Endpoint(baseUri, framework, options);
}

var options = {
  idField: 'id'
}

var eveOptions = {
  idField: '_id'
}

module.exports.metamorphic =
  _create('http://localhost:8080', 'Metamorphic', options);
module.exports.metamorphic_async =
  _create('http://localhost:8080', 'Metamorphic Async', options);
module.exports.django_rest =
  _create('http://localhost/django-rest', 'Django Rest', options);
module.exports.django_rest_34 =
  _create('http://localhost/django-rest-3.4', 'Django Rest 3.4', options);
module.exports.eve =
  _create('http://localhost:5000', 'Eve', eveOptions);
module.exports.loopback =
  _create('http://localhost:3000/api', 'LoopBack', options);
module.exports.sails =
  _create('http://localhost:1337', 'Sails', options);
  
module.exports.metamorphic_online =
  _create('http://176.111.107.16:8080', 'Metamorphic Online', options);
module.exports.metamorphic_async_online =
  _create('http://localhost:8080', 'Metamorphic Async Online', options);
module.exports.django_rest_online =
  _create('http://176.111.108.44/django-rest', 'Django Rest Online', options);
module.exports.django_rest_34_online =
  _create('http://176.111.107.16/django-rest-3.4', 'Django Rest 3.4 Online', options);

module.exports.All = [
  module.exports.metamorphic,
  module.exports.metamorphic_async,
  module.exports.django_rest,
  module.exports.django_rest_34,
  module.exports.eve,
  module.exports.loopback,
  module.exports.sails,
  module.exports.metamorphic_online,
  module.exports.metamorphic_async_online,
  module.exports.django_rest_online,
  module.exports.django_rest_34_online
]

module.exports.Default = module.exports.django_rest_34;
