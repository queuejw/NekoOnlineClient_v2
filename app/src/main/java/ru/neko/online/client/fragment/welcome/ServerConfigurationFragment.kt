package ru.neko.online.client.fragment.welcome

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import ru.neko.online.client.R
import ru.neko.online.client.activity.WelcomeActivity
import ru.neko.online.client.components.AccountPrefs

class ServerConfigurationFragment : Fragment(R.layout.server_configuration_fragment) {

    private var serverEditText: TextInputEditText? = null
    private var portEditText: TextInputEditText? = null

    private var saveButton: MaterialButton? = null
    private var cancelButton: MaterialButton? = null
    private var resetButton: MaterialButton? = null

    private var prefs: AccountPrefs? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context?.let {
            prefs = AccountPrefs(it)
        }
        (activity as WelcomeActivity?)?.setToolbarTitle("Конфигурация сервера")
        serverEditText = view.findViewById<TextInputEditText>(R.id.server_edit_text)
        portEditText = view.findViewById<TextInputEditText>(R.id.port_edit_text)
        saveButton = view.findViewById<MaterialButton>(R.id.save_server_config_button)
        cancelButton = view.findViewById<MaterialButton>(R.id.cancel_button)
        resetButton = view.findViewById<MaterialButton>(R.id.reset_server_config_button)

        setUi()
    }

    private fun setUi() {
        prefs?.let {
            serverEditText?.setText(it.serverAddress)
            portEditText?.setText(it.serverPort.toString())
        }

        cancelButton?.setOnClickListener {
            closeSettings()
        }
        saveButton?.setOnClickListener {
            saveData()
        }
        resetButton?.setOnClickListener {
            context?.let {
                if(prefs?.clearServerConfig() == true) {
                    showDialog(it)
                } else {
                    showErrorDialog(it)
                }
            }
        }
    }

    private fun saveData() {
        var successful = true
        val address = serverEditText?.text.toString()
        if(address.isEmpty()) {
            successful = false
        } else {
            prefs?.serverAddress = address
        }
        val port = portEditText?.text.toString()
        if (port.isEmpty()) {
            successful = false
        } else {
            prefs?.serverPort = port.toInt()
        }
        context?.let {
            if(successful) {
                showDialog(it)
            } else {
                showErrorDialog(it)
            }
        }
    }

    private fun closeSettings() {
        (activity as WelcomeActivity?)?.setFragment(-1, true)
    }

    private fun showDialog(context: Context) {
        MaterialAlertDialogBuilder(context)
            .setTitle("Успешно")
            .setIcon(R.drawable.ic_check)
            .setMessage("Конфигурация сервера была изменена.")
            .setCancelable(false)
            .setPositiveButton(getString(android.R.string.ok)) { _, _ ->
                closeSettings()
            }
            .show()
    }

    private fun showErrorDialog(context: Context) {
        MaterialAlertDialogBuilder(context)
            .setTitle("Ой")
            .setIcon(R.drawable.ic_check)
            .setMessage("Не получилось сохранить изменения. Проверьте конфигурацию сервера.")
            .setPositiveButton(getString(android.R.string.ok), null)
            .show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        prefs = null
    }
}
