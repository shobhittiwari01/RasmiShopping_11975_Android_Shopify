package com.rasmishopping.app.productsection.activities

object RatingCountTypeFilter {

    val ratingcountTypeFilter = hashMapOf<String,String>()

    fun asList():ArrayList<String>{
        val list = arrayListOf<String>()
        ratingcountTypeFilter.values.forEach {
            list.add(it)
        }
        return list
    }
}