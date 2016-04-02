package com.goho.service

import com.goho.conf.GoHoConf
import com.goho.conf.GoHoConf._
import com.goho.service.db.{CitySearchResponse, HotelDataBase}
import org.http4s.headers.{Authorization, `WWW-Authenticate`}
import org.http4s.util.CaseInsensitiveString

import org.http4s._
import org.http4s.dsl._

import scala.concurrent.ExecutionContext
import scalaz.concurrent.Task
/**
 * Created by yash.datta on 30/03/16.
 */
/**
 * Main service class
 */
class GoHoService extends AuthorizeKey
    with GoHoConf {

  val dbService = new HotelDataBase
  // Initialize database service
  dbService.init

  val authHeader = CaseInsensitiveString("Authorization")
  val rateLimiter = new RateLimiter(refreshRate, enableRate)

  def gohoService(implicit executionContext: ExecutionContext = gohoExecutorService) = HttpService {
    case req @ GET -> Root =>
      Ok("")
    case req @ GET -> Root / "getHotelsByCity" / city => {
      req.authType match {
        case p @ Some(AuthScheme.Bearer) =>
          val key = req.headers.get(authHeader).get.value.split(" ").last
          // Its a valid key!
          if (accept(key)) {
            // apply rate limit
            if (rateLimiter.accept(key)) {
              try {
                val response = TaskFactory.getTask(dbService.getRecords(city)).run
                val output = GoHoConf.json.toJson(response)
                Ok(s"${output}")
              } catch {
                case e: Exception =>
                  BadRequest(s"Exception Occurred: ${e.toString}")
              }
            } else {
              TooManyRequests(s"Too many request for key ${key}")
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
