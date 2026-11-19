package com.githubpusher.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/** Simple list adapter for RepoConfig rows. */
class ConfigListAdapter(
    private var items: List<RepoConfig>,
    private val onClick: (RepoConfig) -> Unit
) : RecyclerView.Adapter<ConfigListAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.nameText)
        val repo: TextView = view.findViewById(R.id.repoText)
        val meta: TextView = view.findViewById(R.id.metaText)
    }

    fun submit(list: List<RepoConfig>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_config, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val c = items[position]
        holder.name.text = c.name
        holder.repo.text = c.repoUrl
        val prefix = c.pathPrefix.ifBlank { "(repo root)" }
        holder.meta.text = "branch: ${c.branch}  ·  path: $prefix"
        holder.itemView.setOnClickListener { onClick(c) }
    }

    override fun getItemCount(): Int = items.size
}
