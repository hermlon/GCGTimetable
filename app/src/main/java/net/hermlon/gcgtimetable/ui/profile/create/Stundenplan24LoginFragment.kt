package net.hermlon.gcgtimetable.ui.profile.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import net.hermlon.gcgtimetable.R
import net.hermlon.gcgtimetable.databinding.FragmentAskhostingBinding
import net.hermlon.gcgtimetable.databinding.FragmentStundenplan24loginBinding

class Stundenplan24LoginFragment : Fragment() {

    private lateinit var binding : FragmentStundenplan24loginBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_stundenplan24login, container, false)

        return binding.root
    }
}