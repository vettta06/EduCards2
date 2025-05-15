package com.example.educards2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.educards2.databinding.ActivityLecturesBinding
import com.example.educards2.lectures.LectureAdapter
import com.example.educards2.lectures.LectureFile
import com.example.educards2.lectures.SubjectFolder

class LecturesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLecturesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLecturesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        setupRecyclerView()
        setupColors()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        val folders = listOf(
            SubjectFolder(
                name = "Линейная алгебра",
                lectureFiles = listOf(
                    LectureFile(
                        title = "Лекция 2",
                        url = "https://online-edu.mirea.ru/pluginfile.php/1655141/mod_folder/content/0/%D0%9B%D0%B8%D0%BD%D0%B5%D0%B9%D0%BD%D0%B0%D1%8F%20%D0%B0%D0%BB%D0%B3%D0%B5%D0%B1%D1%80%D0%B0%20%D0%B8%20%D0%B0%D0%BD%D0%B0%D0%BB%D0%B8%D1%82%D0%B8%D1%87%D0%B5%D1%81%D0%BA%D0%B0%D1%8F%20%D0%B3%D0%B5%D0%BE%D0%BC%D0%B5%D1%82%D1%80%D0%B8%D1%8F%202_%D0%9B%D0%B5%D0%BA%D1%86%D0%B8%D1%8F%202.pdf?forcedownload=1"
                    ),
                    LectureFile(
                        title = "Лекция 5",
                        url = "https://online-edu.mirea.ru/pluginfile.php/1655141/mod_folder/content/0/%D0%9B%D0%B8%D0%BD%D0%B5%D0%B9%D0%BD%D0%B0%D1%8F%20%D0%B0%D0%BB%D0%B3%D0%B5%D0%B1%D1%80%D0%B0%20%D0%B8%20%D0%B0%D0%BD%D0%B0%D0%BB%D0%B8%D1%82%D0%B8%D1%87%D0%B5%D1%81%D0%BA%D0%B0%D1%8F%20%D0%B3%D0%B5%D0%BE%D0%BC%D0%B5%D1%82%D1%80%D0%B8%D1%8F%202_%D0%9B%D0%B5%D0%BA%D1%86%D0%B8%D1%8F%205.pdf?forcedownload=1"
                    )
                )
            )
        )

        binding.lecturesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@LecturesActivity)
            adapter = LectureAdapter(folders)
        }
    }
    private fun setupColors() {
        window.decorView.setBackgroundColor(ContextCompat.getColor(this, R.color.background_activity))
    }
}