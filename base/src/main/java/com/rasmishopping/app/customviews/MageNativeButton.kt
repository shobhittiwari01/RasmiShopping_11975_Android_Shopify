package com.rasmishopping.app.customviews

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity

class MageNativeButton : AppCompatButton {

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs)
        setTextSize(attrs)
        setBack(attrs)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
        setTextSize(attrs)
        setBack(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.MageNativeButton)
            val type = a.getString(R.styleable.MageNativeButton_buttontype)
            try {
                if (type != null && type == "white" || type == "round") {
                    var typeface: Typeface? = null
                    typeface = Typeface.createFromAsset(context.assets, "fonts/popnormal.ttf")
                    setTypeface(typeface)
                } else {
                    if (type != null) {
                        var typeface: Typeface? = null
                        if (typeface == null) {
                            typeface =
                                Typeface.createFromAsset(context.assets, "fonts/popnormal.ttf")
//                            typeface = Typeface.createFromAsset(context.assets, "fonts/$type.ttf")
                        }
                        setTypeface(typeface)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

            a.recycle()
        }
    }


    private fun setTextSize(attrs: AttributeSet) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.MageNativeButton)
        val type = a.getString(R.styleable.MageNativeButton_buttontype)
        when (type) {
            "bold" -> textSize = 15f
            "normal" -> textSize = 13f
            "white" -> textSize = 15f
            "filtereset","filtenormal","roundedcorner" -> textSize = 15f
        }
        a.recycle()
    }

    private fun setBack(attrs: AttributeSet) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.MageNativeButton)
        val type = a.getBoolean(R.styleable.MageNativeButton_buttonastext, false)
        val buttontype = a.getString(R.styleable.MageNativeButton_buttontype)
        if (!type) {
            try {
                setBackgroundColor(Color.parseColor(NewBaseActivity.themeColor))
                setTextColor(Color.parseColor(NewBaseActivity.textColor))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            setBackgroundColor(resources.getColor(R.color.white))
        }
        when (buttontype) {
            "filtereset" -> {
                try {
                    setBackgroundColor(Color.parseColor(NewBaseActivity.textColor))
                    setTextColor(Color.parseColor(NewBaseActivity.themeColor))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            "filtenormal" -> {
                try {
                    setBackgroundColor(Color.parseColor(NewBaseActivity.themeColor))
                    setTextColor(Color.parseColor(NewBaseActivity.textColor))
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
            "roundedcorner" -> {
                try {
                    var back = ContextCompat.getDrawable(context!!, R.drawable.roundmagenativebutton) as GradientDrawable
                    back.setColor(Color.parseColor(NewBaseActivity.themeColor))
                    back.setStroke(1,Color.parseColor(NewBaseActivity.themeColor))
                    background=back
                    setTextColor(Color.parseColor(NewBaseActivity.textColor))
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
            "revertroundedcorner" -> {
                try {
                    var back = ContextCompat.getDrawable(context!!, R.drawable.roundmagenativebutton) as GradientDrawable
                    back.setColor(Color.parseColor(NewBaseActivity.textColor))
                    back.setStroke(1,Color.parseColor(NewBaseActivity.themeColor))
                    background=back
                    setTextColor(Color.parseColor(NewBaseActivity.themeColor))
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
        a.recycle()
    }
}
