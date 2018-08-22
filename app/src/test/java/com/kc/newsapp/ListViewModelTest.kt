package com.kc.newsapp

import android.arch.lifecycle.Observer
import android.content.SharedPreferences
import com.kc.newsapp.data.ArticlesRepository
import com.kc.newsapp.data.model.Article
import com.kc.newsapp.data.model.Articles
import com.kc.newsapp.viewmodel.ListViewModel
import com.kc.newsapp.viewmodel.ListViewModel.Companion.KEY_COUNTRIES
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.*
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class ListViewModelTest {

    private lateinit var viewModel: ListViewModel

    @Mock private lateinit var repository: ArticlesRepository
    @Mock private lateinit var sharedPreferences: SharedPreferences
    @Mock private lateinit var article: Article
    private lateinit var articles: Articles

    @Mock private lateinit var countriesObserver: Observer<Set<String>>

    @Mock private lateinit var promptObserver: Observer<String>

    @Before fun init() {
        MockitoAnnotations.initMocks(this)
        viewModel = ListViewModel(sharedPreferences, repository)
        articles = Articles(articles = listOf(article, article, article))
    }

    @Test fun selectCountriesOfInterest() {
        val countries = setOf("be", "br", "bg")
        `when`(sharedPreferences.getStringSet(KEY_COUNTRIES, setOf())).thenReturn(countries)

        viewModel.countryOfInterest.observeForever(countriesObserver)
        verify(countriesObserver).onChanged(countries)
    }

    @Test fun addToBookmark() {
        val msg = "Sample Message"
        viewModel.promptMessage.observeForever(promptObserver)
        viewModel.showPrompt(msg)
        verify(promptObserver).onChanged(msg)
    }

    @Test fun emptyCountriesOfInterest_emptyList_rotate() {
        viewModel.combinedList().value = null
        viewModel.fetchArticles()
        verify(repository, times(0)).fetchArticles(false, emptySet())
    }

    @Test fun presetCountriesOfInterest_withLoadedList_rotate() {
        val presetCountries = setOf("ar")
        viewModel.countryOfInterest.value = presetCountries

        viewModel.combinedList().value = articles
        viewModel.fetchArticles()
        verify(repository, times(0)).fetchArticles(false, presetCountries)
    }

    @Test fun presetCountriesOfInterest_withLoadedList_pullToRefresh() {
        val presetCountries = setOf("ar")
        viewModel.countryOfInterest.value = presetCountries

        viewModel.combinedList().value = articles
        viewModel.fetchArticles(true)
        verify(repository, times(1)).fetchArticles(true, presetCountries)
    }
}