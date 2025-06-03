package ru.neko.online.client.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import com.google.android.material.appbar.MaterialToolbar
import ru.neko.online.client.R
import ru.neko.online.client.components.utils.BottomSheet
import ru.neko.online.client.fragment.welcome.CreatePasswordFragment
import ru.neko.online.client.fragment.welcome.CreateUserNameFragment
import ru.neko.online.client.fragment.welcome.FinishRegistrationFragment
import ru.neko.online.client.fragment.welcome.LoginFragment
import ru.neko.online.client.fragment.welcome.ServerConfigurationFragment
import ru.neko.online.client.fragment.welcome.WelcomeFragment

class WelcomeActivity : AppCompatActivity() {

    private var fragmentContainer: FragmentContainerView? = null

    private var toolbar: MaterialToolbar? = null

    private var infoBottomSheet: BottomSheet? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.welcome_activity)
        infoBottomSheet = BottomSheet(this)
        fragmentContainer = findViewById<FragmentContainerView>(R.id.fragment_container)
        toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setUi()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {}
        })
        setFragment(-1, false)
    }

    private fun setUi() {
        toolbar?.let { v ->
            setSupportActionBar(v)
            ViewCompat.setOnApplyWindowInsetsListener(v) { view, listener ->
                val insets = listener.getInsets(WindowInsetsCompat.Type.systemBars())
                view.updatePadding(insets.left, insets.top, insets.right, 0)
                WindowInsetsCompat.CONSUMED
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.welcome_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

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


    fun setToolbarTitle(newTitle: String) {
        toolbar?.title = newTitle
    }

    fun setDefaultToolbarTitle() {
        toolbar?.title = getString(R.string.welcome)
    }

    private fun getFragment(value: Int): Fragment {
        return when (value) {
            0 -> CreateUserNameFragment()
            1 -> LoginFragment()
            2 -> CreatePasswordFragment()
            3 -> FinishRegistrationFragment()
            4 -> ServerConfigurationFragment()
            else -> WelcomeFragment()
        }
    }

    fun setFragment(fragment: Int, inverseTranslation: Boolean) {
        val translation = if (!inverseTranslation) -125f else 125f
        fragmentContainer?.let {
            it.animate().alpha(0f).translationX(translation).setDuration(150).withEndAction {
                it.translationX = if (inverseTranslation) -125f else 125f
                supportFragmentManager.commit(true) {
                    replace(it.id, getFragment(fragment))
                }
                it.animate().alpha(1f).translationX(0f).setDuration(150).start()
            }.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        infoBottomSheet = null
    }
}