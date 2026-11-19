package com.githubpusher.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

/** Home screen: list, export, and import repository configurations. */
class MainActivity : AppCompatActivity() {

    private lateinit var store: ConfigStore
    private lateinit var adapter: ConfigListAdapter
    private lateinit var emptyView: TextView

    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) writeExport(uri)
    }

    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) readImport(uri)
    }

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
        findViewById<Button>(R.id.btnExport).setOnClickListener {
            if (store.list().isEmpty()) {
                Toast.makeText(this, R.string.nothing_to_export, Toast.LENGTH_SHORT).show()
            } else {
                exportLauncher.launch("github-zip-pusher-configs.json")
            }
        }
        findViewById<Button>(R.id.btnImport).setOnClickListener {
            importLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
        }
    }

    override fun onResume() {
        super.onResume()
        reload()
    }

    private fun reload() {
        val configs = store.list()
        adapter.submit(configs)
        emptyView.visibility = if (configs.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun writeExport(uri: Uri) {
        try {
            val json = ConfigBackup.toJson(store.list())
            contentResolver.openOutputStream(uri)?.use { out ->
                out.write(json.toByteArray(Charsets.UTF_8))
            } ?: error("Cannot open output stream")
            Toast.makeText(this, R.string.export_ok, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(
                this,
                getString(R.string.export_failed, e.message ?: e.toString()),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun readImport(uri: Uri) {
        try {
            val text = contentResolver.openInputStream(uri)?.use {
                it.readBytes().toString(Charsets.UTF_8)
            } ?: error("Cannot open input stream")
            val imported = ConfigBackup.fromJson(text)
            if (imported.isEmpty()) error("No configurations in file")
            // Merge: upsert by id (keeps other local configs with different ids)
            imported.forEach { store.upsert(it) }
            reload()
            Toast.makeText(
                this,
                getString(R.string.import_ok, imported.size),
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                this,
                getString(R.string.import_failed, e.message ?: e.toString()),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
