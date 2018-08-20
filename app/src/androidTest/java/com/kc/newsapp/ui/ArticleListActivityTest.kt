package com.kc.newsapp.ui

import android.content.Intent
import android.preference.PreferenceManager
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.*
import android.support.test.espresso.Espresso.*
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import android.support.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import android.support.test.espresso.intent.Intents
import android.support.test.espresso.intent.Intents.intended
import android.support.test.espresso.intent.Intents.times
import android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent
import android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE
import android.support.test.espresso.matcher.ViewMatchers.Visibility.GONE
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.v7.widget.RecyclerView
import android.view.View
import com.google.gson.Gson
import com.kc.newsapp.App
import com.kc.newsapp.R
import com.kc.newsapp.data.model.Articles
import com.kc.newsapp.data.remote.Endpoint
import com.kc.newsapp.di.AppModule
import com.kc.newsapp.di.DaggerTestAppComponent
import com.kc.newsapp.testing.TestUtil
import com.kc.newsapp.viewmodel.ListViewModel
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit


@RunWith(AndroidJUnit4::class)
class ArticleListActivityTest {

    private lateinit var server: MockWebServer

    //@Mock private lateinit var sharedPrefs: SharedPreferences

    @Rule
    @JvmField var activityTestRule = ActivityTestRule(ArticleListActivity::class.java, false, false)
    private var mIdlingResource: IdlingResource? = null

    @Before fun setUp() {
        Intents.init()

        server = MockWebServer()
        server.start()
        Endpoint.URL = server.url("/").toString()

        DaggerTestAppComponent.builder()
                .appModule(AppModule(getApplication()))
                .build().let { getApplication().setComponent(it) }
    }

    @After fun tearDown() {
        Intents.release()
        if (mIdlingResource != null) {
            IdlingRegistry.getInstance().unregister(mIdlingResource)
            mIdlingResource = null
        }

        PreferenceManager.getDefaultSharedPreferences(getApplication()).edit().apply {
            remove(ListViewModel.KEY_COUNTRIES)
            remove(ListViewModel.KEY_BOOKMARKS)
            remove(ListViewModel.KEY_BOOKMARKS_JSON)
        }.apply()
    }

    @Test fun startActivity() {
        val countries = setOf("jp", "tw", "us")
        preloadCountriesOfInterest(countries)

        var jsonResponse: String? = null
        countries.forEach {
            jsonResponse = TestUtil.getStringFromFile(getApplication(), "200_$it.json")
            server.enqueue(MockResponse().setResponseCode(200).setBody(jsonResponse).setBodyDelay(1, TimeUnit.SECONDS))
        }

        mIdlingResource = launchActivity(activityTestRule)

        verifyScreen(successful = true)
        val objResponse = Gson().fromJson(jsonResponse, Articles::class.java)
        verifyNewsListSize(objResponse.articles.size * countries.size)
    }

    @Test fun selectCountriesOfInterest() {
        mIdlingResource = launchActivity(activityTestRule)

        // open option menu
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext())
        onView(withText(R.string.title_select_countries)).perform(click())

        val names = InstrumentationRegistry.getTargetContext().resources.getStringArray(R.array.country_name)
        val codes = InstrumentationRegistry.getTargetContext().resources.getStringArray(R.array.country_code)

        var jsonResponse: String? = null
        // click on the first 3 countries
        val numberOfCountries = 3
        (0 until numberOfCountries).forEach {
            onView(withText(names[it])).perform(click())
            // populate response for later query
            jsonResponse = TestUtil.getStringFromFile(getApplication(), "200_${codes[it]}.json")
            server.enqueue(MockResponse().setResponseCode(200).setBody(jsonResponse).setBodyDelay(1, TimeUnit.SECONDS))
        }

        // click on OK botton
        onView(withId(android.R.id.button1)).perform(click())

