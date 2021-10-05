package net.hermlon.gcgtimetable.ui.timetable

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.hermlon.gcgtimetable.R
import net.hermlon.gcgtimetable.domain.TimetableLesson

class LessonListAdapter : ListAdapter<TimetableLesson, LessonListAdapter.LessonViewHolder>(LessonsComparator()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
        return LessonViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    class LessonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val subject: TextView = itemView.findViewById(R.id.textview_subject)
        private val teacher: TextView = itemView.findViewById(R.id.textview_teacher)
        private val room: TextView = itemView.findViewById(R.id.textview_room)
        private val number: TextView = itemView.findViewById(R.id.textview_number)
        private val info: TextView = itemView.findViewById(R.id.textview_info)

        fun bind(lesson: TimetableLesson) {
            subject.text = lesson.subject
            teacher.text = lesson.teacher
            room.text = lesson.room
            number.text = lesson.number.toString()
            info.text = lesson.information
        }

        companion object {
            fun create(parent: ViewGroup): LessonViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.lesson_item, parent, false)
                return LessonViewHolder(view)
            }
        }
    }

    class LessonsComparator : DiffUtil.ItemCallback<TimetableLesson>() {
        override fun areItemsTheSame(oldItem: TimetableLesson, newItem: TimetableLesson): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(
            oldItem: TimetableLesson,
            newItem: TimetableLesson
        ): Boolean {
            return oldItem == newItem
        }
    }
}