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

class CommentSpec extends Specification {

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

    "handle add comment" in {
      val res = Await.result(client.addComment(
        message = "hello", handle = Some("handul"), relatedEventId = Some(12345)
      ), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/comments?api_key=apiKey&application_key=appKey")
      val entity = Await.result(Unmarshal(adapter.getRequest.get.entity).to[String], Duration(1, "second"))
      val body = parse(entity)
      (body \ "message").extract[String] must beEqualTo("hello")
      (body \ "handle").extract[String] must beEqualTo("handul")
      (body \ "related_event_id").extract[Long] must beEqualTo(12345)

      adapter.getRequest must beSome.which(_.method == HttpMethods.POST)
    }

    "handle update comment" in {
      val res = Await.result(client.updateComment(
        commentId = 12345, message = Some("hello"), handle = Some("handul")
      ), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/comments/12345?api_key=apiKey&application_key=appKey")
      val entity = Await.result(Unmarshal(adapter.getRequest.get.entity).to[String], Duration(1, "second"))
      val body = parse(entity)
      (body \ "message").extract[String] must beEqualTo("hello")
      (body \ "handle").extract[String] must beEqualTo("handul")

      adapter.getRequest must beSome.which(_.method == HttpMethods.PUT)
    }

    "handle delete comment" in {
      val res = Await.result(client.deleteComment(12345), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/comments/12345?api_key=apiKey&application_key=appKey")

      adapter.getRequest must beSome.which(_.method == HttpMethods.DELETE)
    }
  }
}