package ru.neko.online.client.fragment.welcome

import android.content.Context
import android.os.Bundle
import android.view.View
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
import ru.neko.online.client.activity.WelcomeActivity
import ru.neko.online.client.components.AccountPrefs
import ru.neko.online.client.components.network.NetworkManager

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

    private fun regPlayer(context: Context) {
        val dialog = MaterialAlertDialogBuilder(context)
            .setView(R.layout.registration_loading_dialog)
            .setCancelable(false)
            .create()

        dialog.show()

        lifecycleScope.launch {
            prefs?.let {
                val network = NetworkManager(context)
                val result =
                    network.register(it.accountName!!, it.accountUsername!!, it.accountPassword!!)
                withContext(Dispatchers.Main) {
                    network.closeClient()
                    dialog.dismiss()
                    when (result) {
                        HttpStatusCode.OK.value -> showSuccessDialog(context)
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
            }
        }
    }

    private fun showSuccessDialog(context: Context) {
        MaterialAlertDialogBuilder(context)
            .setTitle("Добро пожаловать!")
            .setMessage("Аккаунт успешно создан. Давайте начнём игру!")
            .setPositiveButton("Начать", null)
            .setCancelable(false)
            .show()
    }

    private fun showErrorDialog(context: Context, errorMessage: String) {
        MaterialAlertDialogBuilder(context)
            .setTitle("Ой!")
            .setIcon(R.drawable.ic_error)
            .setMessage(errorMessage)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton("Поддержка", null)
            .setCancelable(false)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        prefs = null
    }
}