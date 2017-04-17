[![Build Status](https://travis-ci.org/adnanyaqoobvirk/datadog-scala.png?branch=master)](https://travis-ci.org/adnanyaqoobvirk/datadog-scala)

# Datadog-Scala

A Scala library for interacting with the Datadog API using akka-http.

As of October 2014 this library covers all the methods in the [Datadog API Documentation](http://docs.datadoghq.com/api/).

# Example

```scala
import org.yaqoob.datadog.Client
import org.yaqoob.datadog.HttpAdapter

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

implicit val defaultActorSystem = ActorSystem()
implicit val defaultMaterializer = ActorMaterializer()
implicit val executionContext = defaultActorSystem.dispatcher
    
val adapter = new HttpAdapter()
val client = new Client(apiKey = "XXX", appKey = "XXX", httpAdapter = adapter)
client.getAllTimeboards.foreach({ response =>
    println(response.body)
})
```
