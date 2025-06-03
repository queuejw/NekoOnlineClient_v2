package ru.neko.online.client.components.error

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import ru.neko.online.client.BuildConfig
import ru.neko.online.client.R
import ru.neko.online.client.activity.MainActivity

class CriticalErrorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.critical_error_screen)

        val stacktrace = intent.extras?.getString("stacktrace")

        val device = "\nDevice: ${Build.MODEL}\n"
        val nekoVersion = "\nNeko Online Version: ${BuildConfig.VERSION_NAME}\n"
        val android = "Android Version: ${Build.VERSION.SDK_INT}\n\n"

        val resultErrorData = device + nekoVersion + android + stacktrace

        val errorDetailsTextView = findViewById<MaterialTextView>(R.id.error_details_text)
        errorDetailsTextView.text = resultErrorData

        val restart = findViewById<MaterialButton>(R.id.critical_error_restart_game_button)
        val sendDataBtn = findViewById<MaterialButton>(R.id.critical_error_send_data_button)
        val tgBtn = findViewById<MaterialButton>(R.id.critical_error_tg_button)

        restart.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            finish()
        }
        tgBtn.setOnClickListener {
            openTg()
        }
        sendDataBtn.setOnClickListener {
            copyError(resultErrorData)
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = "mailto:dimon6018t@gmail.com".toUri()
            intent.putExtra(Intent.EXTRA_SUBJECT, "Neko Online Crash report")
            intent.putExtra(Intent.EXTRA_TEXT, resultErrorData)
            try {
                startActivity(intent)
            } catch (_: ActivityNotFoundException) {
                showErrorDialog(this)
            }
        }
    }

    private fun openTg() {
        startActivity(Intent(Intent.ACTION_VIEW).setData("https://t.me/neko_online".toUri()))
    }

    private fun showErrorDialog(context: Context) {
        MaterialAlertDialogBuilder(context)
            .setTitle(getString(R.string.error_dialog_title))
            .setIcon(R.drawable.ic_error)
            .setMessage(getString(R.string.critical_error_email_error))
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(getString(R.string.dialog_support_btn)) { _, _ ->
                startActivity(Intent(Intent.ACTION_VIEW).setData("https://t.me/neko_online".toUri()))
            }
            .setCancelable(false)
            .show()
    }

    private fun copyError(error: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("neko_stacktrace", error)
        clipboard.setPrimaryClip(clip)
    }
}