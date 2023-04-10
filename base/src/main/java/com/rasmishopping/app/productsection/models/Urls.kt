package com.rasmishopping.app.productsection.models

import java.io.Serializable

data class Urls(
    val compact: String,
    val huge: String,
    val original: String,
    val small: String
): Serializable