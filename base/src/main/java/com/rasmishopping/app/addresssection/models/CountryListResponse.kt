package com.rasmishopping.app.addresssection.models


import com.google.gson.annotations.SerializedName

data class CountryListResponse(
    @SerializedName("countries")
    val countries: List<Country>
) {
    data class Country(
        @SerializedName("country")
        val country: String,
        @SerializedName("states")
        val states: List<String>
    )
}