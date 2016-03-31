package com.goho.service

import com.goho.conf.GoHoConf._
import com.goho.service.db.HotelDataBase
import org.http4s.headers.{Authorization, `WWW-Authenticate`}
import org.http4s.util.CaseInsensitiveString

import org.http4s.{Challenge, AttributeKey, HttpService}
import org.http4s._
import org.http4s.server._
import org.http4s.dsl._
import org.http4s.server.middleware.authentication.DigestAuthentication
import org.http4s.server.middleware.authentication._

import scala.concurrent.ExecutionContext
import scalaz.concurrent.Task
/**
 * Created by yash.datta on 30/03/16.
 */
object GoHoService extends AuthorizeKey {

  HotelDataBase.init()

  val authHeader = CaseInsensitiveString("Authorization")

  def gohoService(implicit executionContext: ExecutionContext = gohoExecutorService) = HttpService {
    case req @ GET -> Root =>
      Ok("")
    case req @ GET -> Root / "getHotelsByCity" / city => {
      req.authType match {
        case p @ Some(AuthScheme.Bearer) =>
          val key = req.headers.get(authHeader).get.value.split(" ").last
          if (accept(key)) {
            try {
              val records = TaskFactory.getTask(HotelDataBase.getRecords(city)).run
              val output = records.mkString("\n")
              Ok(s"${output}")
            } catch {
              case e: Exception =>
                BadRequest(s"Exception Occurred: ${e.toString}")
            }
          } else {
            Task.now(Response(Status.Unauthorized).putHeaders(Authorization(OAuth2BearerToken(key))))
          }
        // Does not entertain requests for any other auth schemes for now
        case Some(_) | None =>
          Forbidden("")
      }
    }
  }
}
