package ru.neko.online.client.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.net.toUri
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
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
    private var connectionSnackbar: Snackbar? = null

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
        prepareClient(0)
    }

    private fun editSnackbarText(newText: String) {
        connectionSnackbar?.setText(newText)
    }

    private fun createSnackbar() {
        if (connectionSnackbar == null) {
            viewPager?.let {
                connectionSnackbar = Snackbar.make(it, "null", Snackbar.LENGTH_INDEFINITE)
            }
        }
    }

    private fun showSnackbar() {
        connectionSnackbar?.let {
            if (it.isShown) it.dismiss()
            it.show()
        }
    }

    private fun prepareClient(attempt: Int) {
        if (attempt > 3) {
            showOfflineDialog(this)
            return
        }
        lifecycleScope.launch {
            val results = checkUser(this@MainActivity)
            val connectionBool = results.second
            if (!connectionBool) {
                withContext(Dispatchers.Main) {
                    createSnackbar()
                    editSnackbarText("Не удалось подключиться к серверу. Попробуем cнова через 5 секунд")
                    showSnackbar()
                }
                for (i in 5 downTo 0) {
                    delay(1000)
                    withContext(Dispatchers.Main) {
                        editSnackbarText("Не удалось подключиться к серверу. Попробуем cнова через $i секунд")
                    }
                }
                withContext(Dispatchers.Main) {
                    connectionSnackbar?.dismiss()
                }
                prepareClient(attempt + 1)
                cancel()
            }
            val dataStatus = results.first
            if (!dataStatus && connectionBool) {
                Log.d("Main", "Ошибка входа")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Ошибка входа", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showOfflineDialog(context: Context) {
        MaterialAlertDialogBuilder(context)
            .setTitle("Ой!")
            .setIcon(R.drawable.ic_error)
            .setMessage("Не удалось установить связь с сервером. Проверьте подключение к интернету.")
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton("Поддержка") { _, _ ->
                startActivity(Intent(Intent.ACTION_VIEW).setData("https://t.me/neko_online".toUri()))
            }
            .setCancelable(false)
            .show()
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

    // first - data status
    // second - connection status
    private suspend fun checkUser(context: Context): Pair<Boolean, Boolean> {
        var accountPrefs: AccountPrefs? = AccountPrefs(context)

        val username = accountPrefs?.accountUsername
        val password = accountPrefs?.accountPassword

        if (username == null || password == null) {
            return Pair(false, false)
        }
        withContext(Dispatchers.Main) {
            animateSyncIndicator(false)
        }
        val network = NetworkManager(context)
        val result = network.login(username, password)

        val jsonObj = result.first
        val status = result.second

        withContext(Dispatchers.Main) {
            network.closeClient()
            animateSyncIndicator(true)
        }
        if (status == HttpStatusCode.ServiceUnavailable.value) {
            return Pair(false, false)
        }
        if (jsonObj == null) {
            accountPrefs = null
            return Pair(false, true)

        } else {
            val token = jsonObj.get("token").toString()
            val id = jsonObj.get("id").toString().toLong()

            val userToken = accountPrefs.userToken
            val userId = accountPrefs.userId

            Log.d("NET", "Server token - $token")
            Log.d("NET", "Local token - $userToken")

            Log.d("NET", "Server id - $id")
            Log.d("NET", "Local id - $userId")

            if (userToken == null) {
                return Pair(false, true)
            }

            val bool = (token == userToken) && (id == userId)

            accountPrefs = null
            return Pair(bool, true)
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

    override fun onDestroy() {
        connectionSnackbar = null
        prefs = null
        super.onDestroy()
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