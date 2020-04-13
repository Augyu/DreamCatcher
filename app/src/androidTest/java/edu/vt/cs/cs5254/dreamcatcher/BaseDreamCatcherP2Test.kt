package edu.vt.cs.cs5254.dreamcatcher

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.contrib.DrawerMatchers.isOpen
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.*
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaseDreamCatcherPart2Test {

    // See https://developer.android.com/training/testing/espresso/intents#kotlin
    @get:Rule
    var myIntentsRule: IntentsTestRule<DreamActivity> =
        IntentsTestRule(DreamActivity::class.java)

    @Before
    fun clearDatabase() =
        DreamRepository.get().deleteAllDreams()

    // ==========================================================
    // Please ensure your application passes these tests
    // before submitting your project
    // ==========================================================

    @Test
    fun appContextGivesCorrectPackageName() {
        val appContext: Context = androidx.test.core.app.ApplicationProvider.getApplicationContext()
        Assert.assertEquals("edu.vt.cs.cs5254.dreamcatcher", appContext.packageName)
    }

    // ----------------------------------------------------------
    // Basic Functionality
    // ----------------------------------------------------------
    @Test
    fun createDream_CheckTitleRealizedDeferred() {
        // create dream "My Dream" and select realized
        // check title / realized / deferred
        onView(withId(R.id.new_dream)).perform(click())
        onView(withId(R.id.dream_title)).perform(replaceText("My Dream"))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.dream_realized)).perform(click())
        onView(withId(R.id.dream_title)).check(matches(withText("My Dream")))
        onView(withId(R.id.dream_realized)).check(matches(isChecked()))
        onView(withId(R.id.dream_deferred)).check(matches(not(isEnabled())))
    }

    @Test
    fun createDream_CheckEntries() {
        // create dream "My Dream" and select realized
        // check revealed entry and realized entry
        onView(withId(R.id.new_dream)).perform(click())
        onView(withId(R.id.dream_title)).perform(replaceText("My Dream"))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.dream_realized)).perform(click())
        onView(withId(R.id.dream_entry_recycler_view))
            .check(
                matches(
                    atPosition(
                        0, hasDescendant(
                            anyOf(
                                withText(containsString("Revealed")),
                                withText(containsString("revealed")),
                                withText(containsString("REVEALED"))
                            )
                        )
                    )
                )
            )
        onView(withId(R.id.dream_entry_recycler_view))
            .check(
                matches(
                    atPosition(
                        1, hasDescendant(
                            anyOf(
                                withText(containsString("Realized")),
                                withText(containsString("realized")),
                                withText(containsString("REALIZED"))
                            )
                        )
                    )
                )
            )
    }

    @Test
    fun createDream_CheckListView() {
        // create dream "My Dream" and select realized
        // check list view title and realized icon
        onView(withId(R.id.new_dream)).perform(click())
        onView(withId(R.id.dream_title)).perform(replaceText("My Dream"))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.dream_realized)).perform(click())
        Espresso.pressBack()
        onView(withId(R.id.dream_recycler_view))
            .check(
                matches(
                    atPosition(
                        0,
                        hasDescendant(withText("My Dream"))
                    )
                )
            )
        onView(withId(R.id.dream_recycler_view))
            .check(
                matches(
                    atPosition(
                        0,
                        hasDescendant(withTagValue(`is`(R.drawable.dream_realized_icon)))
                    )
                )
            )
    }

    @Test
    fun createAndShareDream_CheckText() {
        // create dream "My Dream" and select realized
        // select share dream
        // check intent for correct action, subject, and text
        onView(withId(R.id.new_dream)).perform(click())
        onView(withId(R.id.dream_title)).perform(replaceText("My Dream"))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.dream_realized)).perform(click())
        intending(not(isInternal()))
            .respondWith(ActivityResult(Activity.RESULT_OK, null))
        onView(withId(R.id.share_dream)).perform(click())
        intended(
            allOf(
                hasAction(Intent.ACTION_CHOOSER),
                hasExtra(
                    `is`(Intent.EXTRA_INTENT),
                    allOf(
                        hasAction(Intent.ACTION_SEND),
                        hasExtra(Intent.EXTRA_SUBJECT, "My Dream"),
                        hasExtra(
                            `is`(Intent.EXTRA_TEXT),
                            containsString("My Dream")
                        ),
                        hasExtra(
                            `is`(Intent.EXTRA_TEXT),
                            anyOf(
                                containsString("Realized"),
                                containsString("realized"),
                                containsString("REALIZED")
                            )
                        )
                    )
                )
            )
        )
    }

    @Test
    fun createDreamAndTakePhoto_CheckImageView() {
        // create dream "My Dream" and select realized
        // select take dream photo
        // check intents for correct action and output-uri
        onView(withId(R.id.new_dream)).perform(click())
        onView(withId(R.id.dream_title)).perform(replaceText("My Dream"))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.dream_realized)).perform(click())
        intending(not(isInternal()))
            .respondWith(ActivityResult(Activity.RESULT_OK, null))
        onView(withId(R.id.take_dream_photo)).perform(click())
        intended(hasAction(MediaStore.ACTION_IMAGE_CAPTURE))
    }
