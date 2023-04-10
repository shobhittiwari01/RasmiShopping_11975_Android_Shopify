package com.rasmishopping.app.productsection.activities

object ColorTypeFilter {

    val colorTypeFilter = hashMapOf<String,String>()

    fun asList():ArrayList<String>{
        val list = arrayListOf<String>()
        colorTypeFilter.values.forEach {
            list.add(it)
        }
        return list
    }

}