package com.rasmishopping.app.productsection.activities

object SizeTypeFilter {

    val sizeTypeFilter = hashMapOf<String,String>()

    fun asList():ArrayList<String>{
        val list = arrayListOf<String>()
        sizeTypeFilter.values.forEach {
            list.add(it)
        }
        return list
    }
}