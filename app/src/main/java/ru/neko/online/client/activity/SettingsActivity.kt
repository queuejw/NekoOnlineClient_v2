package ru.neko.online.client.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.slider.Slider
import ru.neko.online.client.R
import ru.neko.online.client.config.Prefs


class SettingsActivity: AppCompatActivity() {

    private var materialToolbar: MaterialToolbar? = null

    private var syncSlider: Slider? = null

    private var prefs: Prefs? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.setttings_activity)

        prefs = Prefs(this)

        materialToolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        syncSlider = findViewById<Slider>(R.id.auto_sync_slider)

        setSupportActionBar(materialToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        configureUi()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        prefs = null
    }

    private fun configureUi() {
        materialToolbar?.let { v ->
            ViewCompat.setOnApplyWindowInsetsListener(v) { view, listener ->
                val insets = listener.getInsets(WindowInsetsCompat.Type.systemBars())
                view.updatePadding(insets.left, insets.top, insets.right, 0)
                WindowInsetsCompat.CONSUMED
            }
        }
        syncSlider?.let {
            prefs?.let { p ->
                it.value = p.autoSyncTime.toFloat()
            }
            it.addOnChangeListener { slider, value, fromUser ->
                prefs?.autoSyncTime = value.toInt()
            }
        }
    }
}