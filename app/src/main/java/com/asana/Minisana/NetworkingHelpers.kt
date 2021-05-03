package com.asana.Minisana

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

/**
 * ATTENTION
 *
 * We don't expect you to need to modify this file, but feel free to.
 *
 * This file takes care of authenticating with the Asana API and dealing with image loading.
 *
 * Patterns used in this file are not necessarily "good" patterns, and code in here will not help
 * you understand the rest of the app.
 *
 * Static methods you can use that are implemented in this file:
 *
 * - NetworkingHelpers.enqueueHTTPRequest(url, callback)
 * - NetworkingHelpers.enqueueTaskImageRequest(taskID, callback)
 * - NetworkingHelpers.enqueueSearchQuery(query, callback)
 *
 * Treat them as black boxes and do not worry about their implementations.
 *
 */
object NetworkingHelpers {
    private const val WORKSPACE_ID = "892071173450719"
    private const val WORDS_PROJECT_ID = "892073084005160"
    private const val AUTHORIZATION_VALUE = "Bearer 0/df2870483ef5ffce57418857e8f5f6ea"

    const val TASKS_FOR_PROJECT_URL = "https://app.asana.com/api/1.0/projects/" + WORDS_PROJECT_ID + "/tasks?limit=20"
    private const val SEARCH_FOR_TASKS_URL = "https://app.asana.com/api/1.0/workspaces/" + WORKSPACE_ID + "/typeahead?type=task&query="

    private val client = OkHttpClient()

    /**
     * Make an HTTP request to the Asana API. The response is an okhttp3.Response.
     *
     * @param url
     * @param callback
     */
    fun enqueueHTTPRequest(url: String, callback: SimpleCallback) {
        val request = Request.Builder()
                .url(url)
                .header("Authorization", AUTHORIZATION_VALUE)
                .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                Log.i("NetworkingHelpers", "Got response for $url")
                try {
                    callback.onSuccessfulResponse(response)
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        })
    }

    /**
     * Constructs a url for a given query, and sends the request with
     * @param query
     * @param callback
     */
    fun enqueueSearchQuery(query: String, callback: SimpleCallback) {
        val url = SEARCH_FOR_TASKS_URL + query
        enqueueHTTPRequest(url, callback)
    }

    /**
     * Start an asynchronous request to fetch the image data for the first attachment of the given
     * task.
     *
     * The implementation of this method is quick and dirty. It does not necessarily represent
     * what we consider best practices.
     *
     * @param taskID
     * @param callback
     */
    fun enqueueTaskImageRequest(taskID: String, callback: ImageDownloadCallback) {
        // Note to candidates: you don't need to do your parsing this way; we just did it because
        // it has no dependencies
        // 1. get first attachment
        // 2. get attachment details (fetchAttachmentAndGetImage)
        // 3. download image (fetchImage)
        enqueueHTTPRequest(
                "https://app.asana.com/api/1.0/tasks/$taskID/attachments",
                object : SimpleCallback() {
                    @Throws(IOException::class, JSONException::class)
                    override fun onSuccessfulResponse(response: Response) {
                        val json = JSONObject(response.body()!!.string())
                        val jsonAttachmentList = json.getJSONArray("data")
                        if (jsonAttachmentList.length() < 1) {
                            return  // no attachments
                        }
                        val jsonAttachment = jsonAttachmentList[0] as JSONObject
                        val attachmentID = jsonAttachment.getString("gid")
                        fetchAttachmentAndGetImage(attachmentID, callback)
                    }
                })
    }

    // Private, please ignore
    private fun fetchAttachmentAndGetImage(attachmentID: String, callback: ImageDownloadCallback) {
        enqueueHTTPRequest(
                "https://app.asana.com/api/1.0/attachments/$attachmentID",
                object : SimpleCallback() {
                    @Throws(IOException::class, JSONException::class)
                    override fun onSuccessfulResponse(response: Response) {
                        val json = JSONObject(response.body()!!.string())
                        val jsonData = json.getJSONObject("data")
                        val attachmentURL = jsonData.getString("view_url")
                        fetchImage(attachmentURL, callback)
                    }
                })
    }

    // Private, please ignore
    private fun fetchImage(attachmentURL: String, callback: ImageDownloadCallback) {
        val request = Request.Builder().url(attachmentURL).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val inputStream = response.body()!!.byteStream()
                val bitmap = BitmapFactory.decodeStream(inputStream)
                callback.onDownloadedImage(bitmap)
            }
        })
    }

    abstract class SimpleCallback {
        @Throws(IOException::class, JSONException::class)
        abstract fun onSuccessfulResponse(response: Response)
    }

    abstract class ImageDownloadCallback {
        abstract fun onDownloadedImage(bitmap: Bitmap?)
    }
}