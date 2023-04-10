package com.rasmishopping.app.customviews

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.google.android.material.textfield.TextInputEditText
import com.rasmishopping.app.R

class MageNativeEditInputText : TextInputEditText {

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs)
        setColor(attrs)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
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
            } catch (e: Exception) {
                e.printStackTrace()
            }

            a.recycle()
        }
    }
    private fun setColor(attrs: AttributeSet){
        val a = context.obtainStyledAttributes(attrs, R.styleable.MageNativeEditText)
        val type = a.getString(R.styleable.MageNativeEditText_edittype)
        when (type) {
            "auto_search" -> setHintTextColor(Color.parseColor("#9E9E9E"))
            "email_pass_color"->setHintTextColor(resources.getColorStateList(R.color.submenutext))
            "email_pass_color"->setTextColor(resources.getColorStateList(R.color.submenutext))
        }
    }
}
