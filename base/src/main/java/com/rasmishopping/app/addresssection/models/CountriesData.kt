package com.rasmishopping.app.addresssection.models


import com.google.gson.annotations.SerializedName

data class CountriesData(
    @SerializedName("country")
    val country: List<Country>
) {
    data class Country(
        @SerializedName("name")
        val name: String,
        @SerializedName("id")
        val id: Int,
        @SerializedName("state")
        val state: List<State>
    ) {
        data class State(
            @SerializedName("name")
            val name: String,
            @SerializedName("id")
            val id: Int,
            @SerializedName("city")
            val city: List<City>
        ) {
            data class City(
                @SerializedName("name")
                val name: String,
                @SerializedName("id")
                val id: Int
            )
        }
    }
}