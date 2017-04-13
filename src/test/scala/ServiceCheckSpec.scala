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

class ServiceCheckSpec extends Specification {

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

    "handle add service check" in {
      val res = Await.result(
        client.addServiceCheck(
          check = "app.is_ok", hostName = "app1", status = 0
        ), Duration(5, "second")
      )

      res.statusCode must beEqualTo(200)

      val uri = adapter.getRequest.get.uri.toString
      uri must contain("https://app.datadoghq.com/api/v1/check_run")

      val params = adapter.getRequest.get.uri.query().toMap
      params must havePairs(
        "api_key" -> "apiKey",
        "application_key" -> "appKey",
        "check" -> "app.is_ok",
        "host_name" -> "app1",
        "status" -> "0"
      )

      adapter.getRequest must beSome.which(_.method == HttpMethods.POST)
    }
  }
}
