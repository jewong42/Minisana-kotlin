package com.asana.Minisana

import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asana.Minisana.NetworkingHelpers.enqueueHTTPRequest
import com.asana.Minisana.NetworkingHelpers.enqueueSearchQuery
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class TaskListFragment: Fragment() {

    var mCache: MutableLiveData<MutableList<ExampleTask>> = MutableLiveData()
    var mNextPage: NextPage? = null
    var mAdapter = TaskRecyclerViewAdapter()
    var mSearchCache = HashMap<String, MutableList<ExampleTask>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Make the request for the first page.
        // The response is parsed but not stored anywhere.
//        fetchList()
//        Here is some example code for loading an image for a given task.
//        We advise you not to use an image loading library because fetching images for task
//        attachments is more complicated than simply building a URL, and this helper takes
//        care of all the details.
    }

    private fun fetchList(uri: String) {
        enqueueHTTPRequest(
                uri,
                object : NetworkingHelpers.SimpleCallback() {
                    @Throws(IOException::class, JSONException::class)
                    override fun onSuccessfulResponse(response: Response) {
                        // store body, as response.body().string() can only be called once
                        val body = response.body()!!.string()
                        Log.i("NetworkingHelpers", JSONObject(body).toString(2))
                        val json = JSONObject(body)
                        val jsonTaskList = json.getJSONArray("data")
                        var cache = mutableListOf<ExampleTask>()
                        for (i in 0 until jsonTaskList.length()) {
                            val jsonTask = jsonTaskList.getJSONObject(i)
                            val gid = jsonTask.getString("gid")
                            val name = jsonTask.getString("name")
                            val resourceType = jsonTask.getString("resource_type")
                            NetworkingHelpers.enqueueTaskImageRequest(gid, object : NetworkingHelpers.ImageDownloadCallback() {
                                override fun onDownloadedImage(bitmap: Bitmap?) {
                                    Log.d("Jerry", "onDownloadedImage")
                                    mCache.value?.add(ExampleTask(gid, name, resourceType, bitmap))
                                    mCache.postValue(mCache.value)
                                    Log.d("Jerry", mCache.value?.toTypedArray()?.contentToString())
                                }
                            })
                        }
                        val jsonNextPage = json.getJSONObject("next_page")
                        if (jsonNextPage != null) {
                            val offset = jsonNextPage.getString("offset")
                            val path = jsonNextPage.getString("path")
                            val uri = jsonNextPage.getString("uri")
                            mNextPage = NextPage(offset, path, uri)
                        }
                        mCache.postValue(cache)
                    }
                })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_task_list, container, false)
        val searchBox: EditText = view.findViewById(R.id.searchbox)
        searchBox.let {
            it.visibility = View.VISIBLE
            it.setOnEditorActionListener { v: TextView?, keyCode: Int, event: KeyEvent? ->
                applySearchQuery(it.text.toString())
                true
            }
            it.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun afterTextChanged(s: Editable) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    if (s.isNullOrBlank()) {
                        mAdapter.clearData()
                        fetchList(NetworkingHelpers.TASKS_FOR_PROJECT_URL)
                    }
                    else applySearchQuery(it.text.toString())
                }
            })
        }
        val recyclerView: RecyclerView = view.findViewById(R.id.list)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = mAdapter
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!recyclerView.canScrollVertically(1)) mNextPage?.let { fetchList(it.uri) }
            }
        })

        mCache.observe(this, Observer {
            Log.i("Jerry", "mCache updated")
            if (searchBox.text.isNullOrBlank()) mAdapter.addData(it)
            else mAdapter.replaceData(it)
        })
        fetchList(NetworkingHelpers.TASKS_FOR_PROJECT_URL)
        return view
    }

    /**
     * Called when user changes the query (every keypress)
     * @param query
     */
    fun applySearchQuery(query: String) {
        // TODO: this
        if (mSearchCache.containsKey(query)) return mCache.postValue(mSearchCache[query])
        else enqueueSearchQuery(query,
            object : NetworkingHelpers.SimpleCallback() {
                override fun onSuccessfulResponse(response: Response) {
                    val body = response.body()!!.string()
                    Log.i("NetworkingHelpers", JSONObject(body).toString(2))
                    val json = JSONObject(body)
                    val jsonTaskList = json.getJSONArray("data")
                    var cache = mutableListOf<ExampleTask>()
                    for (i in 0 until jsonTaskList.length()) {
                        val jsonTask = jsonTaskList.getJSONObject(i)
                        val gid = jsonTask.getString("gid")
                        val name = jsonTask.getString("name")
                        val resourceType = jsonTask.getString("resource_type")
                        NetworkingHelpers.enqueueTaskImageRequest(gid, object : NetworkingHelpers.ImageDownloadCallback() {
                            override fun onDownloadedImage(bitmap: Bitmap?) {
                                cache.add(ExampleTask(gid, name, resourceType, bitmap))
                            }
                        })
                    }
                    mSearchCache.put(query, cache)
                    mCache.postValue(cache)
                }
            })
        Log.i("SearchQuery", query)
    }

    companion object {
        fun newInstance(): TaskListFragment {
            val fragment = TaskListFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}