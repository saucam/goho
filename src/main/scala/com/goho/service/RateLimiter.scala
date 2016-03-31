package com.goho.service

import java.util.HashMap
import java.util.concurrent.ConcurrentHashMap

import com.goho.service.db.HotelRecord
import com.typesafe.config.ConfigFactory

import scala.collection.mutable.ArrayBuffer

/**
 * Created by yash.datta on 31/03/16.
 */
import AuthorizedKeys._
import RateLimiter._

/**
 *
 * @param refreshInterval time in seconds in which rates are refreshed
 */
class RateLimiter(refreshInterval: Int = 10, enableInterval: Long = 300) {
  // Maintain Current Limit
  val arr = new Array[Int](rateMap.size)
  val enableArray = new Array[Boolean](arr.size)
  // Lock object
  val lock = new Object()
  // Initialize the arrays
  init()

  def init(): Unit = {
    var i = 0
    while (i < rateMap.size) {
      arr(i) = 0
      enableArray(i) = true
      i += 1
    }
  }

  @volatile
  private var doRefresh = true
  @volatile
  private var doEnable = true
  val refreshSleepTime: Long = refreshInterval*1000
  val enableSleepTime: Long = enableInterval*1000

  // Spawn a thread that keeps refreshing every 10 seconds
  val refreshThread = new Thread ( new Runnable {
    override def run(): Unit = {
      while (doRefresh) {
        Thread.sleep(refreshSleepTime)
        // Update the rates!
        lock.synchronized {
          var i = 0
          while (i < arr.size) {
            // Refreshes only the enabled keys
            if (enableArray(i)) {
              arr(i) = 0
            }
            i += 1
          }
        }
      }
    }
  }).start

  val enableThread = new Thread ( new Runnable {
    override def run(): Unit = {
      while(doEnable) {
        Thread.sleep(enableSleepTime)
        // Enable the disabled apis
        lock.synchronized {
          var i = 0
          while (i < enableArray.size) {
            if (!enableArray(i)) {
              enableArray(i) = true
              // also refreshes just in case
              arr(i) = 0
            }
            i += 1
          }
        }
      }
    }
  }).start

  /**
   * Only called for keys that are present in keyMap
   * @param key
   * @return
   */
  def accept(key: String): Boolean = {
    val id = keyMap(key)
    if (enableArray(id)) {
      lock.synchronized {
        if (enableArray(id)) {
          if (arr(id) == rateMap.get(id)) {
            // This User has reached limit
            enableArray(id) = false
            return false
          } else {
            arr(id) += 1
            return true
          }
        } else {
          return false
        }
      }
    } else {
      return false
    }
  }

  def destroy(): Unit = {
    doRefresh = false
    doEnable = false
  }

}

object RateLimiter {

  val SECOND = 1000L
  val MINUTE = SECOND*60

  val globalRateLimit = "rate.global.limit"
  // Read from config file
  val conf = ConfigFactory.load("ratelimit")

  // Read only map for refreshing!
  val rateMap: HashMap[Int, Int] = new HashMap()

  keys.foreach({ x =>
    if (conf.hasPath(x)) {
      rateMap.put(keyMap(x), conf.getInt(x))
    } else {
      rateMap.put(keyMap(x), conf.getInt(globalRateLimit))
    }
  })

}
