package com.githubpusher.app

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.UUID

/** Create or edit a single RepoConfig. */
class EditConfigActivity : AppCompatActivity() {

    private lateinit var store: ConfigStore
    private var editingId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_config)
        store = ConfigStore(this)
        editingId = intent.getStringExtra(EXTRA_ID)

        val inputName = findViewById<EditText>(R.id.inputName)
        val inputRepo = findViewById<EditText>(R.id.inputRepoUrl)
        val inputBranch = findViewById<EditText>(R.id.inputBranch)
        val inputToken = findViewById<EditText>(R.id.inputToken)
        val inputPrefix = findViewById<EditText>(R.id.inputPathPrefix)
        val inputMsg = findViewById<EditText>(R.id.inputCommitMsg)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnDelete = findViewById<Button>(R.id.btnDelete)

        inputBranch.setText("main")
        inputMsg.setText(getString(R.string.commit_msg_hint))

        editingId?.let { id ->
            store.get(id)?.let { c ->
                inputName.setText(c.name)
                inputRepo.setText(c.repoUrl)
                inputBranch.setText(c.branch)
                inputToken.setText(c.token)
                inputPrefix.setText(c.pathPrefix)
                inputMsg.setText(c.defaultCommitMessage)
                btnDelete.visibility = View.VISIBLE
            }
        }

        btnSave.setOnClickListener {
            val name = inputName.text?.toString()?.trim().orEmpty()
            val repo = inputRepo.text?.toString()?.trim().orEmpty()
            val branch = inputBranch.text?.toString()?.trim().orEmpty().ifBlank { "main" }
            val token = inputToken.text?.toString()?.trim().orEmpty()
            val prefix = inputPrefix.text?.toString()?.trim().orEmpty()?.trim('/') ?: ""
            val msg = inputMsg.text?.toString()?.trim().orEmpty()
                .ifBlank { "Update files from zip" }
            if (name.isBlank() || repo.isBlank() || token.isBlank()) {
                Toast.makeText(this, "Name, repository URL and token are required", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (RepoUrlParser.parse(repo) == null) {
                Toast.makeText(this, "Invalid repository URL", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            store.upsert(
                RepoConfig(
                    id = editingId ?: UUID.randomUUID().toString(),
                    name = name,
                    repoUrl = repo,
                    branch = branch,
                    token = token,
                    pathPrefix = prefix,
                    defaultCommitMessage = msg
                )
            )
            finish()
        }
        btnDelete.setOnClickListener {
            editingId?.let { store.delete(it) }
            finish()
        }
    }

    companion object {
        const val EXTRA_ID = "config_id"
    }
}
