package com.rasmishopping.app.utils

import com.google.firebase.database.DataSnapshot
import com.rasmishopping.app.utils.Status.ERROR
import com.rasmishopping.app.utils.Status.SUCCESS
import io.reactivex.annotations.NonNull
import io.reactivex.annotations.Nullable

class FireBaseResponse private constructor(
    val status: Status, @param:Nullable @field:Nullable
    val data: Boolean?, @param:Nullable @field:Nullable
    val error: Throwable?
) {
    companion object {

        fun success(@NonNull data: Boolean?): FireBaseResponse {
            return FireBaseResponse(SUCCESS, data, null)
        }

        fun error(@NonNull error: Throwable): FireBaseResponse {
            return FireBaseResponse(ERROR, null, error)
        }
    }
}
