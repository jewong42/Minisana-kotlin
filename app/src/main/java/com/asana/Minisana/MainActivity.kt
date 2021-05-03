package com.asana.Minisana

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * A simple wrapper for TaskListFragment
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val fm = supportFragmentManager
        var fragment = fm.findFragmentById(R.id.fragment_container) as TaskListFragment?
        if (fragment == null) {
            fragment = TaskListFragment.newInstance()
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit()
        }
    }
}