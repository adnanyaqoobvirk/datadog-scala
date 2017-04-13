package test

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import org.yaqoob.datadog.common.Metric
import org.json4s._
import org.json4s.native.JsonMethods._
import org.specs2.mutable.Specification
import org.yaqoob.datadog.Client

import scala.concurrent.duration._
import scala.concurrent.Await

class MetricSpec extends Specification {

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

    "handle add metrics" in {
      val res = Await.result(client.addMetrics(
        series = Seq(
          Metric(
            name = "foo.bar.test",
            points = Seq((1412183578, 12.0), (1412183579, 123.0)),
            host = Some("poop.example.com"),
            tags = Some(Seq("tag1", "tag2:foo")),
            metricType = Some("gauge")
          ),
          Metric(
            name = "foo.bar.gorch",
            points = Seq((1412183580, 12.0), (1412183581, 123.0)),
            host = Some("poop2.example.com"),
            tags = Some(Seq("tag3", "tag3:foo")),
            metricType = Some("counter")
          )
        )
      ), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/series?api_key=apiKey&application_key=appKey")
      val entity = Await.result(Unmarshal(adapter.getRequest.get.entity).to[String], Duration(1, "second"))
      val body = parse(entity)
      val names = for {
        JObject(series) <- body
        JField("metric", JString(name)) <- series
      } yield name

      names must have size(2)
      names must contain(be_==("foo.bar.test")).exactly(1)
      names must contain(be_==("foo.bar.gorch")).exactly(1)

      val points = for {
        JObject(series) <- body
        JField("points", JArray(point)) <- series
      } yield point

      points must have size(2)
      points must contain(be_==(Seq(JArray(List(JInt(1412183578), JDouble(12.0))), JArray(List(JInt(1412183579), JDouble(123.0)))))).exactly(1)
      points must contain(be_==(Seq(JArray(List(JInt(1412183580), JDouble(12.0))), JArray(List(JInt(1412183581), JDouble(123.0)))))).exactly(1)


      adapter.getRequest must beSome.which(_.method == HttpMethods.POST)
    }

    "handle query timeseries" in {
      val res = Await.result(client.query(
        query = "system.cpu.idle{*}by{host}",
        from = 1470453155,
        to = 1470539518
      ), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      val params = adapter.getRequest.get.uri.query().toMap

      params must havePairs(
        "api_key" -> "apiKey",
        "application_key" -> "appKey",
        "query" -> "system.cpu.idle{*}by{host}",
        "from" -> "1470453155",
        "to" -> "1470539518"
      )

      adapter.getRequest must beSome.which(_.method == HttpMethods.GET)
    }
  }
}
