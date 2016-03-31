package com.goho

import com.goho.service.{TestGoHoService, TaskFactory, RateLimiter}
import com.typesafe.scalalogging.slf4j.LazyLogging
import org.http4s.headers.Authorization
import org.http4s.{OAuth2BearerToken, Headers, Request}
import org.http4s.client.blaze.PooledHttp1Client
import org.http4s.dsl._
import org.scalatest.Inspectors.forAll

import scalaz.concurrent.Task

/**
 * Created by yash.datta on 31/03/16.
 */
class RateLimiterSuite extends GoHoFunSuite
    with LazyLogging  {

  val validKey = "db78d85b7b27862779404c38abddd520"
  val validKeyList = List("db78d85b7b27862779404c38abddd520", "3c1dd592ad4a4958c9efc7eb98274f0d")

  val service = new TestGoHoService
  val server = new HServer(service.gohoService)
  val client = PooledHttp1Client()

  override def beforeAll(): Unit = {
    server.start
  }

  override def afterAll(): Unit = {
    client.shutdownNow
    server.stop
  }

  test("Key should not get disabled on requests = ") {

    val rateLimiter = new RateLimiter(10)

    import com.goho.service.AuthorizedKeys._
    RateLimiter.rateMap.put(keyMap(validKey), 5)

    val tasks =
    for(i <- 1 to 5) yield {
      TaskFactory.getTask {
        rateLimiter.accept(validKey)
      }
    }

    val results = Task.gatherUnordered(tasks).run

    results should not contain (false)
  }

  test("Key should get disabled on requests > configured limit") {
    val rateLimiter = new RateLimiter(10)

    import com.goho.service.AuthorizedKeys._

    RateLimiter.rateMap.put(keyMap(validKey), 5)
    val tasks =
      for(i <- 1 to 6) yield {
        TaskFactory.getTask {
          rateLimiter.accept(validKey)
        }
      }

    val results = Task.gatherUnordered(tasks).run

    results should contain (false)
  }

  test("If requests are sent after timeout time, they should be served") {

    val refreshTime = 3
    val timeOut = 7
    val rateLimiter = new RateLimiter(refreshTime, timeOut)

    import com.goho.service.AuthorizedKeys._

    RateLimiter.rateMap.put(keyMap(validKey), 5)
    val tasks =
      for(i <- 1 to 5) yield {
        TaskFactory.getTask {
          rateLimiter.accept(validKey)
        }
      }

    val results = Task.gatherUnordered(tasks).run

    results should not contain (false)

    // Now key gets disabled
    rateLimiter.accept(validKey) should equal (false)

    // Sleep for configured time
    Thread.sleep((timeOut+1)*1000)

    // Now get served
    val sresults = Task.gatherUnordered(tasks).run
    sresults should not contain (false)

  }

  test("Queries within rate limit return ok") {

    val target = uri("http://localhost:8080/getHotelsByCity") / "Bangkok"
    val keyList = Array(List.fill(5)(validKeyList(0)), List.fill(5)(validKeyList(1))).flatMap(x=>x)
    val reqs = keyList.map(x => Request(uri = target, headers = Headers(Authorization(OAuth2BearerToken(x)))))

    val tasks = reqs.map(x => TaskFactory.getTask({
      val resp = client.toHttpService(x).run
      resp.status
    }))

    val statuses = Task.gatherUnordered(tasks).run
    forAll (statuses) { x =>
      x should equal (Ok)
    }
  }

}
