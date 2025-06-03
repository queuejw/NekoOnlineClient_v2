package ru.neko.online.client.fragment.welcome

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import ru.neko.online.client.R
import ru.neko.online.client.activity.WelcomeActivity
import ru.neko.online.client.components.AccountPrefs

class CreatePasswordFragment : Fragment(R.layout.create_password_fragment) {

    private var editText1: TextInputEditText? = null
    private var editText2: TextInputEditText? = null
    private var nextButton: MaterialButton? = null
    private var cancelButton: MaterialButton? = null

    private var passwordInputLayout: TextInputLayout? = null
    private var passwordInputLayout2: TextInputLayout? = null

    private var prefs: AccountPrefs? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context?.let {
            prefs = AccountPrefs(it)
        }
        editText1 = view.findViewById<TextInputEditText>(R.id.edit_text_1)
        editText2 = view.findViewById<TextInputEditText>(R.id.edit_text_2)
        nextButton = view.findViewById<MaterialButton>(R.id.next_button)
        cancelButton = view.findViewById<MaterialButton>(R.id.cancel_button)
        passwordInputLayout = view.findViewById<TextInputLayout>(R.id.password_text_input_layout_1)
        passwordInputLayout2 = view.findViewById<TextInputLayout>(R.id.password_text_input_layout_2)

        (activity as WelcomeActivity?)?.setToolbarTitle(getString(R.string.register_activity_title_2))
        setUi()
    }

    private fun checkTextLayouts() {
        if(passwordInputLayout != null && passwordInputLayout2 != null) {
            var isBlocked = false
            if (editText1!!.text != null) {
                val text = editText1!!.text.toString()
                if (text.length > 25) {
                    passwordInputLayout!!.error = getString(R.string.password_error)
                    nextButton?.isEnabled = false
                    isBlocked = true
                } else {
                    nextButton?.isEnabled = editText1!!.text.toString() == editText2!!.text.toString()
                    passwordInputLayout!!.error = null
                }
            }
            if (editText2!!.text != null) {
                val text = editText2!!.text.toString()
                if (text.length > 25) {
                    passwordInputLayout2!!.error = getString(R.string.password_error)
                    nextButton?.isEnabled = false
                    isBlocked = true
                } else {
                    if (!isBlocked) {
                        nextButton?.isEnabled = editText1!!.text.toString() == editText2!!.text.toString()
                    }
                    passwordInputLayout2!!.error = null
                }
            }
        }
    }

    private fun setUi() {
        editText2?.addTextChangedListener(object : TextWatcher {
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
                if(editText1 != null && editText2 != null) {
                    checkTextLayouts()
                }
            }

        })

        editText1?.addTextChangedListener(object : TextWatcher {
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
                if(editText1 != null && editText2 != null) {
                    checkTextLayouts()
                }
            }

        })

        cancelButton?.setOnClickListener {
            (activity as WelcomeActivity?)?.setFragment(0, true)
        }
        nextButton?.setOnClickListener {
            editText2?.let {
                prefs?.accountPassword = it.text.toString()
            }
            (activity as WelcomeActivity?)?.setFragment(3, false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        prefs = null
    }
}