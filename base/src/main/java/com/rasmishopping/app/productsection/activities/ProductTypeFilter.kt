package com.rasmishopping.app.productsection.activities

object ProductTypeFilter {

    val productTypeFilter = hashMapOf<String,String>()

    fun asList():ArrayList<String>{
        val list = arrayListOf<String>()
        productTypeFilter.values.forEach {
            list.add(it)
        }
        return list
    }
}