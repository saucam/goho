package com.goho

import com.goho.conf.GoHoConf
import org.http4s._
import org.http4s.server.blaze._
import org.http4s.server.Server

/**
 * Created by yash.datta on 30/03/16.
 */
class HServer(service: HttpService, host: String = "127.0.0.1", port: Int = 8080) {

  val builder =
    BlazeBuilder.mountService(service).bindHttp(port, host).withServiceExecutor(GoHoConf.gohoExecutorService)
  var server: Option[Server] = None

  def start(): Unit = {
    server = Some(builder.run)
  }

  def stop(): Unit = {
    if (server != None) {
      server.get.shutdownNow
      server = None
    }
  }

  def awaitShutdown(): Unit = {
    if (server != None) {
      server.get.awaitShutdown()
    }
  }
}
