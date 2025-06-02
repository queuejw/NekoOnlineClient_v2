package ru.neko.online.client.fragment.welcome

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.neko.online.client.R
import ru.neko.online.client.activity.MainActivity
import ru.neko.online.client.activity.WelcomeActivity
import ru.neko.online.client.components.AccountPrefs
import ru.neko.online.client.components.network.NetworkManager
import ru.neko.online.client.components.network.serializable.RegUser
import ru.neko.online.client.config.Prefs

class FinishRegistrationFragment : Fragment(R.layout.finish_registration_fragment) {

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

        (activity as WelcomeActivity?)?.setToolbarTitle("Финиш!")

        setUi()
    }

    private fun setUi() {
        prefs?.let {
            nicknameTextView?.text =
                context?.getString(R.string.username_finish_registration_tip, it.accountName)
        }
        cancelButton?.setOnClickListener {
            (activity as WelcomeActivity?)?.setFragment(2, true)
        }
        finishRegButton?.setOnClickListener {
            context?.let {
                finishRegButton?.isEnabled = false
                regPlayer(it)
            }
        }
    }

    private fun createLoadingDialog(context: Context): AlertDialog {
        return MaterialAlertDialogBuilder(context)
            .setView(R.layout.loading_dialog)
            .setCancelable(false)
            .create()
    }

    private fun regPlayer(context: Context) {
        val dialog = createLoadingDialog(context)

        dialog.show()

        lifecycleScope.launch {
            prefs?.let {
                val network = NetworkManager(context)
                val result =
                    network.networkPost("register", RegUser(it.accountName!!, it.accountUsername!!, it.accountPassword!!))

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
                                "Нам не удалось подключиться к серверу. Проверьте подключение к интернету или попробуйте позже."
                            )

                            HttpStatusCode.Conflict.value -> showErrorDialog(
                                context,
                                "Аккаунт с таким никнеймом уже существует. Попробуйте ввести другой никнейм."
                            )

                            else -> showErrorDialog(
                                context,
                                "Нам не получилось создать аккаунт. Попробуйте ещё раз. Если не получается, попробуйте создать аккаунт позже или обратитесь к нам."
                            )
                        }
                        finishRegButton?.isEnabled = true
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
                    accountPrefs = null
                    withContext(Dispatchers.Main) {
                        dialog.dismiss()
                        when (status) {
                            HttpStatusCode.OK.value -> {
                                appPrefs?.apply {
                                    isFirstLaunch = false
                                }
                                appPrefs = null
                                showSuccessDialog(context)
                            }

                            else -> showErrorDialog(
                                context,
                                "Нам не получилось создать аккаунт. Попробуйте ещё раз. Если не получается, попробуйте создать аккаунт позже или обратитесь к нам."
                            )
                        }
                    }
                }
            }
        }
    }

    private fun showSuccessDialog(context: Context) {
        MaterialAlertDialogBuilder(context)
            .setTitle("Добро пожаловать!")
            .setIcon(R.drawable.ic_fullcat_icon)
            .setMessage("Аккаунт успешно создан.\nДавайте начнём игру!")
            .setPositiveButton("Начать") { _, _ ->
                val intent = Intent(
                    context,
                    MainActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )
                startActivity(intent)
            }
            .setCancelable(false)
            .show()
    }

    private fun showErrorDialog(context: Context, errorMessage: String) {
        MaterialAlertDialogBuilder(context)
            .setTitle("Ой!")
            .setIcon(R.drawable.ic_error)
            .setMessage(errorMessage)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton("Поддержка") { _, _ ->
                startActivity(Intent(Intent.ACTION_VIEW).setData("https://t.me/neko_online".toUri()))
            }
            .setCancelable(false)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        prefs = null
    }
}