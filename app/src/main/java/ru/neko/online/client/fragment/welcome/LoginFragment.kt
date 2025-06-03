package ru.neko.online.client.fragment.welcome

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.neko.online.client.R
import ru.neko.online.client.activity.MainActivity
import ru.neko.online.client.activity.WelcomeActivity
import ru.neko.online.client.components.AccountPrefs
import ru.neko.online.client.components.network.NetworkManager
import ru.neko.online.client.components.network.serializable.LoginUser
import ru.neko.online.client.config.Prefs

class LoginFragment : Fragment(R.layout.login_fragment) {

    private var registerButton: MaterialButton? = null
    private var loginButton: MaterialButton? = null

    private var usernameEditText: TextInputEditText? = null
    private var passwordEditText: TextInputEditText? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerButton = view.findViewById<MaterialButton>(R.id.register_button)
        loginButton = view.findViewById<MaterialButton>(R.id.login_button)
        usernameEditText = view.findViewById<TextInputEditText>(R.id.username_edit_text)
        passwordEditText = view.findViewById<TextInputEditText>(R.id.password_edit_text)
        (activity as WelcomeActivity?)?.setToolbarTitle(getString(R.string.login_activity_title))
        setUi()
    }

    private fun setUi() {
        registerButton?.setOnClickListener {
            (activity as WelcomeActivity?)?.setFragment(0, false)
        }
        loginButton?.setOnClickListener {
            context?.let {
                login(it)
            }
        }
        passwordEditText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(
                p0: CharSequence?,
                p1: Int,
                p2: Int,
                p3: Int
            ) {
            }

            override fun onTextChanged(
                p0: CharSequence?,
                p1: Int,
                p2: Int,
                p3: Int
            ) {
                usernameEditText?.let {
                    loginButton?.isEnabled =
                        (p0?.isEmpty() == false) && usernameEditText!!.text?.isNotEmpty() == true
                }
            }

        })
    }

    private fun createLoadingDialog(context: Context): AlertDialog {
        return MaterialAlertDialogBuilder(context)
            .setView(R.layout.loading_dialog)
            .setCancelable(false)
            .create()
    }

    private fun login(context: Context) {
        if (usernameEditText != null && passwordEditText != null) {

            val dialog = createLoadingDialog(context)

            dialog.show()

            val username = usernameEditText!!.text.toString()
            val password = passwordEditText!!.text.toString()
            if (username.isNotEmpty() && password.isNotEmpty()) {
                lifecycleScope.launch {
                    val network = NetworkManager(context)
                    val result = network.networkPost("login", LoginUser(username, password))

                    val status = result.second
                    val jsonObj = result.first

                    withContext(Dispatchers.Main) {
                        network.closeClient()
                    }

                    if (jsonObj == null) {
                        withContext(Dispatchers.Main) {
                            dialog.dismiss()
                            when (status) {
                                HttpStatusCode.ServiceUnavailable.value -> showErrorDialog(
                                    context,
                                    getString(R.string.dialog_connection_error)
                                )

                                HttpStatusCode.Unauthorized.value -> showErrorDialog(
                                    context,
                                    getString(R.string.dialog_account_error)
                                )

                                else -> showErrorDialog(
                                    context,
                                    getString(R.string.dialog_other_error)
                                )
                            }
                        }
                    } else {
                        val token = jsonObj.getString("token")
                        val id = jsonObj.getLong("id")

                        var accountPrefs: AccountPrefs? = AccountPrefs(context)
                        var appPrefs: Prefs? = Prefs(context)
                        accountPrefs?.apply {
                            userToken = token
                            userId = id
                        }
                        if (status != HttpStatusCode.OK.value) {
                            accountPrefs = null
                            appPrefs = null
                            withContext(Dispatchers.Main) {
                                dialog.dismiss()
                                showErrorDialog(context, getString(R.string.dialog_unexpected_error))
                            }
                        } else {
                            appPrefs?.apply {
                                isFirstLaunch = false
                            }
                            accountPrefs?.apply {
                                accountPassword = password
                                accountUsername = username
                            }
                            accountPrefs = null
                            appPrefs = null
                            withContext(Dispatchers.Main) {
                                dialog.dismiss()
                                val intent = Intent(
                                    context,
                                    MainActivity::class.java
                                ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK
                                )
                                startActivity(intent)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showErrorDialog(context: Context, errorMessage: String) {
        MaterialAlertDialogBuilder(context)
            .setTitle(getString(R.string.error_dialog_title))
            .setIcon(R.drawable.ic_error)
            .setMessage(errorMessage)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(getString(R.string.dialog_support_btn)) { _, _ ->
                startActivity(Intent(Intent.ACTION_VIEW).setData("https://t.me/neko_online".toUri()))
            }
            .setCancelable(false)
            .show()
    }
}