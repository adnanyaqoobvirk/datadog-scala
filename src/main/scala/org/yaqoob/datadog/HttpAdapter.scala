package org.yaqoob.datadog

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.Uri.{Authority, Host, Path, Query}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util.Timeout

import org.yaqoob.datadog.common.Response

import scala.concurrent.{ExecutionContextExecutor, Future}

/**
  * Created by Adnan Yaqoob on 13/04/2017.
  */
class HttpAdapter(
                   httpTimeoutSeconds: Int = 10
                 )(implicit system: ActorSystem, materializer: ActorMaterializer, executtionContext: ExecutionContextExecutor) {

  // Akka's Ask pattern requires an implicit timeout to know
  // how long to wait for a response.
  implicit val timeout = Timeout(httpTimeoutSeconds, TimeUnit.SECONDS)

  val httpPool = Http().superPool[Unit]()

  def doRequest(
                 scheme: String,
                 authority: String,
                 path: String,
                 method: String,
                 body: Option[String] = None,
                 params: Map[String,Option[String]] = Map.empty,
                 contentType: String = "json"): Future[Response] = {

    // Turn a map of string,opt[string] into a map of string,string which is
    // what Query wants
    val filteredParams = params.filter(
      // Filter out keys that are None
      _._2.isDefined
    ).map(
      // Convert the remaining tuples to str,str
      param => param._1 -> param._2.get
    )
    // Make a Uri
    val finalUrl = Uri(
      scheme = scheme,
      authority = Authority(host = Host(authority)),
      path = Path("/api/v1/" + path)
    ).withQuery(Query(filteredParams))

    // Use the provided case classes from spray-client
    // to construct an HTTP request of the type needed.
    val httpRequest: HttpRequest = method match {
      case "DELETE" => RequestBuilding.Delete(finalUrl, body)
      case "GET" => RequestBuilding.Get(finalUrl, body)
      case "POST" => contentType match {
        case "json" => RequestBuilding.Post(finalUrl, body.map({
          b => HttpEntity(ContentTypes.`application/json`, b)
        }).getOrElse(HttpEntity.Empty))
        case _ => {
          // This is going to be a form-encoded post. There's only one
          // API call that works this way (ugh) so I'm not going to worry
          // too much about making this work as cleanly as the rest of the
          // stuff. (IMO)
          val formUrl = Uri(
            scheme = scheme,
            authority = Authority(host = Host(authority)),
            path = Path("/api/v1/" + path)
          )

          RequestBuilding.Post(formUrl, FormData(filteredParams))
        }
      }
      case "PUT" => RequestBuilding.Put(finalUrl, HttpEntity(ContentTypes.`application/json`, body.get))
      case _ => throw new IllegalArgumentException("Unknown HTTP method: " + method)
    }

    system.log.debug("%s: %s".format(method, finalUrl))
    // For spelunkers, the ? is a function of the Akka "ask pattern". Unlike !
    // it waits for a response in the form of a future. In this case we're
    // sending along a case class representing the type of HTTP request we want
    // to do and something down in the guts of the actors handles it and gets
    // us a response.
    doHttp(httpRequest)
  }

  def doHttp(request: HttpRequest): Future[Response] = {
    Source.single((request, ()))
      .via(httpPool)
      .map { case (response, _) => response.get }
      .runWith(Sink.head)
      .flatMap(
        res =>
          Unmarshal(res.entity).to[String].map(
            entity =>
              Response(
                statusCode = res.status.intValue,
                entity
              )
          )
      )
  }
}
