package com.goho.service.db

/**
 * Created by yash.datta on 30/03/16.
 */

/**
 * gson throws stack overflow when serialzing enums!!
object RoomType extends Enumeration {
  type RoomType = Value
  val Deluxe = Value("Deluxe")
  val Superior = Value("Superior")
  val SweetSuite = Value("Sweet Suite")
} */


import java.util.HashMap

import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag

case class HotelRecord(hotelId: Int, room: String, price: Int)
case class CitySearchResponse[HotelRecord](HotelRecords: Array[HotelRecord])(implicit m: ClassTag[HotelRecord])
  extends Response[HotelRecord](HotelRecords)

/**
 * This object mocks a real database service for this POC
 */
class HotelDataBase extends DBService {

  val SORT_ASC = 1
  val SORT_DESC = 2

  val dbMap: HashMap[String, ArrayBuffer[HotelRecord]] = new HashMap()

  // Reads the csv here
  def init(): Unit = {

    val in = this.getClass.getResourceAsStream("/hoteldb.csv")
    val lines = scala.io.Source.fromInputStream(in).getLines

    // Skipping the first line
    lines.next
    for (line <- lines) {
      val cols = line.split(",").map(_.trim)
      val city = cols(0)
      val record = HotelRecord(cols(1).toInt, /*RoomType.withName(cols(2))*/cols(2), cols(3).toInt)
      if(dbMap.containsKey(city)) {
        val arr = dbMap.get(city)
        arr.append(record)
      } else {
        val arr = new ArrayBuffer[HotelRecord]()
        arr.append(record)
        dbMap.put(city, arr)
      }
    }
  }

  override def getRecords(city: String): CitySearchResponse[HotelRecord] = {
    var ordering = 0
    var cityName = city
    if (city.contains("=")) {
      cityName = city.split("=").head
      // Only 2 valid values
      city.split("=").last match {
        case "sa" =>
          ordering = SORT_ASC
        case "sd" =>
          ordering = SORT_DESC
        case _ =>
          throw new DataBaseException("Illegal ordering request!")
      }
    }
    getOrderedRecords(cityName, ordering)
  }

  override def getOrderedRecords(city: String, ordering: Int = 0): CitySearchResponse[HotelRecord] = {
    val records = {
      if (!dbMap.containsKey(city)) ArrayBuffer()
      val records = dbMap.get(city)
      ordering match {
        case SORT_ASC =>
          records.sortBy(x => x.price)
        case SORT_DESC =>
          records.sortBy(x => x.price)(Ordering[Int].reverse)
        case _ =>
          records
      }
    }
    new CitySearchResponse[HotelRecord](records.toArray)
  }
}
