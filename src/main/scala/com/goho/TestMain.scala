package com.goho

import com.goho.service.GoHoService
import org.http4s._, org.http4s.dsl._
import org.http4s.server.blaze._
/**
 * Created by yash.datta on 30/03/16.
 */
object TestMain {

  def main(args: Array[String]): Unit = {

    val service = new GoHoService
    val server = new HServer(service.gohoService)
    // Start serving
    server.start
    // Wait
    server.awaitShutdown
  }

}
