package com.rasmishopping.app.productsection.models

import java.io.Serializable

data class Picture(
    val hidden: Boolean,
    val urls: Urls
): Serializable