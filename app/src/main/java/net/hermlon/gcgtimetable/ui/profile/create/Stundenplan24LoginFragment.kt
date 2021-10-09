package net.hermlon.gcgtimetable.ui.profile.create

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import net.hermlon.gcgtimetable.R
import net.hermlon.gcgtimetable.databinding.FragmentStundenplan24loginBinding
import net.hermlon.gcgtimetable.util.ResourceStatus

@AndroidEntryPoint
class Stundenplan24LoginFragment : Fragment() {

    private val viewModel: LoginFragmentViewModel by viewModels()
    private lateinit var binding : FragmentStundenplan24loginBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_stundenplan24login, container, false)

        binding.loginViewModel = viewModel
        binding.lifecycleOwner = this

        binding.loginButton.setOnClickListener {
            login()
        }

        binding.loginPassword.setOnEditorActionListener { _, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_DONE) {
                login()
                true
            } else {
                false
            }
        }

        viewModel.status.observe(viewLifecycleOwner) {
            if(it != ResourceStatus.LOADING && it != ResourceStatus.SUCCESS) {
                binding.loginError.visibility = View.VISIBLE
                binding.loginError.text = when(it) {
                    ResourceStatus.ERROR_OFFLINE -> getString(R.string.login_error_offline)
                    ResourceStatus.ERROR_AUTH -> getString(R.string.login_error_auth)
                    ResourceStatus.ERROR_NOT_FOUND -> getString(R.string.login_error_not_found)
                    else -> getString(R.string.login_error_unknown)
                }
            } else {
                binding.loginError.visibility = View.INVISIBLE
            }
        }

        viewModel.success.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { success ->
                if(success) {
                    findNavController().navigate(R.id.action_login_s24_success)
                }
            }
        }

        return binding.root
    }

    private fun login() {
        val schoolnr = binding.loginSchoolnr.text.toString()
        val username = binding.loginUsername.text.toString()
        val password = binding.loginPassword.text.toString()
        val isStudent = true/*binding.radioButtonStudent.isChecked()*/
        viewModel.onLogin(schoolnr, username, password, isStudent)
    }

    override fun onStop() {
        super.onStop()
        viewModel.resetNoSourceAvailable()
    }
}