package net.hermlon.gcgtimetable.util

import android.graphics.Color
import kotlin.random.Random

class CourseColor {
    companion object {
        fun getById(courseId: Long): Int {
            val random = Random(courseId)
            val hue = random.nextFloat() * 360
            val sat = 0.4f + random.nextFloat() * 0.2f
            val value = 0.8f + random.nextFloat() * 0.15f
            return Color.HSVToColor(floatArrayOf(hue, sat, value))
        }

        fun getBySubject(subject: String): Int {
            var seedSum = 0f
            subject.forEach {
                seedSum += (it.code - 48)
            }
            val similarRandom = (seedSum % 30) / 30

            val hue = similarRandom * 360
            val sat = 0.4f + similarRandom * 0.2f
            val value = 0.8f + similarRandom * 0.15f
            return Color.HSVToColor(floatArrayOf(hue, sat, value))
        }
    }
}