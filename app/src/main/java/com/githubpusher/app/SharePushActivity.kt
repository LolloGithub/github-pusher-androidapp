package com.githubpusher.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

/** Share/open ZIP → choose config → push to GitHub. */
class SharePushActivity : AppCompatActivity() {

    private lateinit var store: ConfigStore
    private var zipUri: android.net.Uri? = null
    private var selected: RepoConfig? = null
    private lateinit var statusText: TextView
    private lateinit var progress: ProgressBar
    private lateinit var btnPush: MaterialButton
    private lateinit var zipInfo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_push)
        store = ConfigStore(this)
        statusText = findViewById(R.id.statusText)
        progress = findViewById(R.id.progress)
        btnPush = findViewById(R.id.btnPush)
        zipInfo = findViewById(R.id.zipInfo)
        zipUri = ShareIntents.extractZipUri(intent)
        if (zipUri == null) {
            Toast.makeText(this, R.string.no_zip, Toast.LENGTH_LONG).show()
            finish()
            return
        }
        zipInfo.text = "ZIP: $zipUri"
        val configs = store.list()
        if (configs.isEmpty()) {
            Toast.makeText(this, R.string.no_configs_for_share, Toast.LENGTH_LONG).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        val adapter = ConfigListAdapter(configs) { config ->
            selected = config
            btnPush.isEnabled = true
            statusText.text = "Selected: ${config.name}"
        }
        findViewById<RecyclerView>(R.id.configList).apply {
            layoutManager = LinearLayoutManager(this@SharePushActivity)
            this.adapter = adapter
        }
        if (configs.size == 1) {
            selected = configs[0]
            btnPush.isEnabled = true
            statusText.text = "Selected: ${configs[0].name}"
        }
        btnPush.setOnClickListener { runPush() }
    }

    private fun runPush() {
        val uri = zipUri ?: return
        val config = selected ?: return
        val parsed = RepoUrlParser.parse(config.repoUrl) ?: run {
            statusText.text = "Invalid repository URL in configuration"
            return
        }
        btnPush.isEnabled = false
        progress.visibility = View.VISIBLE
        statusText.text = getString(R.string.unzipping)
        lifecycleScope.launch {
            try {
                val n = PushPipeline.pushZip(this@SharePushActivity, uri, config, parsed) {
                    statusText.text = it
                }
                zipInfo.text = getString(R.string.files_in_zip, n)
                statusText.text = getString(R.string.push_success)
                Toast.makeText(this@SharePushActivity, R.string.push_success, Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                val msg = e.message ?: e.toString()
                statusText.text = getString(R.string.push_failed, msg)
                Toast.makeText(this@SharePushActivity, msg, Toast.LENGTH_LONG).show()
                btnPush.isEnabled = true
            } finally {
                progress.visibility = View.GONE
            }
        }
    }
}
