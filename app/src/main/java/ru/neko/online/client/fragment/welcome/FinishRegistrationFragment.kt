package ru.neko.online.client.fragment.welcome

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import ru.neko.online.client.R
import ru.neko.online.client.activity.WelcomeActivity
import ru.neko.online.client.components.AccountPrefs

class FinishRegistrationFragment: Fragment(R.layout.finish_registration_fragment) {

    private var cancelButton: MaterialButton? = null
    private var finishRegButton: MaterialButton? = null

    private var nicknameTextView: MaterialTextView? = null

    private var prefs: AccountPrefs? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context?.let {
            prefs = AccountPrefs(it)
        }
        cancelButton = view.findViewById<MaterialButton>(R.id.cancel_button)
        finishRegButton = view.findViewById<MaterialButton>(R.id.finish_reg_button)
        nicknameTextView = view.findViewById<MaterialTextView>(R.id.registration_username_text)

        setUi()
    }

    private fun setUi() {
        prefs?.let {
            nicknameTextView?.text = context?.getString(R.string.username_finish_registration_tip, it.accountUsername)
        }
        cancelButton?.setOnClickListener {
            (activity as WelcomeActivity?)?.setFragment(-1, true)
        }
        finishRegButton?.setOnClickListener {

        }
    }
}