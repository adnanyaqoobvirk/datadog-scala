package test

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpRequest
import akka.stream.ActorMaterializer
import org.yaqoob.datadog.common.Response
import org.yaqoob.datadog.HttpAdapter

import scala.concurrent.{ExecutionContextExecutor, Future}

class OkHttpAdapter()(implicit system: ActorSystem, materializer: ActorMaterializer, executtionContext: ExecutionContextExecutor) extends HttpAdapter {
  var lastRequest: Option[HttpRequest] = None

  override def doHttp(request: HttpRequest) = {
    lastRequest = Some(request)
    Future { Response(200, "Ok") }
  }

  def getRequest = lastRequest
}
