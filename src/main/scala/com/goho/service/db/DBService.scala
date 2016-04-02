package com.goho.service.db

/**
 * Created by yash.datta on 30/03/16.
 */
trait DBService {

  def getRecords(key: String): Response[_]
  def getOrderedRecords(key: String, ordering: Int): Response[_]

}
