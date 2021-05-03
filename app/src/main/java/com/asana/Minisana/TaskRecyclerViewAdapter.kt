package com.asana.Minisana

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter for displaying a real list of tasks in a RecyclerView
 */
class TaskRecyclerViewAdapter(
        private var dataSet: MutableList<ExampleTask> = mutableListOf<ExampleTask>()
) : RecyclerView.Adapter<TaskRecyclerViewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_task, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var task = dataSet[position]
        holder.textView.text = task.name
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    fun addData(data: List<ExampleTask>) {
        dataSet.addAll(data)
        notifyDataSetChanged()
    }

    fun replaceData(data: List<ExampleTask>) {
        clearData()
        dataSet.addAll(data)
        notifyDataSetChanged()
    }

    fun clearData() {
        dataSet.clear()
        notifyDataSetChanged()
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val textView = view.findViewById<View>(R.id.content) as TextView
        val imageView = view.findViewById<View>(R.id.image) as ImageView
    }
}