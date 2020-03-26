package edu.vt.cs.cs5254.dreamcatcher

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import edu.vt.cs.cs5254.dreamcatcher.DreamListFragment.DreamHolder
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaseDreamCatcherP1Test {

    @get:Rule
    var myActivityRule: ActivityTestRule<DreamActivity> =
        ActivityTestRule(DreamActivity::class.java)

    @Before
    fun reconstructDatabase() =
        DreamRepository.get().reconstructSampleDatabase()

    // ==========================================================
    // Please ensure your application passes these tests
    // before submitting your project
    // ==========================================================

    @Test
    fun appContextGivesCorrectPackageName() {
        val appContext: Context = androidx.test.core.app.ApplicationProvider.getApplicationContext()
        Assert.assertEquals("edu.vt.cs.cs5254.dreamcatcher", appContext.packageName)
    }

    @Test
    fun listViewHasDreamNamedDream0() {
        onView(withText("Dream #0"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun listViewDream1IsNamedDream1() {
        onView(withId(R.id.dream_recycler_view))
            .check(
                matches(
                    atPosition(
                        1,
                        hasDescendant(withText("Dream #1"))
                    )
                )
            )
    }

    @Test
    fun listViewDream0HasNoIcon() {
        onView(withId(R.id.dream_recycler_view))
            .check(
                matches(
                    atPosition(
                        0,
                        hasDescendant(withTagValue(`is`(0)))
                    )
                )
            )
    }

    @Test
    fun listViewDream1HasDeferredIcon() {
        onView(withId(R.id.dream_recycler_view))
            .check(
                matches(
                    atPosition(
                        1,
                        hasDescendant(withTagValue(`is`(R.drawable.dream_deferred_icon)))
                    )
                )
            )
    }

    @Test
    fun detailViewDream2HasComment() {
        onView(withId(R.id.dream_recycler_view))
            .perform(actionOnItemAtPosition<DreamHolder>(2, click()))
        onView(withId(R.id.dream_entry_1_button))
            .check(matches(withText(CoreMatchers.containsString("Dream 2 Entry 1"))))
    }

    @Test
    fun detailViewDream2HasTitleDream2() {
        onView(withId(R.id.dream_recycler_view))
            .perform(actionOnItemAtPosition<DreamHolder>(2, click()))
        onView(withId(R.id.dream_title))
            .check(matches(withText("Dream #2")))
    }

    @Test
    fun detailViewDream2HasDeferredCheckboxSelected() {
        onView(withId(R.id.dream_recycler_view))
            .perform(actionOnItemAtPosition<DreamHolder>(1, click()))
        onView(withId(R.id.dream_deferred))
            .check(matches(isChecked()))
    }

    @Test
    fun base_detailViewDream2ClickRealizedClickBack_ListViewDream2HasNoIcon() {
        onView(withId(R.id.dream_recycler_view))
            .perform(actionOnItemAtPosition<DreamHolder>(2, click()))
        onView(withId(R.id.dream_realized))
            .perform(click())
        Espresso.pressBack()
        onView(withId(R.id.dream_recycler_view))
            .check(matches(atPosition(2, hasDescendant(withTagValue(`is`(0))))))
    }

    @Test
    fun base_detailViewDream0ClickRealizedClickBack_ListViewDream2HasRealizedIcon() {
        onView(withId(R.id.dream_recycler_view))
            .perform(actionOnItemAtPosition<DreamHolder>(0, click()))
        onView(withId(R.id.dream_realized))
            .perform(click())
        Espresso.pressBack()
        onView(withId(R.id.dream_recycler_view))
            .check(matches(
                atPosition(
                    0,
                    hasDescendant(withTagValue(`is`(R.drawable.dream_realized_icon)))
                )
            ))
    }

    companion object {
        private fun atPosition(
            position: Int,
            itemMatcher: Matcher<View?>
        ): Matcher<View?> {
            return object : BoundedMatcher<View?, RecyclerView>(RecyclerView::class.java) {
                override fun describeTo(description: Description) {
                    description.appendText("has item at position $position: ")
                    itemMatcher.describeTo(description)
                }

                override fun matchesSafely(view: RecyclerView): Boolean {
                    val viewHolder = view.findViewHolderForAdapterPosition(position)
                        ?: // has no item on such position
                        return false
                    return itemMatcher.matches(viewHolder.itemView)
                }
            }
        }
    }
}
