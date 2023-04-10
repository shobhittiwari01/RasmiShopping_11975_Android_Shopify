package com.rasmishopping.app.utils

import com.google.gson.JsonElement
import com.rasmishopping.app.utils.Status.*
import io.reactivex.annotations.NonNull

class ApiResponse private constructor(
    val status: Status,
    val data: JsonElement?,
    val error: Throwable?
) {
    companion object {
        fun loading(): ApiResponse {
            return ApiResponse(LOADING, null, null)
        }

        fun success(@NonNull data: JsonElement): ApiResponse {
            return ApiResponse(SUCCESS, data, null)
        }

        fun error(@NonNull error: Throwable): ApiResponse {
            return ApiResponse(ERROR, null, error)
        }
    }
}
