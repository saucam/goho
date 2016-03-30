package com.goho.service

import com.goho.conf.GoHoConf._
import com.goho.service.db.HotelDataBase

import org.http4s.{AttributeKey, HttpService}
import org.http4s.server._
import org.http4s.dsl._
import org.http4s.server.middleware.authentication.DigestAuthentication
import org.http4s.server.middleware.authentication._

import scala.concurrent.ExecutionContext
import scalaz.concurrent.Task
/**
 * Created by yash.datta on 30/03/16.
 */
object GoHoService  {

  HotelDataBase.init()

  // A Router can mount multiple services to prefixes.  The request is passed to the
  // service with the longest matching prefix.
  def service(implicit executionContext: ExecutionContext = ExecutionContext.global): HttpService = Router(
    "" -> gohoService
  )

  def gohoService(implicit executionContext: ExecutionContext = gohoExecutorService) = HttpService {
    case req @ GET -> Root =>
      Ok("")
    case req @ GET -> Root / "getHotelsByCity" / city / key => {
      //val city = req.attributes.get
      val records = Task.now(HotelDataBase.getOrderedRecords(city)).run
      val output = records.mkString("\n")
      Ok(s"${output}")
    }
  }

  // Services can be protected using HTTP authentication.
  /*val realm = "testrealm"

  def auth_store(r: String, u: String) = if (r == realm && u == "username") Task.now(Some("password"))
  else Task.now(None)

  val digest = new DigestAuthentication(realm, auth_store)

  def gohoService = digest(basicService)*/

}
