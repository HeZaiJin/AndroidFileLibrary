package com.hand.file.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.hand.document.core.DirectoryLoader
import com.hand.document.core.DirectoryResult
import com.hand.document.core.RootInfo
import com.hand.document.provider.Providers
import com.hand.document.util.LogUtil
import com.hand.file.R

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    companion object {
        var TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            startActivity(Intent(this@MainActivity, DocumentActivity::class.java))
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

        var roots: List<RootInfo> = Providers.getLocalRoots(applicationContext)
        var callback = object : LoaderManager.LoaderCallbacks<DirectoryResult> {

            override fun onCreateLoader(id: Int, args: Bundle?): Loader<DirectoryResult> {
                return DirectoryLoader(this@MainActivity, roots[0], null)
            }

            override fun onLoadFinished(loader: Loader<DirectoryResult>, data: DirectoryResult?) {
                LogUtil.d(TAG,"onLoadFinished")
            }

            override fun onLoaderReset(loader: Loader<DirectoryResult>) {
                LogUtil.d(TAG,"onLoaderReset")
            }

        }
        LoaderManager.getInstance(this@MainActivity).initLoader(2, null, object : LoaderManager.LoaderCallbacks<DirectoryResult> {

            override fun onCreateLoader(id: Int, args: Bundle?): Loader<DirectoryResult> {
                return DirectoryLoader(this@MainActivity, roots[0], null)
            }

            override fun onLoadFinished(loader: Loader<DirectoryResult>, data: DirectoryResult?) {
                LogUtil.d(TAG,"onLoadFinished")
            }

            override fun onLoaderReset(loader: Loader<DirectoryResult>) {
                LogUtil.d(TAG,"onLoaderReset")
            }

        })
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
