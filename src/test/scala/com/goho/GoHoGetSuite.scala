package com.goho

import com.goho.service.GoHoService
import com.goho.service.db.{RoomType, HotelRecord}
import com.typesafe.scalalogging.slf4j.LazyLogging
import org.http4s.headers.Authorization
import org.http4s.{OAuth2BearerToken, Headers, Request, HttpService}
import org.http4s.client.blaze.PooledHttp1Client
import org.http4s.dsl._

import scala.collection.mutable.ArrayBuffer
import scalaz.concurrent.Task

/**
 * Created by yash.datta on 30/03/16.
 */
class GoHoGetSuite extends GoHoFunSuite
    with LazyLogging  {

  var server: HServer = new HServer(GoHoService.gohoService)

  val client = PooledHttp1Client()
  val expectedRecords: ArrayBuffer[HotelRecord] = new ArrayBuffer()
  val cities = Vector("Bangkok", "Ashburn")
  val validKey = "db78d85b7b27862779404c38abddd520"
  val invalidKey = "SomeRandomKey"

  override def beforeAll(): Unit = {
    val in = this.getClass.getResourceAsStream("/hoteldb.csv")
    val lines = scala.io.Source.fromInputStream(in).getLines

    // Skipping the first line
    lines.next
    for (line <- lines) {
      val cols = line.split(",").map(_.trim)
      val city = cols(0)
      val record = HotelRecord(cols(1).toInt, RoomType.withName(cols(2)), cols(3).toInt)
      if(cities.contains(city)) {
        expectedRecords.append(record)
      }
    }

    server.start
  }

  override def afterAll(): Unit = {
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
      val req = Request(uri = target, headers = Headers(Authorization(OAuth2BearerToken("db78d85b7b27862779404c38abddd520"))))
      client.fetchAs[String](req)
    }

    val recordsList = Task.gatherUnordered(cities.map(getRecords))

    val output = recordsList.run
    val processedOuput = output.map(x => x.split("\n")).flatMap(x => x)
    logger.info(output.mkString("\n\n"))

    client.shutdownNow

    val expOutput = expectedRecords.map(x => x.toString).toList

    processedOuput should contain theSameElementsAs (expOutput)

  }



}
