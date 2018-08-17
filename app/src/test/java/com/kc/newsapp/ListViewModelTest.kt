package com.kc.newsapp

import android.arch.lifecycle.Observer
import android.content.SharedPreferences
import com.kc.newsapp.data.ArticlesRepository
import com.kc.newsapp.data.Listing
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

    @Captor private lateinit var dataCallbackCaptor: ArgumentCaptor<Set<String>>
    @Captor private lateinit var booleanCallbackCaptor: ArgumentCaptor<Boolean>

    @Mock private lateinit var repository: ArticlesRepository
    @Mock private lateinit var sharedPreferences: SharedPreferences
    @Mock private lateinit var dataObserver: Observer<String>
    @Mock private lateinit var countriesObserver: Observer<Set<String>>
    @Mock private lateinit var sharedPreferencesObserver: Observer<Set<String>>
    @Mock private lateinit var resultObserver: Observer<Listing<Articles>>

    @Before fun init() {
        MockitoAnnotations.initMocks(this)
        viewModel = ListViewModel(sharedPreferences, repository)
    }

    @Test fun preloadCountriesOfInterest() {
        val countries = setOf("be", "br", "bg")

        `when`(sharedPreferences.getStringSet(KEY_COUNTRIES, setOf())).thenReturn(countries)

        //viewModel.sharedPreferenceLiveData.observeForever(sharedPreferencesObserver)
        viewModel.countryOfInterest.observeForever(countriesObserver)
        //viewModel.fetchedOutcome.observeForever(resultObserver)

        //viewModel.fetchArticles(true)
        //verify(sharedPreferencesObserver).onChanged(countries)
        verify(countriesObserver).onChanged(countries)
        //verify(resultObserver).onChanged(any())


//        verify(repository).fetchArticles(capture(booleanCallbackCaptor), capture(dataCallbackCaptor))
//        Assert.assertEquals(booleanCallbackCaptor.value, true)
//        Assert.assertEquals(dataCallbackCaptor.value.size, 10)
    }
}