package org.yaqoob.datadog

/**
  * Created by Adnan Yaqoob on 13/04/2017.
  */
package object common {

  case class Metric(
                     name: String,
                     points: Seq[(Long,Double)],
                     metricType: Option[String] = None,
                     tags: Option[Seq[String]] = None,
                     host: Option[String]
                   )

  case class Response(
                       statusCode: Int,
                       body: String
                     )

}
