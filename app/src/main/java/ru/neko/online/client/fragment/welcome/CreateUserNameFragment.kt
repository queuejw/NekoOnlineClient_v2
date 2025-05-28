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

class CreateUserNameFragment : Fragment(R.layout.create_user_name_fragment) {

    private var editText: TextInputEditText? = null
    private var nextButton: MaterialButton? = null
    private var cancelButton: MaterialButton? = null

    private var prefs: AccountPrefs? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context?.let {
            prefs = AccountPrefs(it)
        }
        editText = view.findViewById<TextInputEditText>(R.id.edit_text)
        cancelButton = view.findViewById<MaterialButton>(R.id.cancel_button)
        nextButton = view.findViewById<MaterialButton>(R.id.next_button)
        (activity as WelcomeActivity?)?.setToolbarTitle("Как тебя зовут?")
        setUi()
    }

    private fun setUi() {
        editText?.addTextChangedListener(object : TextWatcher {
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
                editText?.let {
                    nextButton?.isEnabled = p0?.toString()?.isEmpty() == false
                }
            }

        })
        cancelButton?.setOnClickListener {
            (activity as WelcomeActivity?)?.setFragment(-1, true)
        }
        nextButton?.setOnClickListener {
            editText?.let {
                prefs?.accountUsername = it.text.toString()
            }
            (activity as WelcomeActivity?)?.setFragment(2, false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        prefs = null
    }
}