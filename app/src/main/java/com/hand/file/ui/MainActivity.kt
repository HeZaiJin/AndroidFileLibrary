package com.hand.file.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.hand.document.core.RootInfo
import com.hand.document.provider.Providers
import com.hand.document.util.LogUtil
import com.hand.file.R

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    companion object {
        var TAG = "MainActivity"
    }

    private lateinit var localRoots: List<RootInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navView.setNavigationItemSelectedListener(this)
        localRoots = Providers.getLocalRoots(this)
    }

    fun onClick(view: View) {
        LogUtil.d(TAG, "onClick start ")
        when (view.id) {
            R.id.image -> {
                DocumentActivity.start(this@MainActivity, Providers.ROOT_IMAGE, null)
            }
            R.id.video -> {
                DocumentActivity.start(this@MainActivity, Providers.ROOT_VIDEO, null)
            }
            R.id.auto -> {
                DocumentActivity.start(this@MainActivity, Providers.ROOT_AUDIO, null)
            }
            R.id.documents -> {
                DocumentActivity.start(this@MainActivity, Providers.ROOT_DOCUMENTS, null)
            }
            R.id.downloads -> {
                DocumentActivity.start(this@MainActivity, Providers.ROOT_DOWNLOADS, null)
            }

            R.id.internal -> {
                DocumentActivity.start(this@MainActivity, Providers.ROOT_LOCAL_STORAGE, localRoots[0])
            }

        }
        LogUtil.d(TAG, "onClick end ")
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_home -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_tools -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}