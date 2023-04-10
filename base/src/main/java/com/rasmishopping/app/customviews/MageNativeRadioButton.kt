package com.rasmishopping.app.customviews

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatRadioButton
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import java.util.*

class MageNativeRadioButton : AppCompatRadioButton {

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs)
        setColor(attrs)

        setTextSize(attrs)

    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
        setColor(attrs)
        setTextSize(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.MageNativeRadioButton)
            val type = a.getString(R.styleable.MageNativeRadioButton_radiotype)
            try {
                if (type != null) {
                    var typeface: Typeface? = null
                    if (typeface == null) {
//                        typeface = Typeface.createFromAsset(context.assets, "fonts/$type.ttf")
                        typeface =
                            Typeface.createFromAsset(context.assets, "fonts/popnormal.ttf")
                    }
                    setTypeface(typeface)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            a.recycle()
        }
    }
    fun setColor(attrs: AttributeSet?) {
        try
        {
            val a = context.obtainStyledAttributes(attrs, R.styleable.MageNativeTextView)
            val type = a.getString(R.styleable.MageNativeTextView_texttype)
            setTextColor(context.resources.getColor(R.color.black))
            when (Objects.requireNonNull<String>(type)) {
                "whitetext","dealwhite" ,"collectionslideritem","menushortform","usernameshort","productslideritemname_hv"-> setTextColor(
                    Color.WHITE)
                "blacktext","deallight"  ,"ratingvalue" -> setTextColor(context.resources.getColor(R.color.black))
                "normalgrey1txt" -> setTextColor(Color.parseColor(context.getString(R.string.normalgrey1text)))
                "normalgrey1txt16sp","varianttittle","subscribediscount" -> setTextColor(Color.parseColor(context.getString(R.string.normalgrey1text)))
                "secondary2txt" -> setTextColor(Color.parseColor(context.getString(R.string.secondary2)))
                "normalbluetxt" -> setTextColor(Color.parseColor(context.getString(R.string.bluetext)))
                "normalgrey1bigtxt" -> setTextColor(Color.parseColor(context.getString(R.string.normalgrey1text)))
                "normalgrey2txt" -> setTextColor(Color.parseColor(context.getString(R.string.normalgrey2text)))
                "normalgrey2txt36sp","productviewrating" -> setTextColor(Color.parseColor(context.getString(R.string.normalgrey2text)))
                "normalgrey2lighttxt14sp" -> setTextColor(Color.parseColor(context.getString(R.string.normalgrey2text)))
                "normalgrey3txt" -> setTextColor(Color.parseColor(context.getString(R.string.normalgrey3text)))
                "normalgrey3txtregular" -> setTextColor(Color.parseColor(context.getString(R.string.normalgrey3txtregular)))
                "productnamenormal","carttext" -> setTextColor(Color.parseColor(context.getString(R.string.normalgrey3text)))
                "normalgrey3lighttxt" -> setTextColor(Color.parseColor(context.getString(R.string.normalgrey3text)))
                "normalgrey3smalltxt" -> setTextColor(Color.parseColor(context.getString(R.string.normalgrey3text)))
                "normalgrey2bigtxt","sellinggroup" -> setTextColor(Color.parseColor(context.getString(R.string.normalgrey2text)))
                "greentxt" -> setTextColor(Color.parseColor(context.getString(R.string.greentext)))
                "whitenormaltext" -> setTextColor(Color.WHITE)
                "whitebigtext" -> setTextColor(Color.WHITE)
                "filtertextlist", "filtertext","instafeed_tag","offersellingplan"-> setTextColor(
                    Color.parseColor("#000000"))
                "productnamespecial"->setTextColor(context.resources.getColor(R.color.specialpricecolor))
                "productlistaddtocart"-> setTextColor(Color.parseColor(NewBaseActivity.textColor))
                "nodata"-> setTextColor(Color.parseColor(NewBaseActivity.textColor))
                "productlistname"->setTextColor(context.resources.getColor(R.color.normalgrey1text))
                "productlistdescription"->setTextColor(context.resources.getColor(R.color.normalgrey2text))
                "productlistnormalprice"->setTextColor(context.resources.getColor(R.color.normalgrey3text))
                "productlistoffertext","cartiteoffertext"->setTextColor(context.resources.getColor(R.color.greentext))
                "variantnamequick"->setTextColor(context.resources.getColor(R.color.normalgrey3text))
                "searchnormal"->setTextColor(context.resources.getColor(R.color.normalgrey3text))
                "advancesearch","cartitemnormalprice"->setTextColor(context.resources.getColor(R.color.normalgrey3text))
                "recentsearchkeyword","emptystring","cartitemvarint","cartitemremove"->setTextColor(context.resources.getColor(R.color.normalgrey3text))
                "recentsearchheading"->setTextColor(context.resources.getColor(R.color.normalgrey1text))
                "clearrecentsearch"->setTextColor(context.resources.getColor(R.color.clearrecentcolor))
                "nocarttext"->setTextColor(context.resources.getColor(R.color.nocarttext))
                "clearcart"->setTextColor(context.resources.getColor(R.color.normalgrey2text))
                "cartitemname","cartitemapply","menucategoryheading","previewtext","productname"->setTextColor(context.resources.getColor(R.color.normalgrey1text))
                "cartquantity","shippingtext"->setTextColor(context.resources.getColor(R.color.normalgrey2text))
                "carttotal","bold"->setTextColor(context.resources.getColor(R.color.black))
                "menuappversion","submenutext"->setTextColor(context.resources.getColor(R.color.submenutext))
                "sort_head","count","menucopyright"->setTextColor(context.resources.getColor(R.color.menucopyright))
                "myaccount_username","menuwelcometext","menuheadertext","menutext","previewdescription"->setTextColor(context.resources.getColor(R.color.normalgrey2text))
            }
            a.recycle()
        }
        catch (e:java.lang.Exception) {  Log.i("PRakharCheck","Error")
            e.printStackTrace()
        }
    }


    private fun setTextSize(attrs: AttributeSet) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.MageNativeRadioButton)
        val type = a.getString(R.styleable.MageNativeRadioButton_radiotype)
        when (type) {
            "bold" -> textSize = 15f
            "normal" -> textSize = 13f
        }
    }
}
