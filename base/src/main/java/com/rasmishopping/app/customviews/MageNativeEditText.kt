package com.rasmishopping.app.customviews

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.rasmishopping.app.R

class MageNativeEditText : AppCompatEditText {

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs)
        setSize(attrs)
        setColor(attrs)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
        setSize(attrs)
        setColor(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.MageNativeEditText)
            val type = a.getString(R.styleable.MageNativeEditText_edittype)
            try {
                if (type != null && type == "white") {
                    var typeface: Typeface? = null
                    typeface = Typeface.createFromAsset(context.assets, "fonts/popnormal.ttf")
                    setTypeface(typeface)
                } else {
                    if (type != null) {
//                        val typeface = Typeface.createFromAsset(context.assets, "fonts/$type.ttf")
                        val typeface =
                            Typeface.createFromAsset(context.assets, "fonts/popnormal.ttf")
                        setTypeface(typeface)
                    }
                }
                when (type) {
                    "auto_search" -> {
                        val typeface =
                            Typeface.createFromAsset(context.assets, "fonts/poplight.ttf")
                        setTypeface(typeface)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            a.recycle()
        }
    }


    private fun setSize(attrs: AttributeSet) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.MageNativeEditText)
        val type = a.getString(R.styleable.MageNativeEditText_edittype)
        when (type) {
            "bold" -> textSize = 15f
            "normal" -> textSize = 13f
            "email_pass_color", "white" -> textSize = 15f
        }
    }
   private fun setColor(attrs: AttributeSet){
       val a = context.obtainStyledAttributes(attrs, R.styleable.MageNativeEditText)
       val type = a.getString(R.styleable.MageNativeEditText_edittype)
       when (type) {
           "auto_search" -> setHintTextColor(Color.parseColor("#9E9E9E"))

       }
    }
}
