package com.example.algolia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.core.hits.connectHitsView
import com.algolia.instantsearch.helper.android.searchbox.SearchBoxViewAppCompat
import com.algolia.instantsearch.helper.searchbox.SearchBoxConnector
import com.algolia.instantsearch.helper.searchbox.SearchMode
import com.algolia.instantsearch.helper.searchbox.connectView
import com.algolia.instantsearch.helper.searcher.SearcherSingleIndex
import com.algolia.instantsearch.helper.tracker.HitsTracker
import com.algolia.instantsearch.insights.sharedInsights
import com.algolia.search.client.ClientSearch
import com.algolia.search.configuration.ConfigurationSearch
import com.algolia.search.helper.deserialize
import com.algolia.search.model.insights.EventName
import com.example.algolia.extension.configureRecyclerView
import com.example.algolia.extension.configureSearchView
import io.ktor.client.features.logging.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.include_search.*

open class MainActivity : AppCompatActivity() {

     val client = ClientSearch(
        ConfigurationSearch(
            applicationID = App.AppID,
            apiKey = App.ApiKey,
            logLevel = LogLevel.ALL
        )
    )
    val stubIndex = client.initIndex(App.IndexName)
    val searcher = SearcherSingleIndex(stubIndex)
    val searchBox = SearchBoxConnector(searcher, searchMode = SearchMode.AsYouType)

   val hitsTracker = HitsTracker(
        eventName = EventName("demo"),
        searcher = searcher,
        insights = sharedInsights(App.IndexName)
    )
   val connection = ConnectionHandler(searchBox, hitsTracker)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        setSupportActionBar(toolbar)

        val adapter = ListItemAdapter(hitsTracker)
        val searchBoxView = SearchBoxViewAppCompat(searchView)
        connection += searchBox.connectView(searchBoxView)
        connection += searcher.connectHitsView(adapter) { response ->
            response.hits
                .deserialize(ListItem.serializer())
                .mapIndexed { index, listItem -> ItemModel(listItem, index + 1) }
        }

        configureSearchView(searchView, resources.getString(R.string.search_items))
        configureRecyclerView(adapter)

        searcher.searchAsync()
    }
    
  /*  override fun onDestroy() {
        super.onDestroy()
        searcher.cancel()
        connection.clear()
    }*/
}