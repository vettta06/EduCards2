package com.example.educards2


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.educards2.database.Deck
import com.example.educards2.databinding.ItemDeckBinding

class DeckAdapter(private val onClick: (Deck) -> Unit) :
    ListAdapter<Deck, DeckAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(val binding: ItemDeckBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(deck: Deck) {
            binding.deckName.text = deck.name
            binding.deckIcon.setImageResource(R.drawable.ic_default_deck)
            itemView.setOnClickListener { onClick(deck) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDeckBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Deck>() {
        override fun areItemsTheSame(oldItem: Deck, newItem: Deck) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Deck, newItem: Deck) = oldItem == newItem
    }
}