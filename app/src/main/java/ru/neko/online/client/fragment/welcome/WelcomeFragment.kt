package ru.neko.online.client.fragment.welcome

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import ru.neko.online.client.R
import ru.neko.online.client.activity.WelcomeActivity
import ru.neko.online.client.components.Cat
import kotlin.random.Random

class WelcomeFragment : Fragment(R.layout.welcome_fragment) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val registerButton: MaterialButton = view.findViewById<MaterialButton>(R.id.register_button)
        val loginButton: MaterialButton = view.findViewById<MaterialButton>(R.id.login_button)
        val randomCatIcon: ImageView = view.findViewById<ImageView>(R.id.random_cat_icon)
        val settings: ImageView = view.findViewById<ImageView>(R.id.server_config_imageview)
        context?.let {
            val size = it.resources.getDimensionPixelSize(R.dimen.neko_display_size)
            randomCatIcon.setImageBitmap(Cat(it, Random.nextLong(), null, null).createBitmap(size, size))
        }
        (activity as WelcomeActivity?)?.setDefaultToolbarTitle()
        registerButton.setOnClickListener {
            register()
        }
        loginButton.setOnClickListener {
            login()
        }
        settings.setOnClickListener {
            (activity as WelcomeActivity?)?.setFragment(4, false)
        }
    }

    private fun register() {
        (activity as WelcomeActivity?)?.setFragment(0, false)
    }

    private fun login() {
        (activity as WelcomeActivity?)?.setFragment(1, false)
    }
}