package com.goho

import com.typesafe.scalalogging.slf4j.LazyLogging
import org.http4s.HttpService
import org.http4s.dsl._

import org.http4s.client.blaze._

import scalaz.concurrent.Task

/**
 * Created by yash.datta on 30/03/16.
 */
class SimpleGetSuite extends GoHoFunSuite
    with LazyLogging {

  var server: HServer = _
  val client = PooledHttp1Client()

  override def beforeAll(): Unit = {

    val service = HttpService {
      case GET -> Root / "hello" / name =>
        Ok(s"Hello, $name.")
    }

    server = new HServer(service)
    server.start
  }

  override def afterAll(): Unit = {
    server.stop
  }

  test("Simple Rest Call") {

    def hello(name: String): Task[String] = {
      val target = uri("http://localhost:8080/hello/") / name
      client.getAs[String](target)
    }

    val people = Vector("Michael", "Jessica", "Ashley", "Christopher")
    // people: scala.collection.immutable.Vector[String] = Vector(Michael, Jessica, Ashley, Christopher)

    val greetingList = Task.gatherUnordered(people.map(hello))
    logger.info(greetingList.run.mkString("\n"))

    client.shutdownNow

  }

}
