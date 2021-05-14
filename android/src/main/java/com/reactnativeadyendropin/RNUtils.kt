package com.reactnativeadyendropin

import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.WritableNativeArray
import com.facebook.react.bridge.WritableNativeMap
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
  }
}
