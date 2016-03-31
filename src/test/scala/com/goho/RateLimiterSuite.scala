package com.goho

import com.goho.service.{TaskFactory, RateLimiter}
import com.typesafe.scalalogging.slf4j.LazyLogging

import scalaz.concurrent.Task

/**
 * Created by yash.datta on 31/03/16.
 */
class RateLimiterSuite extends GoHoFunSuite
    with LazyLogging  {

  val validKey = "db78d85b7b27862779404c38abddd520"

  override def beforeAll(): Unit = {
    RateLimiter
  }

  override def afterAll(): Unit = {

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

  // test("Queries get Too many requests error in case ")

}
