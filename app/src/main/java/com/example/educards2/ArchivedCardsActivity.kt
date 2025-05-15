package com.example.educards2

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.educards2.database.AppDatabase
import com.example.educards2.database.Card
import com.example.educards2.databinding.ActivityArchivedCardsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ArchivedCardsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityArchivedCardsBinding
    private lateinit var db: AppDatabase
    private lateinit var adapter: ArchivedCardsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArchivedCardsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnBack.setOnClickListener {
            finish()
        }
        db = AppDatabase.getDatabase(this)
        setupRecyclerView()
        loadArchivedCards()
    }

    private fun setupRecyclerView() {
        adapter = ArchivedCardsAdapter { card -> restoreCard(card) }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun loadArchivedCards() {
        lifecycleScope.launch {
            db.cardDao().getArchivedCards().collect { cards ->
                adapter.submitList(cards)
            }
        }
    }

    private fun restoreCard(card: Card) {
        lifecycleScope.launch(Dispatchers.IO) {
            db.cardDao().restoreCard(card.id)
            loadArchivedCards()
        }
        Toast.makeText(this, "Карточка восстановлена", Toast.LENGTH_SHORT).show()
    }
}