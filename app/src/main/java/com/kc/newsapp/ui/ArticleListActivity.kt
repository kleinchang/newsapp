package com.kc.newsapp.ui

import android.arch.lifecycle.ViewModelProviders
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.VisibleForTesting
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.iid.FirebaseInstanceId
import com.kc.newsapp.data.ArticlesRepository
import com.kc.newsapp.data.local.ArticlesLocalData
import com.kc.newsapp.data.remote.ArticlesRemoteData
import com.kc.newsapp.data.remote.ArticlesService
import com.kc.newsapp.viewmodel.ListViewModel
import com.kc.newsapp.viewmodel.ListViewModelFactory
import kotlinx.android.synthetic.main.activity_article_list.*
import kotlinx.android.synthetic.main.activity_main.*
import com.kc.newsapp.*
import com.kc.newsapp.testing.EspressoIdlingResource
import com.kc.newsapp.util.Util
import javax.inject.Inject


class ArticleListActivity : AppCompatActivity() {

    private lateinit var rvAdapter: ArticlesAdapter
    private lateinit var viewModel: ListViewModel
    @Inject lateinit var sharedPreferences: SharedPreferences

    companion object {
        const val CURRENT_TAB = "tab"
    }

    private var currentTab = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        App.appComponent.inject(this)

        currentTab = savedInstanceState?.getBoolean(CURRENT_TAB, true) ?: true
        if (currentTab)
            addFragment(ArticleListFragment.TAG)
        else
            addFragment(BookmarkFragment.TAG)

        bottom_navigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_favorites -> {
                    currentTab = addFragment(BookmarkFragment.TAG)
                    true
                }
                R.id.action_live_news -> {
                    currentTab = addFragment(ArticleListFragment.TAG)
                    true
                }
                else -> {
                    throw Exception("Artificial")
                    true
                }
            }
        }

        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            Util.log("ArticleListActivity token: ${it.token}")
        }

        viewModel = getViewModel()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putBoolean(CURRENT_TAB, currentTab)
    }

    private fun addFragment(tag: String): Boolean {
        val fragment = supportFragmentManager.findFragmentByTag(tag)
        if (fragment == null) {
            supportFragmentManager.beginTransaction().
                    replace(R.id.content, if (tag == ArticleListFragment.TAG) ArticleListFragment() else BookmarkFragment(), tag)
                    .addToBackStack(null).commit()
        } else {
            supportFragmentManager.beginTransaction().replace(R.id.content, fragment, tag)
                    .addToBackStack(null).commit()
        }
        return tag == ArticleListFragment.TAG
    }

    private fun getViewModel(): ListViewModel {
        val repo = ArticlesRepository(ArticlesLocalData(applicationContext), ArticlesRemoteData(ArticlesService()))
        return ViewModelProviders.of(this, ListViewModelFactory(sharedPreferences, repo))[ListViewModel::class.java]
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.actions, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun openDialog(viewModel: ListViewModel) {

        AlertDialog.Builder(this@ArticleListActivity).apply {

            val countryCode = resources.getStringArray(R.array.country_code)
            val checked = BooleanArray(countryCode.size, { i -> viewModel.countryOfInterest.value?.contains(countryCode[i]) ?: false })
            val set = mutableSetOf<String>()
            viewModel.countryOfInterest.value?.forEach { set.add(it) }

            val countryName = resources.getStringArray(R.array.country_name)
            setTitle(R.string.title_select_countries).setMultiChoiceItems(countryName, checked) {
                dialog, which, isChecked ->
                if (isChecked)
                    set.add(countryCode[which])
                else
                    set.remove(countryCode[which])
            }.setPositiveButton(R.string.ok) {
                dialog, which ->
                viewModel.updateCountries(set)
            }.setNegativeButton(R.string.cancel) {
                dialog, which ->
            }.create().show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            openDialog(viewModel)
            true
        }
//        R.id.action_favorite -> {
//            openDialog(viewModel)
//            true
//        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    val countingIdlingResource @VisibleForTesting get() = EspressoIdlingResource.countingIdlingResource
}