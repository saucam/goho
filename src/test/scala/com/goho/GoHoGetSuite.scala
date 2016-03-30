package com.goho

import com.goho.service.GoHoService
import com.goho.service.db.{RoomType, HotelRecord}
import com.typesafe.scalalogging.slf4j.LazyLogging
import org.http4s.HttpService
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

  test("Simple Get for GOHO Service") {

    def getRecords(city: String): Task[String] = {
      val target = uri("http://localhost:8080/getHotelsByCity/") / city / "RandomKey"
      client.getAs[String](target)
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
