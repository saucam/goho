package com.goho

import com.goho.service.{RateLimiter, TestGoHoService, GoHoService}
import com.goho.service.db.{CitySearchResponse, HotelRecord}
import com.goho.conf.GoHoConf._
import com.typesafe.scalalogging.slf4j.LazyLogging
import org.http4s.headers.Authorization
import org.http4s.{OAuth2BearerToken, Headers, Request, HttpService}
import org.http4s.client.blaze.PooledHttp1Client
import org.http4s.dsl._

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import scalaz.Nondeterminism
import scalaz.concurrent.Task

/**
 * Created by yash.datta on 30/03/16.
 */
class GoHoGetSuite extends GoHoFunSuite
    with LazyLogging  {

  val service = new TestGoHoService
  val validKey = "db78d85b7b27862779404c38abddd520"
  val invalidKey = "SomeRandomKey"

  // Increasing rate limit for a valid key for testing
  import com.goho.service.AuthorizedKeys._
  RateLimiter.rateMap.put(keyMap(validKey), 10)
  var server: HServer = new HServer(service.gohoService)

  val client = PooledHttp1Client()

  val cities = Vector("Bangkok", "Ashburn")
  val expectedRecords = new HashMap[String, ArrayBuffer[HotelRecord]]
  cities.foreach(x => (expectedRecords.put(x, new ArrayBuffer())))


  override def beforeAll(): Unit = {
    val in = this.getClass.getResourceAsStream("/hoteldb.csv")
    val lines = scala.io.Source.fromInputStream(in).getLines

    // Skipping the first line
    lines.next

    for (line <- lines) {
      val cols = line.split(",").map(_.trim)
      val city = cols(0)
      val record = HotelRecord(cols(1).toInt, cols(2), cols(3).toInt)
      if(cities.contains(city)) {
        expectedRecords(city).append(record)
      }
    }

    server.start
  }

  override def afterAll(): Unit = {
    client.shutdownNow
    server.stop
  }

  test("Single Valid Request for GOHO Service") {

    val target = uri("http://localhost:8080/getHotelsByCity") / "Bangkok"
    val req = Request(uri = target, headers = Headers(Authorization(OAuth2BearerToken(validKey))))
    val resp = client.toHttpService(req).run

    resp.status should equal (Ok)
  }

  test("Single Invalid Request for GOHO Service") {

    val target = uri("http://localhost:8080/getHotelsByCity") / "Bangkok"
    // Invalid key in the header
    val req = Request(uri = target, headers = Headers(Authorization(OAuth2BearerToken(invalidKey))))
    val resp = client.toHttpService(req).run

    resp.status should equal (Unauthorized)
  }

  test("Single Invalid Request with no Key for GOHO Service") {

    val target = uri("http://localhost:8080/getHotelsByCity") / "Bangkok"
    // No key in the header
    val req = Request(uri = target)
    val resp = client.toHttpService(req).run

    resp.status should equal (Forbidden)
  }

  test("Simple Get for GOHO Service") {

    def getRecords(city: String): Task[String] = {
      val target = uri("http://localhost:8080/getHotelsByCity") / city
      val req = Request(uri = target, headers = Headers(Authorization(OAuth2BearerToken(validKey))))
      client.fetchAs[String](req)
    }

    val tasks = cities.map(getRecords)
    val recordsList: Task[List[String]] =
      Nondeterminism[Task].nmap2(tasks(0), tasks(1))((resp1: String, resp2: String) => {
      List(resp1, resp2)
    })

    val output = recordsList.run

    // Log the results for info
    logger.info(output.mkString("\n"))

    val expOutput = cities.map(x => json.toJson(CitySearchResponse(expectedRecords(x).toArray)))

    output should contain theSameElementsAs(expOutput)
  }

  test("Get sa in cityname returns records sorted on room price in GOHO Service") {

    def getRecords(city: String): Task[String] = {
      val target = uri("http://localhost:8080/getHotelsByCity") / s"${city}=sa"
      val req = Request(uri = target, headers = Headers(Authorization(OAuth2BearerToken(validKey))))
      client.fetchAs[String](req)
    }

    val tasks = cities.map(getRecords)
    val recordsList: Task[List[String]] =
      Nondeterminism[Task].nmap2(tasks(0), tasks(1))((resp1: String, resp2: String) => {
      List(resp1, resp2)
    })

    val output = recordsList.run

    // Log the results for info
    logger.info(output.mkString("\n"))

    val expOutput = cities.map(x =>
      json.toJson(CitySearchResponse(expectedRecords(x).sortBy(p => p.price).toArray)))

    output should contain theSameElementsAs(expOutput)
  }

  test("Get sd in cityname returns records sorted on room price in descending order in GOHO Service") {

    def getRecords(city: String): Task[String] = {
      val target = uri("http://localhost:8080/getHotelsByCity") / s"${city}=sd"
      val req = Request(uri = target, headers = Headers(Authorization(OAuth2BearerToken(validKey))))
      client.fetchAs[String](req)
    }

    val tasks = cities.map(getRecords)
    val recordsList: Task[List[String]] =
      Nondeterminism[Task].nmap2(tasks(0), tasks(1))((resp1: String, resp2: String) => {
      List(resp1, resp2)
    })

    val output = recordsList.run

    // Log the results for info
    logger.info(output.mkString("\n"))

    val expOutput = cities.map(x =>
      json.toJson(CitySearchResponse(expectedRecords(x).sortBy(p => p.price)(Ordering[Int].reverse).toArray)))

    output should contain theSameElementsAs(expOutput)
  }

  test("Illegal Ordering request throws Bad Request") {
    val target = uri("http://localhost:8080/getHotelsByCity") / "Bangkok=se"
    // Invalid key in the header
    val req = Request(uri = target, headers = Headers(Authorization(OAuth2BearerToken(validKey))))
    val resp = client.toHttpService(req).run

    resp.status should equal (BadRequest)
  }

}
