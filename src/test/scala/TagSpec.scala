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

class TagSpec extends Specification {

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

    "handle get all tags" in {
      val res = Await.result(client.getAllTags, Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/tags/hosts?api_key=apiKey&application_key=appKey")
      adapter.getRequest must beSome.which(_.method == HttpMethods.GET)
    }

    "handle add tags for host" in {
      val res = Await.result(client.addTags("12345", Seq("foo:bar", "butt")), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/tags/hosts/12345?api_key=apiKey&application_key=appKey")

      val entity = Await.result(Unmarshal(adapter.getRequest.get.entity).to[String], Duration(1, "second"))
      val body = parse(entity)
      (body \ "tags")(0).extract[String] must beEqualTo("foo:bar")
      (body \ "tags")(1).extract[String] must beEqualTo("butt")

      adapter.getRequest must beSome.which(_.method == HttpMethods.POST)
    }

    "handle get tags for host" in {
      val res = Await.result(client.getTags("12345"), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/tags/hosts/12345?api_key=apiKey&application_key=appKey")
      adapter.getRequest must beSome.which(_.method == HttpMethods.GET)
    }

    "handle delete tags" in {
      val res = Await.result(client.deleteTags("12345"), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/tags/hosts/12345?api_key=apiKey&application_key=appKey")

      adapter.getRequest must beSome.which(_.method == HttpMethods.DELETE)
    }
  }
}