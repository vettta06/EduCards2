package com.example.educards2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.educards2.Stats.StreakCheckWorker
import com.example.educards2.Stats.StreakManager
import com.example.educards2.database.Achievement
import com.example.educards2.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import java.util.concurrent.TimeUnit
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import com.example.educards2.database.AppDatabase
import com.example.educards2.database.CardDao
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var streakManager: StreakManager
    private lateinit var achievementManager: AchievementManager
    lateinit var cardDao: CardDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        drawerLayout = binding.drawerLayout
        navigationView = binding.navView
        streakManager = StreakManager(applicationContext)
        cardDao = AppDatabase.getDatabase(this).cardDao()
        achievementManager = AchievementManager(
            this,
            cardDao,
            streakManager
        )
        achievementManager.achievementsUpdated.observe(this) { updated ->
            if (updated == true) {
                updateAchievementsUI()
                achievementManager.resetAchievementsUpdated()
            }
        }

        setupUI()
        updateStreakViews()
        setupWorkManager()
        setupAchievements()
    }

    private fun setupUI() {
        setupToolbar()
        setupNavigationDrawer()
        setupClickListeners()
        setupColors()
        lifecycleScope.launch {
            achievementManager.checkAllAchievements()
            updateAchievementsUI()
        }
    }

    private fun setupClickListeners() {
        binding.btnContinue.setOnClickListener {
            streakManager.updateStreak()
            lifecycleScope.launch {
                achievementManager.checkAllAchievements()
                updateAchievementsUI()
            }
            openActivity(UserCardsActivity::class.java)
        }
    }

    /*private fun setupWorkManager() {
        val dailyRequest = PeriodicWorkRequestBuilder<StreakCheckWorker>(
            1,
            TimeUnit.DAYS,
            15,
            TimeUnit.MINUTES
        )
            .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_streak_check",
            ExistingPeriodicWorkPolicy.UPDATE,
            dailyRequest
        )
    }*/
    private fun setupWorkManager() {
        val testMode = true

        val interval = if (testMode) 15L else 1L
        val unit = if (testMode) TimeUnit.MINUTES else TimeUnit.DAYS
        Log.d("StreakDebug", "Настройка WorkManager с интервалом: $interval ${unit.name}")

        val dailyRequest = PeriodicWorkRequestBuilder<StreakCheckWorker>(
            interval,
            unit,
            15,
            TimeUnit.MINUTES
        )
            .setInitialDelay(1, TimeUnit.MINUTES) // Задержка 1 минута для теста
            .build()

        WorkManager.getInstance(this).apply {
            // Отменяем предыдущую задачу
            cancelUniqueWork("daily_streak_check")
            enqueueUniquePeriodicWork(
                "daily_streak_check",
                ExistingPeriodicWorkPolicy.UPDATE,
                dailyRequest
            )
            Log.d("StreakDebug", "Новая задача поставлена в очередь. Время запуска: ${Date()}")

        }
    }

    private fun calculateInitialDelay(): Long {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        return calendar.timeInMillis - System.currentTimeMillis()
    }
    private fun setupAchievements() {
        updateAchievementsUI()
    }

    private fun updateAchievementsUI() {
        val gridLayout = binding.achievementsGrid
        gridLayout.removeAllViews()
        achievementManager.loadAchievementStatuses()

        achievementManager.allAchievements.forEach { achievement ->

            val achievementView = LayoutInflater.from(this)
                .inflate(R.layout.item_achievement, gridLayout, false)

            val icon = achievementView.findViewById<ImageView>(R.id.achievement_icon)
            val title = achievementView.findViewById<TextView>(R.id.achievement_title)

            title.text = achievement.title

            if (achievement.unlocked) {
                icon.setImageResource(R.drawable.ic_achievement_unlocked)
                achievementView.setOnClickListener {
                    showAchievementPopup(achievement)
                }
            } else {
                icon.setImageResource(R.drawable.ic_achievement_locked)
                achievementView.setOnClickListener {
                    Toast.makeText(this, "Не получено: ${achievement.description}",
                        Toast.LENGTH_SHORT).show()
                }
            }

            gridLayout.addView(achievementView)
        }
    }
    private fun showAchievementPopup(achievement: Achievement) {
        AlertDialog.Builder(this)
            .setTitle(achievement.title)
            .setMessage("Поздравляем! Вы получили ачивку: ${achievement.description}")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupNavigationDrawer() {
        val toggle = androidx.appcompat.app.ActionBarDrawerToggle(
            this,
            drawerLayout,
            binding.toolbar,
            R.string.open_nav,
            R.string.close_nav
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener { menuItem ->
            handleNavigationItemSelected(menuItem.itemId)
            true
        }
    }
    private fun setupColors() {
        window.decorView.setBackgroundColor(ContextCompat.getColor(this, R.color.background_activity))
        binding.btnContinue.setBackgroundColor(ContextCompat.getColor(this, R.color.button_continue))
        binding.toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.app_title_background))
    }

    private fun handleNavigationItemSelected(itemId: Int) {
        when (itemId) {
            R.id.nav_built_in_cards -> openActivity(BuiltInCardsActivity::class.java)
            R.id.nav_lectures -> openActivity(LecturesActivity::class.java)
            R.id.nav_user_cards -> openActivity(UserCardsActivity::class.java)
            R.id.nav_statistics -> openActivity(StatisticsActivity::class.java)
            R.id.nav_archive -> openActivity(ArchivedCardsActivity::class.java)
        }
        drawerLayout.closeDrawer(GravityCompat.START)
    }

    private fun updateStreakViews() {
        val prefs = streakManager.getSharedPreferences()
        binding.tvDays.text = "Дней подряд: ${prefs.getInt("current_streak", 0)}"
        binding.tvRecord.text = "Рекорд: ${prefs.getInt("record_streak", 0)}"
    }

    private fun openActivity(activityClass: Class<*>) {
        startActivity(Intent(this, activityClass))
    }
    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            achievementManager.checkAllAchievements()
            updateAchievementsUI()
        }
        updateStreakViews()
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("drawer_open", binding.drawerLayout.isDrawerOpen(GravityCompat.START))
    }
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        if (savedInstanceState.getBoolean("drawer_open")) {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}