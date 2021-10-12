package net.hermlon.gcgtimetable.ui.filter.courseid

import android.graphics.drawable.GradientDrawable
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
            subject.text = course.subject.replaceFirstChar { it.uppercase() }
            teacher.text = course.teacher
            name.text = course.name
            classname.text = course.className
            (color.drawable as GradientDrawable).setColor(CourseColor.getBySubject(course.subject))
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