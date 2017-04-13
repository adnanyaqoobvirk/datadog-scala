package test

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import org.json4s._
import org.json4s.native.JsonMethods._
import org.specs2.mutable.Specification
import org.yaqoob.datadog.Client

import scala.concurrent.duration._
import scala.concurrent.Await

class TimeboardSpec extends Specification {

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

    "handle get all timeboards" in {
      val res = Await.result(client.getAllTimeboards, Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/dash?api_key=apiKey&application_key=appKey")
      adapter.getRequest must beSome.which(_.method == HttpMethods.GET)
    }

    "handle add timeboard" in {
      val res = Await.result(client.addTimeboard("POOP"), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/dash?api_key=apiKey&application_key=appKey")
      Await.result(Unmarshal(adapter.getRequest.get.entity).to[String], Duration(1, "second")) shouldEqual "POOP"

      adapter.getRequest must beSome.which(_.method == HttpMethods.POST)
    }

    "handle get timeboard" in {
      val res = Await.result(client.getTimeboard(12345), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/dash/12345?api_key=apiKey&application_key=appKey")
      adapter.getRequest must beSome.which(_.method == HttpMethods.GET)
    }

    "handle delete timeboard" in {
      val res = Await.result(client.deleteTimeboard(12345), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/dash/12345?api_key=apiKey&application_key=appKey")

      adapter.getRequest must beSome.which(_.method == HttpMethods.DELETE)
    }

    "handle update timeboard" in {
      val res = Await.result(client.updateTimeboard(12345, "POOP"), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/dash/12345?api_key=apiKey&application_key=appKey")
      Await.result(Unmarshal(adapter.getRequest.get.entity).to[String], Duration(1, "second")) shouldEqual "POOP"

      adapter.getRequest must beSome.which(_.method == HttpMethods.PUT)
    }
  }
}