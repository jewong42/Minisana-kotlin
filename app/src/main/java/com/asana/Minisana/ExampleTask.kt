package com.asana.Minisana

import android.graphics.Bitmap

data class ExampleTask(
        val gid: String,
        val name: String,
        val resourceType: String,
        val image: Bitmap?
)