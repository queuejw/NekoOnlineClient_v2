package ru.neko.online.client.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import ru.neko.online.client.R
import ru.neko.online.client.config.Prefs
import ru.neko.online.client.fragment.game.CatControlsFragment
import ru.neko.online.client.fragment.game.HomeFragment
import ru.neko.online.client.fragment.game.UserFragment

class MainActivity : AppCompatActivity() {

    private var viewPager: ViewPager2? = null
    private var pagerAdapter: FragmentStateAdapter? = null

    private var linearLayout: LinearLayoutCompat? = null
    private var materialToolbar: MaterialToolbar? = null

    private var bottomNavigation: BottomNavigationView? = null

    private var prefs: Prefs? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = Prefs(this)
        prefs?.let {
            if(it.isFirstLaunch) {
                val intent = Intent(this, WelcomeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                return
            }
        }
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.main_activity)
        viewPager = findViewById<ViewPager2>(R.id.viewpager)
        pagerAdapter = NekoMainAdapter(this)
        bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        linearLayout = findViewById<LinearLayoutCompat>(R.id.main_linear_layout)
        materialToolbar = findViewById<MaterialToolbar>(R.id.toolbar)

        configureUi()
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
        bottomNavigation?.setOnItemSelectedListener { item ->
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