package test

import akka.actor.ActorSystem
import org.yaqoob.datadog._
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import org.json4s.DefaultFormats
import org.specs2.mutable.Specification
import org.yaqoob.datadog.Client

import scala.concurrent.duration._
import scala.concurrent.Await

class AlertSpec extends Specification {

  implicit val formats = DefaultFormats

  // Sequential because it's less work to share the client instance
  sequential

  "Client" should {

    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val adapter = new OkHttpAdapter()
    val client = new Client(
      apiKey = "apiKey",
      appKey = "appKey",
      httpAdapter = adapter
    )

    "handle get all alerts" in {
      val res = Await.result(client.getAllAlerts, Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/alert?api_key=apiKey&application_key=appKey")
      adapter.getRequest must beSome.which(_.method == HttpMethods.GET)
    }

    "handle add alert" in {
      val res = Await.result(client.addAlert("POOP"), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/alert?api_key=apiKey&application_key=appKey")
      Await.result(Unmarshal(adapter.getRequest.get.entity).to[String], Duration(1, "second")) shouldEqual "POOP"

      adapter.getRequest must beSome.which(_.method == HttpMethods.POST)
    }

    "handle get alert" in {
      val res = Await.result(client.getAlert(12345), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/alert/12345?api_key=apiKey&application_key=appKey")
      adapter.getRequest must beSome.which(_.method == HttpMethods.GET)
    }

    "handle delete alert" in {
      val res = Await.result(client.deleteAlert(12345), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/alert/12345?api_key=apiKey&application_key=appKey")

      adapter.getRequest must beSome.which(_.method == HttpMethods.DELETE)
    }

    "handle update screenboard" in {
      val res = Await.result(client.updateAlert(12345, "POOP"), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/alert/12345?api_key=apiKey&application_key=appKey")
      Await.result(Unmarshal(adapter.getRequest.get.entity).to[String], Duration(1, "second")) shouldEqual "POOP"

      adapter.getRequest must beSome.which(_.method == HttpMethods.PUT)
    }

    "handle mute all alerts" in {
      val res = Await.result(client.muteAllAlerts, Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/mute_alerts?api_key=apiKey&application_key=appKey")

      adapter.getRequest must beSome.which(_.method == HttpMethods.POST)
    }

    "handle unmute all alerts" in {
      val res = Await.result(client.unmuteAllAlerts, Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/unmute_alerts?api_key=apiKey&application_key=appKey")

      adapter.getRequest must beSome.which(_.method == HttpMethods.POST)
    }
  }
}