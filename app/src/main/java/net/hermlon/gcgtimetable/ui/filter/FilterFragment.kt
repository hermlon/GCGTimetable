package net.hermlon.gcgtimetable.ui.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import net.hermlon.gcgtimetable.R

class FilterFragment : Fragment() {

    private val viewModel: FilterFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_filter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()
        val toolbar: MaterialToolbar = view.findViewById(R.id.toolbar)
        toolbar.setupWithNavController(navController)
        toolbar.setOnMenuItemClickListener {
            it.onNavDestinationSelected(navController)
        }
    }
}