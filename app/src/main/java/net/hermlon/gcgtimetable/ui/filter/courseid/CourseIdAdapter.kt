package net.hermlon.gcgtimetable.ui.filter.courseid

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import net.hermlon.gcgtimetable.R
import net.hermlon.gcgtimetable.database.FilterClassName
import net.hermlon.gcgtimetable.database.FilterCourse
import net.hermlon.gcgtimetable.domain.TimetableLesson
import net.hermlon.gcgtimetable.util.CourseColor

class CourseIdAdapter constructor(private val onSelectedCallback: (filterCourse: FilterCourse) -> Unit) : ListAdapter<FilterCourse, CourseIdAdapter.CourseIdViewHolder>(FilterCourseIdComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseIdViewHolder {
        return CourseIdViewHolder.create(parent, onSelectedCallback)
    }

    override fun onBindViewHolder(holder: CourseIdViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    class CourseIdViewHolder(itemView: View, private val onSelectedCallback: (filterCourse: FilterCourse) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val subject: TextView = itemView.findViewById(R.id.textview_subject)
        private val teacher: TextView = itemView.findViewById(R.id.textview_teacher)
        private val name: TextView = itemView.findViewById(R.id.textview_name)
        private val classname: TextView = itemView.findViewById(R.id.textview_classname)
        private val color: ImageView = itemView.findViewById(R.id.circle_icon)

        fun bind(course: FilterCourse) {
            itemView.setOnClickListener(null)
            itemView.setOnClickListener {
                it.isSelected = !it.isSelected
                val c = course.copy(blacklisted = !it.isSelected)
                (color.drawable as GradientDrawable).setColor(getColor(c))
                onSelectedCallback(c)
            }
            subject.text = course.subject.replaceFirstChar { it.uppercase() }
            teacher.text = course.teacher
            name.text = course.name
            classname.text = course.className
            itemView.isSelected = !course.blacklisted
            (color.drawable as GradientDrawable).setColor(getColor(course))
        }

        private fun getColor(course: FilterCourse): Int {
            return if(course.blacklisted) {
                Color.parseColor("#11111111")
                //val value = TypedValue()
                //itemView.context.theme.resolveAttribute(R.attr.colorOnPrimary, value, true)
                //value.data
            } else {
                CourseColor.getBySubject(course.subject)
            }
        }

        companion object {
            fun create(parent: ViewGroup, onSelectedCallback: (filterCourse: FilterCourse) -> Unit): CourseIdViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.view_courseid_item, parent, false)
                return CourseIdViewHolder(view, onSelectedCallback)
            }
        }
    }
}

class FilterCourseIdComparator : DiffUtil.ItemCallback<FilterCourse>() {
    override fun areItemsTheSame(oldItem: FilterCourse, newItem: FilterCourse): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FilterCourse, newItem: FilterCourse): Boolean {
        return oldItem == newItem
    }
}