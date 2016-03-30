package com.goho

import com.goho.service.GoHoService
import com.typesafe.scalalogging.slf4j.LazyLogging
import org.http4s.HttpService
import org.http4s.client.blaze.PooledHttp1Client
import org.http4s.dsl._

import scalaz.concurrent.Task

/**
 * Created by yash.datta on 30/03/16.
 */
class GoHoGetSuite extends GoHoFunSuite
    with LazyLogging  {

  var server: HServer = new HServer(GoHoService.gohoService)

  val client = PooledHttp1Client()

  override def beforeAll(): Unit = {
    server.start
  }

  override def afterAll(): Unit = {
    server.stop
  }

  test("Simple Get for GOHO Service") {

    def getRecords(city: String): Task[String] = {
      val target = uri("http://localhost:8080/getHotelsByCity/") / city / "RandomKey"
      client.getAs[String](target)
    }

    val cities = Vector("Bangkok", "Amsterdam", "Ashburn")
    // people: scala.collection.immutable.Vector[String] = Vector(Michael, Jessica, Ashley, Christopher)

    val recordsList = Task.gatherUnordered(cities.map(getRecords))
    logger.info(recordsList.run.mkString("\n\n"))

    client.shutdownNow

  }

}
