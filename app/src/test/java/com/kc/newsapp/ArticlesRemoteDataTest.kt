package com.kc.newsapp

import android.arch.lifecycle.Observer
import com.kc.newsapp.data.model.Article
import com.kc.newsapp.data.model.Articles
import com.kc.newsapp.data.remote.ArticlesRemoteData
import com.kc.newsapp.data.remote.ArticlesService
import com.kc.newsapp.data.remote.Endpoint
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class ArticlesRemoteDataTest {

    @Mock private lateinit var service: ArticlesService
    @Mock private lateinit var article: Article
    @Mock private lateinit var networkObserver: Observer<Articles>
    @Mock private lateinit var loadingObserver: Observer<Boolean>
    @Mock private lateinit var errorObserver: Observer<Boolean>

    private val emptyResponse = Articles()
    private lateinit var response: Articles
    private lateinit var remoteData: ArticlesRemoteData

    @Before fun init() {
        MockitoAnnotations.initMocks(this)
        remoteData = ArticlesRemoteData(service)
        response = Articles(articles = listOf(article))

        remoteData.network.observeForever(networkObserver)
        remoteData.loading.observeForever(loadingObserver)
        remoteData.error.observeForever(errorObserver)
    }

    @Test fun fetchHeadlinesFrom3Countries() {
        `when`(service.fetchArticles(anyString(), anyString(), anyInt())).thenReturn(response)
        val countries = setOf("ar", "at", "au")
        runBlocking { remoteData.fetchArticles(countries = countries) }
        countries.forEach {
            verify(service, times(1)).fetchArticles(Endpoint.URL, it, 0)
        }
        verify(service, times(countries.size)).fetchArticles(anyString(), anyString(), eq(0))

        val inOrder = inOrder(loadingObserver)
        inOrder.verify(loadingObserver).onChanged(true)
        inOrder.verify(loadingObserver).onChanged(false)

        verify(networkObserver).onChanged(Articles(articles = articleList(countries.size)))
        verify(errorObserver, never()).onChanged(true)
    }

    @Test fun fetchHeadlinesFrom3Countries_noResponse() {
        `when`(service.fetchArticles(anyString(), anyString(), anyInt())).thenReturn(emptyResponse)
        val countries = setOf("ar", "at", "au")

        runBlocking { remoteData.fetchArticles(countries = countries) }
        countries.forEach {
            verify(service, times(1)).fetchArticles(Endpoint.URL, it, 0)
        }
        verify(service, times(countries.size)).fetchArticles(anyString(), anyString(), eq(0))

        val inOrder = inOrder(loadingObserver)
        inOrder.verify(loadingObserver).onChanged(true)
        inOrder.verify(loadingObserver).onChanged(false)

        verify(networkObserver, never()).onChanged(any())
        verify(errorObserver).onChanged(true)
    }

    @Test fun fetchHeadlinesFrom3Countries_partialResponse() {
        val countries = setOf("ar", "at", "au")

        `when`(service.fetchArticles("", countries.elementAt(0), 0)).thenReturn(response)
        `when`(service.fetchArticles("", countries.elementAt(1), 0)).thenReturn(emptyResponse)
        `when`(service.fetchArticles("", countries.elementAt(2), 0)).thenReturn(response)

        runBlocking { remoteData.fetchArticles(countries = countries) }
        countries.forEach {
            verify(service, times(1)).fetchArticles(Endpoint.URL, it, 0)
        }
        verify(service, times(countries.size)).fetchArticles(anyString(), anyString(), eq(0))

        val inOrder = inOrder(loadingObserver)
        inOrder.verify(loadingObserver).onChanged(true)
        inOrder.verify(loadingObserver).onChanged(false)

        verify(networkObserver, never()).onChanged(any())
        verify(errorObserver).onChanged(true)
    }

    private fun articleList(count: Int): List<Article> {
        return (0 until count).flatMap { listOf(article) }
    }
}