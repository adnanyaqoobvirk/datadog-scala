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
import scala.util.Try

class ScreenboardSpec extends Specification {

  implicit val formats = DefaultFormats

  // Sequential because it's less work to share the client instance
  sequential

  "Screenboard Client" should {

    implicit val defaultActorSystem = ActorSystem()
    implicit val defaultMaterializer = ActorMaterializer()
    implicit val executionContext = defaultActorSystem.dispatcher

    val adapter = new OkHttpAdapter()
    val client = new Client(
      apiKey = "apiKey",
      appKey = "appKey",
      httpAdapter = adapter
    )

    "handle get all screenboards" in {
      val res = Await.result(client.getAllScreenboards, Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/screen?api_key=apiKey&application_key=appKey")
      adapter.getRequest must beSome.which(_.method == HttpMethods.GET)
    }

    "handle add screenboard" in {
      val res = Await.result(client.addScreenboard("POOP"), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/screen?api_key=apiKey&application_key=appKey")
      Await.result(Unmarshal(adapter.getRequest.get.entity).to[String], Duration(1, "second")) shouldEqual "POOP"

      adapter.getRequest must beSome.which(_.method == HttpMethods.POST)
    }

    "handle get screenboard" in {
      val res = Await.result(client.getScreenboard(12345), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/screen/12345?api_key=apiKey&application_key=appKey")
      adapter.getRequest must beSome.which(_.method == HttpMethods.GET)
    }

    "handle delete screenboard" in {
      val res = Await.result(client.deleteScreenboard(12345), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/screen/12345?api_key=apiKey&application_key=appKey")

      adapter.getRequest must beSome.which(_.method == HttpMethods.DELETE)
    }

    "handle update screenboard" in {
      val res = Await.result(client.updateScreenboard(12345, "POOP"), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/screen/12345?api_key=apiKey&application_key=appKey")
      Await.result(Unmarshal(adapter.getRequest.get.entity).to[String], Duration(1, "second")) shouldEqual "POOP"

      adapter.getRequest must beSome.which(_.method == HttpMethods.PUT)
    }
  }
}