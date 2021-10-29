package com.reactnativeadyendropin

import com.facebook.react.bridge.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class RNUtils {
  companion object {
    fun jsonToWritableMap(jsonObject: JSONObject?): WritableMap? {
      val writableMap: WritableMap = WritableNativeMap()
      if (jsonObject == null) {
        return null
      }
      val iterator = jsonObject.keys()
      if (!iterator.hasNext()) {
        return null
      }
      while (iterator.hasNext()) {
        val key = iterator.next()
        try {
          val value = jsonObject[key]
          if (value is Boolean) {
            writableMap.putBoolean(key, value)
          } else if (value is Int) {
            writableMap.putInt(key, value)
          } else if (value is Double) {
            writableMap.putDouble(key, value)
          } else if (value is String) {
            writableMap.putString(key, value)
          } else if (value is JSONObject) {
            writableMap.putMap(key, jsonToWritableMap(value))
          } else if (value is JSONArray) {
            writableMap.putArray(key, jsonArrayToWritableArray(value))
          }
        } catch (err: JSONException) {
          throw err
        }
      }
      return writableMap
    }

    fun jsonArrayToWritableArray(jsonArray: JSONArray?): WritableArray? {
      val writableArray: WritableArray = WritableNativeArray()
      if (jsonArray == null) {
        return null
      }
      if (jsonArray.length() <= 0) {
        return null
      }
      for (i in 0 until jsonArray.length()) {
        try {
          val value = jsonArray[i]
          if (value == null) {
            writableArray.pushNull()
          } else if (value is Boolean) {
            writableArray.pushBoolean(value)
          } else if (value is Int) {
            writableArray.pushInt(value)
          } else if (value is Double) {
            writableArray.pushDouble(value)
          } else if (value is String) {
            writableArray.pushString(value)
          } else if (value is JSONObject) {
            writableArray.pushMap(jsonToWritableMap(value))
          } else if (value is JSONArray) {
            writableArray.pushArray(jsonArrayToWritableArray(value))
          }
        } catch (err: JSONException) {
          throw err
        }
      }
      return writableArray
    }

    fun convertArrayToJson(readableArray: ReadableArray?): JSONArray {
      val array = JSONArray()
      for (i in 0 until readableArray!!.size()) {
        when (readableArray.getType(i)) {
          ReadableType.Null -> {
          }
          ReadableType.Boolean -> array.put(readableArray.getBoolean(i))
          ReadableType.Number -> array.put(readableArray.getDouble(i))
          ReadableType.String -> array.put(readableArray.getString(i))
          ReadableType.Map -> array.put(convertMapToJson(readableArray.getMap(i)))
          ReadableType.Array -> array.put(convertArrayToJson(readableArray.getArray(i)))
        }
      }
      return array
    }

    fun convertMapToJson(readableMap: ReadableMap?): JSONObject {
      val obj = JSONObject()
      val iterator = readableMap!!.keySetIterator()
      while (iterator.hasNextKey()) {
        val key = iterator.nextKey()
        when (readableMap.getType(key)) {
          ReadableType.Null -> obj.put(key, JSONObject.NULL)
          ReadableType.Boolean -> obj.put(key, readableMap.getBoolean(key))
          ReadableType.Number -> obj.put(key, readableMap.getDouble(key))
          ReadableType.String -> obj.put(key, readableMap.getString(key))
          ReadableType.Map -> obj.put(key, convertMapToJson(readableMap.getMap(key)))
          ReadableType.Array -> obj.put(key, convertArrayToJson(readableMap.getArray(key)))
        }
      }
      return obj
    }

    fun readableMapToStringMap(readableMap: ReadableMap): Map<String, String> {
      val out = mutableMapOf<String, String>()
      val iterator = readableMap.keySetIterator()
      while (iterator.hasNextKey()) {
        val key = iterator.nextKey()
        if (readableMap.getType(key) == ReadableType.String) {
          out[key] = readableMap.getString(key)!!
        }
      }

      return out
    }
  }
}
