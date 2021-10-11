package net.hermlon.gcgtimetable.ui.filter.classname

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import net.hermlon.gcgtimetable.R
import net.hermlon.gcgtimetable.ui.filter.FilterFragmentViewModel

@AndroidEntryPoint
class ClassNameFilterFragment : Fragment() {

    private val viewModel: FilterFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_class_name_filter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = view as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = ClassNameAdapter {
            viewModel.onClickClassName(it)
        }
        recyclerView.adapter = adapter

        viewModel.classNames.observe(viewLifecycleOwner) {
            adapter.submitList(it!!)
        }
    }
}