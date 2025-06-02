package ru.neko.online.client.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.neko.online.client.R
import ru.neko.online.client.components.AccountPrefs
import ru.neko.online.client.components.network.NetworkManager
import ru.neko.online.client.config.Prefs
import ru.neko.online.client.fragment.game.CatControlsFragment
import ru.neko.online.client.fragment.game.HomeFragment
import ru.neko.online.client.fragment.game.UserFragment

class MainActivity : AppCompatActivity() {

    private var viewPager: ViewPager2? = null
    private var pagerAdapter: FragmentStateAdapter? = null

    private var linearLayout: LinearLayoutCompat? = null
    private var syncIndicator: LinearLayoutCompat? = null

    private var materialToolbar: MaterialToolbar? = null

    private var bottomNavigation: BottomNavigationView? = null

    private var prefs: Prefs? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!configurePrefs()) {
            finish()
            return
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.main_activity)
        viewPager = findViewById<ViewPager2>(R.id.viewpager)
        pagerAdapter = NekoMainAdapter(this)
        bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        linearLayout = findViewById<LinearLayoutCompat>(R.id.main_linear_layout)
        syncIndicator = findViewById<LinearLayoutCompat>(R.id.sync_indicator)
        materialToolbar = findViewById<MaterialToolbar>(R.id.toolbar)

        configureUi()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        lifecycleScope.launch {
            if (!checkUser(this@MainActivity)) {
                Log.d("Main", "Ошибка входа")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Ошибка входа", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun configurePrefs(): Boolean {
        prefs = Prefs(this)
        prefs?.let {
            if (it.isFirstLaunch) {
                val intent = Intent(
                    this,
                    WelcomeActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )
                startActivity(intent)
                return false
            }
        }
        return true
    }

    private fun animateSyncIndicator(hideIndicator: Boolean) {
        if (syncIndicator == null) {
            return
        }
        if (!hideIndicator) syncIndicator!!.visibility = View.VISIBLE
        syncIndicator!!.animate().translationY(if (hideIndicator) -200f else 0f)
            .alpha(if (hideIndicator) 0f else 1f).setDuration(200).withEndAction {
            if (hideIndicator) syncIndicator!!.visibility = View.GONE
        }
    }

    private suspend fun checkUser(context: Context): Boolean {
        var accountPrefs: AccountPrefs? = AccountPrefs(context)

        val username = accountPrefs?.accountUsername
        val password = accountPrefs?.accountPassword

        if (username == null || password == null) {
            return false
        }
        withContext(Dispatchers.Main) {
            animateSyncIndicator(false)
        }
        val network = NetworkManager(context)
        val result = network.login(username, password)

        val jsonObj = result.first

        withContext(Dispatchers.Main) {
            network.closeClient()
        }
        if (jsonObj == null) {
            accountPrefs = null
            withContext(Dispatchers.Main) {
                animateSyncIndicator(true)
            }
            return false
        } else {
            val token = jsonObj.get("token").toString()
            val id = jsonObj.get("id").toString().toLong()

            val userToken = accountPrefs.userToken
            val userId = accountPrefs.userId

            withContext(Dispatchers.Main) {
                animateSyncIndicator(true)
            }

            if (userToken == null) {
                return false
            }

            val bool = (token == userToken) && (id == userId)

            accountPrefs = null
            return bool
        }
    }

    private fun configureUi() {
        materialToolbar?.let { v ->
            ViewCompat.setOnApplyWindowInsetsListener(v) { view, listener ->
                val insets = listener.getInsets(WindowInsetsCompat.Type.systemBars())
                view.updatePadding(insets.left, insets.top, insets.right, 0)
                WindowInsetsCompat.CONSUMED
            }
        }
        bottomNavigation?.let { v ->
            ViewCompat.setOnApplyWindowInsetsListener(v) { view, listener ->
                val insets = listener.getInsets(WindowInsetsCompat.Type.systemBars())
                view.updatePadding(insets.left, 0, insets.right, insets.bottom)
                WindowInsetsCompat.CONSUMED
            }
            v.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.home_menu -> {
                        viewPager?.setCurrentItem(0, true)
                        return@setOnItemSelectedListener true
                    }

                    R.id.cat_controls_menu -> {
                        viewPager?.setCurrentItem(1, true)
                        return@setOnItemSelectedListener true
                    }

                    R.id.user_menu -> {
                        viewPager?.setCurrentItem(2, true)
                        return@setOnItemSelectedListener true
                    }

                    else -> {
                        return@setOnItemSelectedListener false
                    }
                }
            }
        }
        viewPager?.let {
            it.adapter = pagerAdapter
            it.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    bottomNavigation?.apply {
                        selectedItemId = when (position) {
                            1 -> R.id.cat_controls_menu
                            2 -> R.id.user_menu
                            else -> R.id.home_menu
                        }
                    }
                }
            })
        }
    }
}

class NekoMainAdapter(fragment: FragmentActivity) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            1 -> CatControlsFragment()
            2 -> UserFragment()
            else -> HomeFragment()
        }
    }
}