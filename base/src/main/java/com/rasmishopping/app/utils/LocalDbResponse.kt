package com.rasmishopping.app.utils

import com.rasmishopping.app.dbconnection.entities.AppLocalData
import com.rasmishopping.app.utils.Status.ERROR
import com.rasmishopping.app.utils.Status.SUCCESS
import io.reactivex.annotations.NonNull
import io.reactivex.annotations.Nullable

class LocalDbResponse private constructor(
    val status: Status, @param:Nullable @field:Nullable
    val data: AppLocalData?, @param:Nullable @field:Nullable
    val error: Throwable?
) {
    companion object {

        fun success(@NonNull data: AppLocalData): LocalDbResponse {
            return LocalDbResponse(SUCCESS, data, null)
        }

        fun error(@NonNull error: Throwable): LocalDbResponse {
            return LocalDbResponse(ERROR, null, error)
        }
    }

}
