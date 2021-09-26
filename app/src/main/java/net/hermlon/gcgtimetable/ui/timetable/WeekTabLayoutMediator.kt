package net.hermlon.gcgtimetable.ui.timetable

import android.util.Log
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.*
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.lang.IllegalStateException
import java.lang.ref.WeakReference
import kotlin.math.floor
import kotlin.math.roundToInt

class WeekTabLayoutMediator(private val tabLayout: TabLayout, private val viewPager: ViewPager2, private val tabCount: Int, private val tabConfigurationStrategy: TabLayoutMediator.TabConfigurationStrategy) {
    private var attached = false
    private lateinit var onPageChangeCallback: WeekTabLayoutOnPageChangeCallback
    private lateinit var onTabSelectedListener: WeekViewPagerOnTabSelectedListener

    fun attach() {
        if(attached) {
            throw IllegalStateException("WeekTabLayoutMediator is already attached")
        }
        if(viewPager.adapter == null) {
            throw IllegalStateException("TabLayoutMediator attached before ViewPager2 has an " + "adapter")
        }
        attached = true

        onPageChangeCallback = WeekTabLayoutOnPageChangeCallback(tabLayout)
        viewPager.registerOnPageChangeCallback(onPageChangeCallback)

        onTabSelectedListener = WeekViewPagerOnTabSelectedListener(viewPager, smoothScroll = true)
        tabLayout.addOnTabSelectedListener(onTabSelectedListener)

        populateTabsFromPagerAdapter()

        tabLayout.setScrollPosition(viewPager.currentItem, 0f, true)
    }

    private fun populateTabsFromPagerAdapter() {
        tabLayout.removeAllTabs()

        if(viewPager.adapter != null) {
            for(i in 0 until tabCount) {
                val tab = tabLayout.newTab()
                tabConfigurationStrategy.onConfigureTab(tab, i)
                tabLayout.addTab(tab, false)
            }
            tabLayout.selectTab(tabLayout.getTabAt(viewPager.currentItem % tabLayout.tabCount))
        }
    }
}

private class WeekTabLayoutOnPageChangeCallback(tabLayout: TabLayout) : ViewPager2.OnPageChangeCallback() {

    private var tabLayoutRef: WeakReference<TabLayout> = WeakReference(tabLayout)
    private var previousScrollState = SCROLL_STATE_IDLE
    private var scrollState = SCROLL_STATE_IDLE

    override fun onPageScrollStateChanged(state: Int) {
        previousScrollState = scrollState
        scrollState = state
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        val tabLayout = tabLayoutRef.get()
        if(tabLayout != null) {
            val updateText = scrollState != SCROLL_STATE_SETTLING || previousScrollState == SCROLL_STATE_DRAGGING
            val updateIndicator = !(scrollState == SCROLL_STATE_SETTLING && previousScrollState == SCROLL_STATE_IDLE)
            val targetPosition = position % tabLayout.tabCount
            if(targetPosition == tabLayout.tabCount - 1 && tabLayout.selectedTabPosition == tabLayout.tabCount - 1 && positionOffset != 0f) {
                // scroll out of right end

                /* attempt on something that looks nice, but doesn't work after the finger is released and the animation starts */
                //var realPosition = (1 - positionOffset) * (tabLayout.tabCount - 1)
                //var newPosition = floor(realPosition).toInt()
                //tabLayout.setScrollPosition(newPosition, realPosition%1, false, true)
                tabLayout.setScrollPosition(targetPosition, positionOffset, false, false)
            } else if(targetPosition == tabLayout.tabCount - 1 && tabLayout.selectedTabPosition == 0 && positionOffset != 0f) {
                // scroll out of left end
                tabLayout.setScrollPosition(targetPosition, positionOffset, false, false)
            }
            else {
                tabLayout.setScrollPosition(targetPosition, positionOffset, updateText, updateIndicator)
            }
        }
    }

    override fun onPageSelected(position: Int) {
        val tabLayout = tabLayoutRef.get()
        if(tabLayout != null) {
            val targetPosition = position % tabLayout.tabCount
            if(tabLayout.selectedTabPosition != targetPosition) {
                val updateIndicator =
                    scrollState == SCROLL_STATE_IDLE
                        || (scrollState == SCROLL_STATE_SETTLING
                            && previousScrollState == SCROLL_STATE_IDLE);
                tabLayout.selectTab(tabLayout.getTabAt(targetPosition), updateIndicator)
            }
        }
    }
}

private class WeekViewPagerOnTabSelectedListener(val viewPager: ViewPager2, val smoothScroll: Boolean) : TabLayout.OnTabSelectedListener {
    override fun onTabSelected(tab: TabLayout.Tab?) {
        val targetPosition = viewPager.currentItem - (viewPager.currentItem % tab!!.parent!!.tabCount) + tab.position
        viewPager.setCurrentItem(targetPosition, smoothScroll)
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        // No-op
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
        // No-op
    }
}