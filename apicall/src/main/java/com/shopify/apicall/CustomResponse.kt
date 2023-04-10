package com.shopify.apicall

import com.google.gson.JsonElement

interface CustomResponse {
    fun onSuccessRetrofit(result: JsonElement) {}
    fun onErrorRetrofit(error: Throwable) {}
}