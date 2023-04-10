package com.rasmishopping.app.productsection.models

import java.io.Serializable

data class MediaModel(
    val typeName: String?,
    val previewUrl: String?,
    val mediaUrl: String?
) : Serializable
