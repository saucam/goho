package com.goho

import com.goho.service.GoHoService
import org.http4s._, org.http4s.dsl._
import org.http4s.server.blaze._
/**
 * Created by yash.datta on 30/03/16.
 */
object TestMain {

  def main(args: Array[String]): Unit = {

    var host = "127.0.0.1"
    var port = 8080
    if (args.size == 2) {
      host = args(0)
      port = args(1).toInt
    }

    val service = new GoHoService
    val server = new HServer(service.gohoService, host, port)
    // Start serving
    server.start
    // Wait
    server.awaitShutdown
  }

}