//
//    @Test
//    fun createDreamAndComments_CheckComments() {
//        // create dream "My Dream"
//        // create "Comment 1" and "Comment 2"
//        // select realized
//        // check comments
//        onView(withId(R.id.new_dream)).perform(click())
//        onView(withId(R.id.dream_title)).perform(replaceText("My Dream"))
//        Espresso.closeSoftKeyboard()
//        onView(withId(R.id.add_comment_fab)).perform(click())
//        onView(withId(R.id.comment_text)).perform(replaceText("Comment 1"))
//        onView(withText(android.R.string.ok)).perform(click())
//        onView(withId(R.id.add_comment_fab)).perform(click())
//        onView(withId(R.id.comment_text)).perform(replaceText("Comment 2"))
//        onView(withText(android.R.string.ok)).perform(click())
//        onView(withId(R.id.dream_realized)).perform(click())
//        onView(withId(R.id.dream_entry_recycler_view))
//            .check(
//                matches(
//                    atPosition(
//                        1, hasDescendant(
//                            withText(containsString("Comment 1"))
//                        )
//                    )
//                )
//            )
//        onView(withId(R.id.dream_entry_recycler_view))
//            .check(
//                matches(
//                    atPosition(
//                        2, hasDescendant(
//                            withText(containsString("Comment 2"))
//                        )
//                    )
//                )
//            )
//    }

//    @Test
//    fun createDreamAndCommentsDeleteComment_CheckEntries() {
//        // create dream "My Dream"
//        // create "Comment 1" and "Comment 2"
//        // select realized
//        // swipe to delete comment 2
//        // check entries
//        onView(withId(R.id.new_dream)).perform(click())
//        onView(withId(R.id.dream_title)).perform(replaceText("My Dream"))
//        Espresso.closeSoftKeyboard()
//        onView(withId(R.id.add_comment_fab)).perform(click())
//        onView(withId(R.id.comment_text)).perform(replaceText("Comment 1"))
//        onView(withText(android.R.string.ok)).perform(click())
//        onView(withId(R.id.add_comment_fab)).perform(click())
//        onView(withId(R.id.comment_text)).perform(replaceText("Comment 2"))
//        onView(withText(android.R.string.ok)).perform(click())
//        onView(withId(R.id.dream_realized)).perform(click())
//
//        // swipe left to delete
//        onView(withId(R.id.dream_entry_recycler_view)).perform(
//            actionOnItemAtPosition<RecyclerView.ViewHolder>(
//                2,
//                swipeLeft()
//            )
//        )
//        onView(withId(R.id.dream_entry_recycler_view))
//            .check(
//                matches(
//                    atPosition(
//                        1, hasDescendant(
//                            withText(containsString("Comment 1"))
//                        )
//                    )
//                )
//            )
//        onView(withId(R.id.dream_entry_recycler_view))
//            .check(
//                matches(
//                    atPosition(
//                        2, hasDescendant(
//                            anyOf(
//                                withText(containsString("Realized")),
//                                withText(containsString("realized")),
//                                withText(containsString("REALIZED"))
//                            )
//                        )
//                    )
//                )
//            )
//    }

    @Test
    fun selectHomeIconAndDeferredFilter_CheckDeferredSelected() {
        // select home icon
        // select deferred filter
        // check if deferred filter is selected
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())
        onView(withId(R.id.drawer_layout)).check(matches(isOpen()))
        onView(withId(R.id.nav_view))
            .perform(NavigationViewActions.navigateTo(R.id.nav_deferred_dreams))
        onView(withId(R.id.dream_recycler_view)).check(matches(isDisplayed()))
    }

    @Test
    fun createDreamsRADROpenNavDrawerSelectRealized_CheckRealizedDreamsDisplayed() {
        // create dreams: realized - active - deferred - realized
        // in list view, open nav drawer
        // selected realized filter
        // check that only realized dreams are displayed
        onView(withId(R.id.new_dream)).perform(click())
        onView(withId(R.id.dream_title)).perform(replaceText("Realized 1"))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.dream_realized)).perform(click())
        Espresso.pressBack()
        onView(withId(R.id.new_dream)).perform(click())
        onView(withId(R.id.dream_title)).perform(replaceText("Active 1"))
        Espresso.closeSoftKeyboard()
        Espresso.pressBack()
        onView(withId(R.id.new_dream)).perform(click())
        onView(withId(R.id.dream_title)).perform(replaceText("Deferred 1"))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.dream_deferred)).perform(click())
        Espresso.pressBack()
        onView(withId(R.id.new_dream)).perform(click())
        onView(withId(R.id.dream_title)).perform(replaceText("Realized 2"))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.dream_realized)).perform(click())
        Espresso.pressBack()
        onView(withId(R.id.dream_recycler_view))
            .check(
                matches(
                    atPosition(
                        2,
                        hasDescendant(withText("Deferred 1"))
                    )
                )
            )
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())
        onView(withId(R.id.drawer_layout)).check(matches(isOpen()))
        onView(withId(R.id.nav_view))
            .perform(NavigationViewActions.navigateTo(R.id.nav_realized_dreams))
        onView(withId(R.id.dream_recycler_view))
            .check(
                matches(
                    atPosition(
                        1,
                        hasDescendant(withText("Realized 2"))
                    )
                )
            )
    }

    companion object {
        private fun atPosition(
            @Suppress("SameParameterValue") position: Int,
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
