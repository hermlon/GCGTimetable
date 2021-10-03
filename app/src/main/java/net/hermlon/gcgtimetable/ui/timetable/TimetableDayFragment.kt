package net.hermlon.gcgtimetable.ui.timetable

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import dagger.hilt.android.AndroidEntryPoint
import net.hermlon.gcgtimetable.domain.TimetableDay
import net.hermlon.gcgtimetable.domain.TimetableLesson
import net.hermlon.gcgtimetable.ui.theme.TimetableTheme
import net.hermlon.gcgtimetable.ui.timetable.TimetableDayAdapter.Companion.ARG_DATE
import net.hermlon.gcgtimetable.util.Resource
import net.hermlon.gcgtimetable.util.ResourceStatus
import kotlin.random.Random

@AndroidEntryPoint
class TimetableDayFragment : Fragment() {

    private lateinit var viewModel: TimetableDayViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentId = requireArguments().getSerializable(ARG_DATE).toString()
        viewModel = ViewModelProvider(this).get(fragmentId, TimetableDayViewModel::class.java)
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner))
            setContent {
                TimetableTheme {
                    Surface(
                        color = MaterialTheme.colors.background
                    ) {
                        val isRefreshing by viewModel.isLoading.observeAsState(false)
                        SwipeRefresh(
                            modifier = Modifier.fillMaxSize(),
                            state = rememberSwipeRefreshState(isRefreshing),
                            onRefresh = { viewModel.refresh() }
                        ) {
                            val timetable: Resource<TimetableDay> by viewModel.timetable.observeAsState(Resource(ResourceStatus.LOADING))
                            LessonList(timetable.data?.lessons, timetable.data?.isStandard, timetable.status)
                        }
                    }
                }
            }
        }
    }
}

@Preview("Light Mode")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
@Composable
fun Preview() {
    val lessons = listOf(
        TimetableLesson(1, "Mat", false, "Kra", false, "304", false, null, 3),
        TimetableLesson(2, "Deu", false, "Gel", false, "108V", false, null, 5),
        TimetableLesson(3, "Geo", false, "Bro", true, "108V", false, "Geo bei Herr Brode statt Frau Lange", 5),
    )
    TimetableTheme {
        Surface(color = MaterialTheme.colors.background) {
            LessonList(lessons)
        }
    }
}

@Composable
fun LessonList(lessons: List<TimetableLesson>?, standard: Boolean? = false, status: ResourceStatus = ResourceStatus.SUCCESS) {
    var lessonCache: List<TimetableLesson>? by remember { mutableStateOf(null) }
    var standardCache: Boolean? by remember { mutableStateOf(null) }

    /*LazyColumn() {
        itemsIndexed((1..20).toList()) { index, item ->
            Text("$item")
        }
    }*/
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        item {
            Text(status.toString())
        }
        if(lessons != null && standard != null) {
            lessonCache = lessons
            standardCache = standard
        }
        if(lessonCache != null && standardCache != null) {
            itemsIndexed(lessonCache!!) { index, lesson ->
                if(index < 10) {
                    LessonListItem(lesson, standardCache!!)
                    if(index != lessonCache!!.size - 1) {
                        Divider()
                    }
                }
            }
        } else {
            item {
                Text(status.toString())
            }
        }
    }
}

@Composable
fun LessonListItem(lesson: TimetableLesson, standard: Boolean = false) {
    val MinHeight = 56.dp
    val IconLeftPadding = 16.dp
    val IconVerticalPadding = 8.dp
    val ContentLeftPadding = 16.dp
    val ContentRightPadding = 16.dp
    val MainRightPadding = 4.dp
    val TrailingRightPadding = 16.dp

    Row(
        Modifier.heightIn(min = MinHeight)
    ) {
        Box(
            Modifier
                .align(Alignment.CenterVertically)
                .padding(
                    start = IconLeftPadding,
                    top = IconVerticalPadding,
                    bottom = IconVerticalPadding
                ),
            contentAlignment = Alignment.CenterStart
        ) {
            LessonIcon(color = getColorByCourseId(lesson.courseId, MaterialTheme.colors.primary, MaterialTheme.colors.secondary))
        }

        Column(
            Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
                .padding(start = ContentLeftPadding, end = ContentRightPadding)
        ) {
            Text(
                text = lesson.teacher.uppercase(),
                style = MaterialTheme.typography.overline
            )
            Row() {
                Text(
                    modifier = Modifier.padding(end = MainRightPadding),
                    text = lesson.subject,
                    style = MaterialTheme.typography.subtitle1
                )
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        modifier = Modifier.align(Alignment.Bottom),
                        text = "in Raum " + lesson.room,
                        style = MaterialTheme.typography.body2
                    )
                }
            }
        }
        Box(
            Modifier
                .align(Alignment.CenterVertically)
                .padding(end = TrailingRightPadding)
        ) {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    text = lesson.number.toString(),
                    style = MaterialTheme.typography.caption
                )
            }
        }
    }
}

@Composable
fun LessonIcon(color: Color, standard: Boolean = false) {
    Box(modifier = Modifier
        .size(24.dp)
        .clip(shape = CircleShape)
        .background(color)
    )
}

fun getColorByCourseId(courseId: Long, baseColor1: Color, baseColor2: Color): Color {
    val random = Random(courseId).nextFloat()
    return Color(ColorUtils.blendARGB(baseColor1.toArgb(), baseColor2.toArgb(), random))
}