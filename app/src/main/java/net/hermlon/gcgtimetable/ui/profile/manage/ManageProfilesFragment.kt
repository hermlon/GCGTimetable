package net.hermlon.gcgtimetable.ui.profile.manage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import net.hermlon.gcgtimetable.R
import net.hermlon.gcgtimetable.databinding.FragmentManageProfilesBinding
import net.hermlon.gcgtimetable.databinding.FragmentStundenplan24loginBinding
import net.hermlon.gcgtimetable.ui.profile.create.LoginFragmentViewModel

class ManageProfilesFragment : Fragment() {

    private lateinit var viewModel: ManageProfilesViewModel
    private lateinit var binding : FragmentManageProfilesBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_manage_profiles, container, false)

        viewModel = ViewModelProviders.of(this).get(ManageProfilesViewModel::class.java)
        binding.manageProfilesViewModel = viewModel
        binding.lifecycleOwner = this


        return binding.root
    }
}