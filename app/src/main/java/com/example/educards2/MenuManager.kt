package com.example.educards2

import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.android.material.appbar.MaterialToolbar

class MenuManager(
    private val context: AppCompatActivity,
    private val drawerLayout: DrawerLayout,
    private val navigationView: NavigationView,
    private val toolbar: MaterialToolbar
) {
    fun setupNavigation(onItemSelected: (Int) -> Unit) {
        context.setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            context,
            drawerLayout,
            toolbar,
            R.string.open_nav,
            R.string.close_nav
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener { menuItem ->
            onItemSelected(menuItem.itemId)
            true
        }
    }
}