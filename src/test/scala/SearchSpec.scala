package test

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpMethods
import akka.stream.ActorMaterializer
import org.json4s._
import org.json4s.native.JsonMethods._
import org.specs2.mutable.Specification
import org.yaqoob.datadog.Client

import scala.concurrent.duration._
import scala.concurrent.Await

class SearchSpec extends Specification {

  implicit val formats = DefaultFormats

  // Sequential because it's less work to share the client instance
  sequential

  "Client" should {

    implicit val defaultActorSystem = ActorSystem()
    implicit val defaultMaterializer = ActorMaterializer()
    implicit val executionContext = defaultActorSystem.dispatcher

    val adapter = new OkHttpAdapter()
    val client = new Client(
      apiKey = "apiKey",
      appKey = "appKey",
      httpAdapter = adapter
    )

    "handle search" in {
      val res = Await.result(client.search("poop"), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/search?q=poop&api_key=apiKey&application_key=appKey")
      adapter.getRequest must beSome.which(_.method == HttpMethods.GET)
    }
  }
}