package com.example.educards2.lectures

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.educards2.R

class LectureAdapter(private val folders: List<SubjectFolder>) :
    RecyclerView.Adapter<LectureAdapter.FolderViewHolder>() {

    inner class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val folderName: TextView = itemView.findViewById(R.id.folderNameTextView)
        val filesLayout: LinearLayout = itemView.findViewById(R.id.filesLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subject_folder, parent, false)
        return FolderViewHolder(view)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val folder = folders[position]
        holder.folderName.text = folder.name
        holder.filesLayout.removeAllViews()

        if (folder.isExpanded) {
            folder.lectureFiles.forEach { file ->
                val fileView = LayoutInflater.from(holder.itemView.context)
                    .inflate(R.layout.item_lecture_file, holder.filesLayout, false)
                fileView.findViewById<TextView>(R.id.lectureTitle).text = file.title
                fileView.setOnClickListener {
                    openUrlInBrowser(holder.itemView.context, file.url)
                }
                holder.filesLayout.addView(fileView)
            }
        }
        holder.filesLayout.visibility = if (folder.isExpanded) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            folder.isExpanded = !folder.isExpanded
            notifyItemChanged(position)
        }
    }

    private fun openUrlInBrowser(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка открытия ссылки", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount() = folders.size
}