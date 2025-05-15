package com.example.educards2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.educards2.R
import com.example.educards2.Stats.StreakCheckWorker
import com.example.educards2.Stats.StreakManager
import com.example.educards2.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var streakManager: StreakManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        drawerLayout = binding.drawerLayout
        navigationView = binding.navView

        streakManager = StreakManager(applicationContext)
        setupUI()
        updateStreakViews()
        setupWorkManager()
    }
    private fun setupWorkManager() {
        val dailyRequest: PeriodicWorkRequest = PeriodicWorkRequestBuilder<StreakCheckWorker>(
            1,
            TimeUnit.DAYS
        )
            .setInitialDelay(1, TimeUnit.DAYS)
            .build()

        WorkManager.getInstance(this).enqueue(dailyRequest)
    }

    private fun setupUI() {
        setupToolbar()
        setupNavigationDrawer()
        setupClickListeners()
        setupColors()
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

    private fun setupClickListeners() {
        binding.btnContinue.setOnClickListener {
            streakManager.updateStreak()
            openActivity(UserCardsActivity::class.java)
        }
    }

    private fun updateStreakViews() {
        val prefs = streakManager.getSharedPreferences()
        binding.tvDays.text = "Дней подряд: ${prefs.getInt("current_streak", 0)}"
        binding.tvRecord.text = "Рекорд: ${prefs.getInt("record_streak", 0)}"
    }

    private fun openActivity(activityClass: Class<*>) {
        startActivity(Intent(this, activityClass))
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}