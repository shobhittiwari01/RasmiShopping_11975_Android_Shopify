package com.rasmishopping.app.utils

import android.util.Log
import com.rasmishopping.app.sharedprefsection.MagePrefs
import java.lang.Double
import java.text.NumberFormat
import java.util.*
import kotlin.String

object CurrencyFormatter {
    fun setsymbol(data: String, currency_symbol: String): String {
        Log.i("MageNative", "Amount : $data")
        val format = NumberFormat.getCurrencyInstance(Locale.ENGLISH)
        format.currency = Currency.getInstance(currency_symbol)
        MagePrefs.setSymbol( Currency.getInstance(currency_symbol).symbol)
        return format.format(Double.valueOf(data))
    }
}
