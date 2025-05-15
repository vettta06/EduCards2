package com.example.educards2

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.educards2.database.Card
import com.example.educards2.databinding.ItemArchivedCardBinding

class ArchivedCardsAdapter(
    private val onRestoreClick: (Card) -> Unit
) : ListAdapter<Card, ArchivedCardsAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemArchivedCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(card: Card) {
            binding.apply {
                this.card = card
                executePendingBindings()
                btnRestore.setOnClickListener { onRestoreClick(card) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemArchivedCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Card>() {
        override fun areItemsTheSame(oldItem: Card, newItem: Card) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Card, newItem: Card) =
            oldItem == newItem
    }
}