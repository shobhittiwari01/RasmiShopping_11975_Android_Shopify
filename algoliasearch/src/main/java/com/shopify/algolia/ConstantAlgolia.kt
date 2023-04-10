package com.shopify.algolia

import com.algolia.search.model.APIKey
import com.algolia.search.model.ApplicationID
import com.algolia.search.model.IndexName
import com.rasmishopping.app.utils.Urls

object ConstantAlgolia {
    val AppID = ApplicationID(Urls.AppID)
    val ApiKey = APIKey(Urls.ApiKey)
    val IndexName = IndexName(Urls.IndexName)
}