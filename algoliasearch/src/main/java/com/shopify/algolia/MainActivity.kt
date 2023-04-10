package com.shopify.algolia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.core.hits.connectHitsView
import com.algolia.instantsearch.helper.android.searchbox.SearchBoxViewAppCompat
import com.algolia.instantsearch.helper.searchbox.SearchBoxConnector
import com.algolia.instantsearch.helper.searchbox.SearchMode
import com.algolia.instantsearch.helper.searchbox.connectView
import com.algolia.instantsearch.helper.searcher.SearcherSingleIndex
import com.algolia.instantsearch.helper.tracker.HitsTracker
import com.algolia.instantsearch.insights.registerInsights
import com.algolia.instantsearch.insights.sharedInsights
import com.algolia.search.client.ClientSearch
import com.algolia.search.configuration.ConfigurationSearch
import com.algolia.search.helper.deserialize
import com.algolia.search.model.APIKey
import com.algolia.search.model.ApplicationID
import com.algolia.search.model.IndexName
import com.algolia.search.model.insights.EventName
import com.algolia.search.model.insights.UserToken
import com.shopify.algolia.extension.configureRecyclerView
import com.shopify.algolia.extension.configureSearchView
import io.ktor.client.features.logging.*
import kotlinx.android.synthetic.main.alogolia_search.*

class MainActivity : AppCompatActivity() {


    private val client = ClientSearch(
        ConfigurationSearch(
            applicationID =  ConstantAlgolia.AppID,
            apiKey =  ConstantAlgolia.ApiKey,
            logLevel = LogLevel.ALL

        )
    )
    private val stubIndex = client.initIndex( ConstantAlgolia.IndexName)
    private val searcher = SearcherSingleIndex(stubIndex)
    private val searchBox = SearchBoxConnector(searcher, searchMode = SearchMode.AsYouType)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.getStringExtra("app_id")!=null){
            setContentView(R.layout.alogolia_search)
            registerInsights(this,
                ApplicationID(intent.getStringExtra("app_id") as String), APIKey(intent.getStringExtra("apikey") as String), IndexName(intent.getStringExtra("index") as String)
            ).apply {
                loggingEnabled = true
                userToken = UserToken("userToken")
                minBatchSize = 1
            }.also {
                it.loggingEnabled = true
            }
            val hitsTracker = HitsTracker(
                eventName = EventName("demo"),
                searcher = searcher,
                insights = sharedInsights(ConstantAlgolia.IndexName)
            )
            val connection = ConnectionHandler(searchBox, hitsTracker)
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

    }
    
    override fun onDestroy() {
        super.onDestroy()
        searcher.cancel()
       // connection.clear()
    }
}