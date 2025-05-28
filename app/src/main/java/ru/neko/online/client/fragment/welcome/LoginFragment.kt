package ru.neko.online.client.fragment.welcome

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import ru.neko.online.client.R
import ru.neko.online.client.activity.WelcomeActivity

class LoginFragment : Fragment(R.layout.login_fragment) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val register = view.findViewById<MaterialButton>(R.id.register_button)
        register.setOnClickListener {
            (activity as WelcomeActivity?)?.setFragment(0, false)
        }
        (activity as WelcomeActivity?)?.setToolbarTitle("Вход")
    }
}