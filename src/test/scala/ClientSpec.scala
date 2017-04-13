package test

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpRequest
import akka.pattern.AskTimeoutException
import akka.stream.ActorMaterializer
import org.yaqoob.datadog.Client
import org.yaqoob.datadog.common.Response
import org.json4s.DefaultFormats
import org.specs2.mutable.Specification
import org.yaqoob.datadog.{Client, HttpAdapter}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.util.Try

class ClientSpec extends Specification {

  class FiveHundredHttpAdapter()(implicit system: ActorSystem, materializer: ActorMaterializer, executtionContext: ExecutionContextExecutor) extends HttpAdapter{

    override def doHttp(request: HttpRequest): Future[Response] = {
      Future {
        Response(500, "Internal Server Error")
      }
    }
  }

  class SlowHttpAdapter()(implicit system: ActorSystem, materializer: ActorMaterializer, executtionContext: ExecutionContextExecutor) extends HttpAdapter {

    override def doHttp(request: HttpRequest) = {
      Future.failed(new AskTimeoutException("I timed out!"))
    }
  }

  implicit val formats = DefaultFormats

  // Sequential because it's less work to share the client instance
  sequential

  "Client with custom HttpAdapter" should {

    implicit val defaultActorSystem = ActorSystem()
    implicit val defaultMaterializer = ActorMaterializer()
    implicit val executionContext = defaultActorSystem.dispatcher

    "handle user-supplied actor system" in {
      val adapter = new HttpAdapter()
      val attempt = Try({
        val client = new Client(
          apiKey = "abc",
          appKey = "123",
          httpAdapter = adapter
        )
      })
      attempt must beSuccessfulTry
    }
  }

  "Client 500 failures" should {

    implicit val defaultActorSystem = ActorSystem()
    implicit val defaultMaterializer = ActorMaterializer()
    implicit val executionContext = defaultActorSystem.dispatcher

    val adapter = new FiveHundredHttpAdapter()
    val client = new Client(
      apiKey = "abc",
      appKey = "123",
      httpAdapter = adapter
    )

    "handle 500" in {
      val res = Await.result(client.getAllTimeboards, Duration(5, "second"))

      res.statusCode must beEqualTo(500)
    }
  }

  "Client future failures" should {

    implicit val defaultActorSystem = ActorSystem()
    implicit val defaultMaterializer = ActorMaterializer()
    implicit val executionContext = defaultActorSystem.dispatcher

    val adapter = new SlowHttpAdapter()
    val client = new Client(
      apiKey = "abc",
      appKey = "123",
      httpAdapter = adapter
    )

    "handle timeout" in {
      Await.result(client.getAllTimeboards, Duration(10, "second")) must throwA[AskTimeoutException]
    }
  }
}