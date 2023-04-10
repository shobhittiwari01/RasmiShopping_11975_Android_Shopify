package com.shopify.apicall

import androidx.annotation.NonNull
import com.google.gson.JsonElement

class ApiResponse private constructor(
    val status: Status,
    val data: JsonElement?,
    val error: Throwable?
) {
    companion object {
        fun loading(): ApiResponse {
            return ApiResponse(Status.LOADING, null, null)
        }

        fun success(@NonNull data: JsonElement): ApiResponse {
            return ApiResponse(Status.SUCCESS, data, null)
        }

        fun error(@NonNull error: Throwable): ApiResponse {
            return ApiResponse(Status.ERROR, null, error)
        }
    }
}
