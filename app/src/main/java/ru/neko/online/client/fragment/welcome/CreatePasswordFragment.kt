package ru.neko.online.client.fragment.welcome

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import ru.neko.online.client.R
import ru.neko.online.client.activity.WelcomeActivity
import ru.neko.online.client.components.AccountPrefs

class CreatePasswordFragment : Fragment(R.layout.create_password_fragment) {

    private var editText1: TextInputEditText? = null
    private var editText2: TextInputEditText? = null
    private var nextButton: MaterialButton? = null
    private var cancelButton: MaterialButton? = null

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
        setUi()
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
                editText1?.let {
                    if (p0?.isEmpty() == false) {
                        nextButton?.isEnabled = p0.toString() == it.text.toString()
                    } else {
                        nextButton?.isEnabled = false
                    }
                }
            }

        })

        cancelButton?.setOnClickListener {
            (activity as WelcomeActivity?)?.setFragment(-1, true)
        }
        nextButton?.setOnClickListener {
            editText2?.let {
                prefs?.accountPassword = it.text.toString()
            }
            (activity as WelcomeActivity?)?.setFragment(3, true)
        }
    }
}