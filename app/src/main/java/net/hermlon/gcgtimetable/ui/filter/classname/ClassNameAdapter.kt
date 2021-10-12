package net.hermlon.gcgtimetable.ui.filter.classname

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.hermlon.gcgtimetable.R
import net.hermlon.gcgtimetable.database.FilterClassName

class ClassNameAdapter constructor(private val onSelectedCallback: (filterClassName: FilterClassName) -> Unit) : ListAdapter<FilterClassName, ClassNameAdapter.ClassNameViewHolder>(FilterClassNameComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassNameViewHolder {
        return ClassNameViewHolder.create(parent, onSelectedCallback)
    }

    override fun onBindViewHolder(holder: ClassNameViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    class ClassNameViewHolder(itemView: View, private val onSelectedCallback: (filterClassName: FilterClassName) -> Unit) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox_classname)

        fun bind(className: FilterClassName) {
            checkBox.setOnClickListener(null)
            checkBox.setOnClickListener {
                onSelectedCallback(FilterClassName(checkBox.text.toString(), checkBox.isChecked))
            }
            checkBox.text = className.className
            checkBox.isChecked = className.whitelisted
        }

        companion object {
            fun create(parent: ViewGroup, onSelectedCallback: (filterClassName: FilterClassName) -> Unit): ClassNameViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.view_classname_item, parent, false)
                return ClassNameViewHolder(view, onSelectedCallback)
            }
        }
    }
}

class FilterClassNameComparator : DiffUtil.ItemCallback<FilterClassName>() {
    override fun areItemsTheSame(oldItem: FilterClassName, newItem: FilterClassName): Boolean {
        return oldItem.className == newItem.className
    }

    override fun areContentsTheSame(oldItem: FilterClassName, newItem: FilterClassName): Boolean {
        return oldItem == newItem
    }
}