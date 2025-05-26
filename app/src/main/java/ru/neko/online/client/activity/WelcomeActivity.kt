package ru.neko.online.client.activity

import android.os.Bundle
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
import ru.neko.online.client.fragment.welcome.CreatePasswordFragment
import ru.neko.online.client.fragment.welcome.CreateUserNameFragment
import ru.neko.online.client.fragment.welcome.FinishRegistrationFragment
import ru.neko.online.client.fragment.welcome.LoginFragment
import ru.neko.online.client.fragment.welcome.WelcomeFragment

class WelcomeActivity : AppCompatActivity() {

    private var fragmentContainer: FragmentContainerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.welcome_activity)
        fragmentContainer = findViewById<FragmentContainerView>(R.id.fragment_container)
        setUi()
        setFragment(-1, false)
    }

    private fun setUi() {
        val materialToolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        materialToolbar?.let { v ->
            ViewCompat.setOnApplyWindowInsetsListener(v) { view, listener ->
                val insets = listener.getInsets(WindowInsetsCompat.Type.systemBars())
                view.updatePadding(insets.left, insets.top, insets.right, 0)
                WindowInsetsCompat.CONSUMED
            }
        }
    }

    private fun getFragment(value: Int): Fragment {
        return when (value) {
            0 -> CreateUserNameFragment()
            1 -> LoginFragment()
            2 -> CreatePasswordFragment()
            3 -> FinishRegistrationFragment()
            else -> WelcomeFragment()
        }
    }

    fun setFragment(fragment: Int, inverseTranslation: Boolean) {
        val translation = if (!inverseTranslation) -125f else 125f
        fragmentContainer?.let {
            it.animate().alpha(0f).translationX(translation).setDuration(150).withEndAction {
                it.translationX = if(inverseTranslation) -125f else 125f
                supportFragmentManager.commit {
                    replace(it.id, getFragment(fragment))
                }
                it.animate().alpha(1f).translationX(0f).setDuration(150).start()
            }.start()
        }
    }
}