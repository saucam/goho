package com.goho.service

/**
 * Created by yash.datta on 31/03/16.
 */
import AuthorizedKeys._
import scala.collection.immutable.HashMap

trait AuthorizeKey extends AuthService {

  // Authorize
  def accept(key: String): Boolean = {
    keyMap.contains(key)
  }

  def getKeyId(key: String): Int = {
    if (keyMap.contains(key))
      keyMap.get(key).get
    else
      0
  }
}

object AuthorizedKeys {
  val keys = Set(
    "db78d85b7b27862779404c38abddd520",
    "3c1dd592ad4a4958c9efc7eb98274f0d",
    "2b35d3c48d2b8c12484ec936c88c591e",
    "c3f09720db4696567dba2860371e081b",
    "eab18f842642690664075a624630a9cc",
    "3e7e974e2cc48057664dfb93fe55b2df",
    "be6ae5b2cb245fba8736da206eeefa69",
    "fc89c8b9c926360303dfd5acfbbc6f69",
    "44041e5775439e8e0df9d914c8642518",
    "274a0881d3a21aaec70450ed879400b8",
    "6eaf74303c76f8abbba595052750084e",
    "204d7f6b3ebc7131a5f9fed8ba0be91b",
    "01e4d886c7fde06b3748944843505025",
    "c7e5b8da1173691b78a9c8c8dc86c871",
    "456f40ab05d175d3cae0c78b151e82a5",
    "9daebd2c6ba0fa9165212b388e32ddcd",
    "882426a1c29e2f5aed63b886a4a20cea",
    "b0b5bda2e1f621d3789b499d52cf5b5e",
    "04a8dc4dcf3934191a4f1a89a1575370",
    "3428a377cebf0ead570156b1dbdf97b3",
    "c757d7777b09a05ac62580b6d3e92809",
    "bbf05abee48455d7864c4d233bca1d52"
  )

  val keyMap = new HashMap[String, Int]() ++ keys.zipWithIndex.map{case(k: String,i: Int) => (k -> i)}.toList


}
