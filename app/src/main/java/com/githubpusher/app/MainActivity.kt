package com.githubpusher.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

/** Home screen: list saved repository configurations. */
class MainActivity : AppCompatActivity() {

    private lateinit var store: ConfigStore
    private lateinit var adapter: ConfigListAdapter
    private lateinit var emptyView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        store = ConfigStore(this)
        emptyView = findViewById(R.id.emptyView)
        adapter = ConfigListAdapter(emptyList()) { config ->
            startActivity(
                Intent(this, EditConfigActivity::class.java)
                    .putExtra(EditConfigActivity.EXTRA_ID, config.id)
            )
        }
        val list = findViewById<RecyclerView>(R.id.configList)
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter
        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            startActivity(Intent(this, EditConfigActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        val configs = store.list()
        adapter.submit(configs)
        emptyView.visibility = if (configs.isEmpty()) View.VISIBLE else View.GONE
    }
}
