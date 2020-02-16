package net.hermlon.gcgtimetable.ui.profile.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import net.hermlon.gcgtimetable.R
import net.hermlon.gcgtimetable.databinding.FragmentStundenplan24loginBinding

class Stundenplan24LoginFragment : Fragment() {

    private lateinit var viewModel: LoginFragmentViewModel
    private lateinit var binding : FragmentStundenplan24loginBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_stundenplan24login, container, false)

        viewModel = ViewModelProviders.of(this).get(LoginFragmentViewModel::class.java)
        binding.loginViewModel = viewModel
        binding.lifecycleOwner = this

        binding.loginButton.setOnClickListener {
            val schoolnr = binding.loginSchoolnr.text.toString()
            val username = binding.loginUsername.text.toString()
            val password = binding.loginPassword.text.toString()
            val isStudent = true/*binding.radioButtonStudent.isChecked()*/
            viewModel.onLogin(schoolnr, username, password, isStudent)
        }

        viewModel.status.observe(this, Observer { status ->
            when(status) {
                LoginApiStatus.SUCCESS -> Toast.makeText(this.context, "Success", Toast.LENGTH_SHORT).show()
                LoginApiStatus.ERROR_URL -> Toast.makeText(this.context, "Check URL", Toast.LENGTH_SHORT).show()
                LoginApiStatus.ERROR_LOGIN -> Toast.makeText(this.context, "Check Login", Toast.LENGTH_SHORT).show()
            }
        })

        return binding.root
    }
}