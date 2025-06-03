package ru.neko.online.client.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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
import org.json.JSONObject
import ru.neko.online.client.R
import ru.neko.online.client.components.AccountPrefs
import ru.neko.online.client.components.network.NetworkManager
import ru.neko.online.client.components.models.network.TokenUser
import ru.neko.online.client.components.utils.BottomSheet
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

    private var infoBottomSheet: BottomSheet? = null

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
        infoBottomSheet = BottomSheet(this)
        bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        linearLayout = findViewById<LinearLayoutCompat>(R.id.main_linear_layout)
        syncIndicator = findViewById<LinearLayoutCompat>(R.id.sync_indicator)
        materialToolbar = findViewById<MaterialToolbar>(R.id.toolbar)

        setSupportActionBar(materialToolbar)

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
            connectionSnackbar = null
            return
        }
        lifecycleScope.launch {
            val results = syncUserData(this@MainActivity)
            val connectionBool = results.second
            if (!connectionBool) {
                withContext(Dispatchers.Main) {
                    viewPager?.animate()?.alpha(0.5f)?.setDuration(200)?.start()
                    createSnackbar()
                    editSnackbarText(getString(R.string.snackbar_connection_error, 5))
                    showSnackbar()
                }
                for (i in 5 downTo 0) {
                    delay(1000)
                    withContext(Dispatchers.Main) {
                        editSnackbarText(getString(R.string.snackbar_connection_error, i))
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
                Log.d("Main", "Ошибка при получении данных")
                withContext(Dispatchers.Main) {
                    viewPager?.animate()?.alpha(0.5f)?.setDuration(200)?.start()
                    Toast.makeText(this@MainActivity, "Ошибка входа", Toast.LENGTH_LONG).show()
                }
            }
            if(dataStatus && connectionBool) {
                viewPager?.let {
                    if(it.alpha != 1f) it.animate().alpha(1f).setDuration(200).start()
                }
            }
        }
    }

    private fun showOfflineDialog(context: Context) {
        MaterialAlertDialogBuilder(context)
            .setTitle(getString(R.string.error_dialog_title))
            .setIcon(R.drawable.ic_cloud_off)
            .setMessage(getString(R.string.main_dialog_connection_error))
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(getString(R.string.dialog_support_btn)) { _, _ ->
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
            }.start()
    }

    // first - data status
    // second - connection status
    private suspend fun syncUserData(context: Context): Pair<Boolean, Boolean> {
        Log.d("Network", "Sync data")
        var accountPrefs: AccountPrefs? = AccountPrefs(context)

        val token = accountPrefs?.userToken

        if (token == null) {
            return Pair(false, false)
        }
        withContext(Dispatchers.Main) {
            animateSyncIndicator(false)
        }
        val network = NetworkManager(context)
        val result = network.networkPost("userprefs", TokenUser(token))

        val jsonObj: JSONObject? = result.first
        val status: Int = result.second

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

            accountPrefs.apply {
                userDataName = jsonObj.getString("name")
                userDataNCoins = jsonObj.getInt("ncoins")
                userDataFood = jsonObj.getInt("food")
                userDataWater = jsonObj.getInt("water")
                userDataToys = jsonObj.getInt("toys")
            }

            accountPrefs = null

            return Pair(true, true)
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

    private fun exit(context: Context) {
        var accountPrefs: AccountPrefs? = AccountPrefs(context)
        accountPrefs?.clearUserData()
        accountPrefs = null
        var prefs: Prefs? = Prefs(context)
        prefs?.isFirstLaunch = true
        prefs = null

        recreate()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun showExitDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.dialog_warning_title))
            .setIcon(R.drawable.ic_login)
            .setMessage(getString(R.string.dialog_logout_message))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                exit(this)
            }
            .setNegativeButton(
                getString(R.string.no), null
            )
            .setCancelable(false)
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.exit_menu -> {
                showExitDialog()
                return true
            }

            R.id.settings_menu -> {
                return true
            }

            R.id.help_menu -> {
                infoBottomSheet?.getInfoBottomSheet(R.layout.help_bottomsheet)?.show()
                return true
            }

            R.id.about_menu -> {
                infoBottomSheet?.getInfoBottomSheet(R.layout.about_bottomsheet)?.show()
                return true
            }

            else -> return super.onContextItemSelected(item)
        }
    }

    override fun onDestroy() {
        connectionSnackbar = null
        prefs = null
        infoBottomSheet = null
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