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


class EventSpec extends Specification {

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

    "handle get event" in {
      val res = Await.result(client.getEvent(12345), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/events/12345?api_key=apiKey&application_key=appKey")
      adapter.getRequest must beSome.which(_.method == HttpMethods.GET)
    }

    "handle add event" in {
      val res = Await.result(client.addEvent(title = "poop", text = "fart"), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/events?api_key=apiKey&application_key=appKey")
      val entity = Await.result(Unmarshal(adapter.getRequest.get.entity).to[String], Duration(1, "second"))
      val body = parse(entity)
      (body \ "title").extract[String] must beEqualTo("poop")
      (body \ "text").extract[String] must beEqualTo("fart")

      adapter.getRequest must beSome.which(_.method == HttpMethods.POST)
    }

    "handle get events" in {
      val res = Await.result(client.getEvents(start = 12345, end = 12346), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      val uri = adapter.getRequest.get.uri.toString
      uri must contain("https://app.datadoghq.com/api/v1/events")
      uri must contain("end=12346")
      uri must contain("start=12345")
      uri must contain("api_key=apiKey")
      uri must contain("application_key=appKey")
    }

    "handle delete events" in {
      val res = Await.result(client.deleteEvent(eventId = 123456), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      val uri = adapter.getRequest.get.uri.toString
      uri must contain("https://app.datadoghq.com/api/v1/events/123456")
      uri must contain("api_key=apiKey")
      uri must contain("application_key=appKey")
    }
  }
}