        verifyScreen(successful = true)
        val objResponse = Gson().fromJson(jsonResponse, Articles::class.java)
        verifyNewsListSize(objResponse.articles.size * numberOfCountries)
    }

    @Test fun switchTab() {
        mIdlingResource = launchActivity(activityTestRule)
        verifyScreen(false, R.string.prompt_select_countries)

        onView(withId(R.id.action_favorites)).perform(click())
        verifyScreen(false, R.string.prompt_empty_bookmark)
    }

    @Test fun addArticlesToBookmarkAndSwitchTab() {
        val countries = setOf("us", "ca")
        preloadCountriesOfInterest(countries)

        var jsonResponse: String?
        countries.forEach {
            jsonResponse = TestUtil.getStringFromFile(getApplication(), "200_$it.json")
            server.enqueue(MockResponse().setResponseCode(200).setBody(jsonResponse).setBodyDelay(1, TimeUnit.SECONDS))
        }

        mIdlingResource = launchActivity(activityTestRule)
        verifyScreen(true)

        // add articles to bookmark
        val positionToClick = setOf(5, 10, 15)
        positionToClick.forEach {
            onView(withId(R.id.list)).perform(scrollToPosition<ArticlesAdapter.ArticleVH>(it))
            onView(withId(R.id.list)).perform(actionOnItemAtPosition<ArticlesAdapter.ArticleVH>(it, clickChildViewWithId(R.id.bookmark)))
        }

        // switch tab
        onView(withId(R.id.action_favorites)).perform(click())

        // 3 bookmarks added
        verifyNewsListSize(positionToClick.size)
        verifyScreen(true)
    }

//    @Test fun startEmptyPullToRefresh() { }

    @Test fun openNews() {
        val countries = setOf("ca")
        preloadCountriesOfInterest(countries)

        var jsonResponse: String? = null
        countries.forEach {
            jsonResponse = TestUtil.getStringFromFile(getApplication(), "200_$it.json")
            server.enqueue(MockResponse().setResponseCode(200).setBody(jsonResponse).setBodyDelay(1, TimeUnit.SECONDS))
        }

        mIdlingResource = launchActivity(activityTestRule)

        val positionToClick = setOf(5, 10, 15)
        val objResponse = Gson().fromJson(jsonResponse, Articles::class.java)
        positionToClick.forEach {
            onView(withId(R.id.list)).perform(scrollToPosition<ArticlesAdapter.ArticleVH>(it))
            onView(withId(R.id.list)).perform(actionOnItemAtPosition<ArticlesAdapter.ArticleVH>(it, click()))
            intended(allOf(
                    hasComponent(WebViewActivity::class.java.name),
                    hasExtra(WebViewActivity.KEY_URL, objResponse.articles[it].url)
            ))
            Espresso.pressBack()
        }
        intended(hasComponent(WebViewActivity::class.java.name), times(positionToClick.size))
    }

    @Test fun retainListScrolling() { }

    private fun launchActivity(rule: ActivityTestRule<ArticleListActivity>): IdlingResource? {
        rule.launchActivity(Intent())
        var idlingResource = rule.activity.countingIdlingResource
        IdlingRegistry.getInstance().register(idlingResource)
        return idlingResource
    }

    private fun preloadCountriesOfInterest(countries: Set<String>) {
        PreferenceManager.getDefaultSharedPreferences(getApplication()).edit()
                .putStringSet(ListViewModel.KEY_COUNTRIES, countries)
                .apply()
    }

    private fun verifyScreen(successful: Boolean, errorMsgId: Int = 0) {
        onView(withId(R.id.list)).check(matches(withEffectiveVisibility(if (successful) VISIBLE else GONE)))
        onView(withId(R.id.errorView)).check(matches(withEffectiveVisibility(if (successful) GONE else VISIBLE)))
        if (!successful)
            onView(withId(R.id.errorView)).check(matches(withText(errorMsgId)))
    }

    private fun verifyNewsListSize(size: Int) {
        onView(withId(R.id.list)).check(recyclerViewItemCountAssertion(size))
    }


    private fun getApplication() = InstrumentationRegistry.getTargetContext().applicationContext as App

    private fun clickChildViewWithId(id: Int): ViewAction {
        return object : ViewAction {
            override fun perform(uiController: UiController?, view: View?) {
                view?.findViewById<View>(id)?.performClick()
            }

            override fun getConstraints(): Matcher<View>? {
                return null
            }

            override fun getDescription(): String {
                return "Click on a child view with specified id."
            }
        }
    }

    private fun recyclerViewItemCountAssertion(expectedCount: Int): ViewAssertion {
        return object : ViewAssertion {
            override fun check(view: View?, noViewFoundException: NoMatchingViewException?) {
                if (noViewFoundException != null) {
                    throw noViewFoundException
                }

                assertThat((view as RecyclerView).adapter.itemCount, `is`(expectedCount))
            }
        }
    }

}