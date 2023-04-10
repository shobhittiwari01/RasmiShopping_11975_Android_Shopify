package com.rasmishopping.app.customviews

import android.content.Context
import android.content.res.Resources.Theme
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import java.util.*

class MageNativeTextView : AppCompatTextView {

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs)
        setSize(attrs)
        setColor(attrs)
        setBack(attrs)
        setCaps(attrs)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
        setSize(attrs)
        setColor(attrs)
        setBack(attrs)
        setCaps(attrs)
    }
    fun setCaps(attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.MageNativeTextView)
        val type = a.getString(R.styleable.MageNativeTextView_texttype)
        when (Objects.requireNonNull<String>(type)) {
            "menushortform"->isAllCaps=true
        }
    }
    fun setBack(attrs: AttributeSet?){
        val a = context.obtainStyledAttributes(attrs, R.styleable.MageNativeTextView)
        val type = a.getString(R.styleable.MageNativeTextView_texttype)
        when (Objects.requireNonNull<String>(type)) {
            "filtertext" -> setBackgroundColor(Color.parseColor("#000000"))
            "categorybackgound" -> setBackgroundColor(Color.parseColor(NewBaseActivity.themeColor))
            "nodata" -> setBackgroundColor(Color.parseColor(NewBaseActivity.themeColor))
           // "productlistaddtocart" -> setBackgroundColor(Color.parseColor(NewBaseActivity.themeColor))
        }
    }
    fun setColor(attrs: AttributeSet?) {
        try
        {
            val a = context.obtainStyledAttributes(attrs, R.styleable.MageNativeTextView)
            val type = a.getString(R.styleable.MageNativeTextView_texttype)
            setTextColor(context.resources.getColor(R.color.black))
            when (Objects.requireNonNull<String>(type)) {
                "whitetext","dealwhite" ,"collectionslideritem","menushortform","usernameshort","productslideritemname_hv"-> setTextColor(Color.WHITE)
                "productslideritemname", "blacktext","deallight"  ,"ratingvalue" -> setTextColor(context.resources.getColor(R.color.black))
                "sign_text",    "normalgrey1txt" -> setTextColor(Color.parseColor(context.getString(R.string.normalgrey1text)))
                "normalgrey1txt16sp","varianttittle","subscribediscount" -> setTextColor(Color.parseColor(context.getString(R.string.normalgrey1text)))
                "secondary2txt" -> setTextColor(Color.parseColor(context.getString(R.string.secondary2)))
                "normalbluetxt" -> setTextColor(Color.parseColor(context.getString(R.string.bluetext)))
                "normalgrey1bigtxt" -> setTextColor(Color.parseColor(context.getString(R.string.normalgrey1text)))
                "normal",  "normalgrey2txt" -> setTextColor(Color.parseColor(context.getString(R.string.normalgrey2text)))
                "notificationtittle"->setTextColor(context.resources.getColor(R.color.notificationtittle))
                "normalgrey2txt36sp","productviewrating" -> setTextColor(Color.parseColor(context.getString(R.string.normalgrey2text)))
                "normalgrey2lighttxt14sp" -> setTextColor(Color.parseColor(context.getString(R.string.normalgrey2text)))
                "normalgrey3txt" -> setTextColor(Color.parseColor(context.getString(R.string.normalgrey3text)))
                "normalgrey3txtregular" -> setTextColor(Color.parseColor(context.getString(R.string.normalgrey3txtregular)))
                "productnamenormal","carttext" -> setTextColor(Color.parseColor(context.getString(R.string.normalgrey3text)))
                "carttextt" -> setTextColor(Color.parseColor(context.getString(R.string.normalgrey3textt)))
                "normalgrey3lighttxt" -> setTextColor(Color.parseColor(context.getString(R.string.normalgrey3text)))
                "normalgrey3smalltxt","shortdescriptionheading" -> setTextColor(Color.parseColor(context.getString(R.string.normalgrey3text)))
                "normalgrey2bigtxt","sellinggroup" -> setTextColor(Color.parseColor(context.getString(R.string.normalgrey2text)))
                "greentxt" -> setTextColor(Color.parseColor(context.getString(R.string.greentext)))
                "whitenormaltext" -> setTextColor(Color.WHITE)
                "whitebigtext" -> setTextColor(Color.WHITE)
                "filtertext","instafeed_tag","offersellingplan"-> setTextColor(Color.parseColor("#000000"))
                "productnamespecial"->setTextColor(context.resources.getColor(R.color.specialpricecolor))
                "productlistaddtocart"-> setTextColor(Color.parseColor(NewBaseActivity.textColor))
                "nodata"-> setTextColor(Color.parseColor(NewBaseActivity.textColor))
                "productlistname"->setTextColor(context.resources.getColor(R.color.black))
                "productlistdescription"->setTextColor(context.resources.getColor(R.color.normalgrey2text))
                "productlistnormalprice"->setTextColor(context.resources.getColor(R.color.normalgrey3text))
                "productlistoffertext","cartiteoffertext"->setTextColor(context.resources.getColor(R.color.greentext))
                "variantnamequick"->setTextColor(context.resources.getColor(R.color.normalgrey3text))
                "searchnormal"->setTextColor(context.resources.getColor(R.color.normalgrey3text))
                "cartitemnormalprice"->setTextColor(context.resources.getColor(R.color.normalgrey3text))
                "newcarttext"->setTextColor(Color.parseColor("#FFFFFF"))
                "recentsearchheading"->setTextColor(context.resources.getColor(R.color.normalgrey1text))
                "clearrecentsearch"->setTextColor(context.resources.getColor(R.color.clearrecentcolor))
                "nocarttext"->setTextColor(context.resources.getColor(R.color.nocarttext))
                "clearcart"->setTextColor(context.resources.getColor(R.color.normalgrey2text))
                "notificationmessage"->setTextColor(context.resources.getColor(R.color.notificationmessage))
                "cartitemname","cartitemapply","menucategoryheading","previewtext","productname"->setTextColor(context.resources.getColor(R.color.normalgrey1text))
                "cartquantity","shippingtext"->setTextColor(context.resources.getColor(R.color.normalgrey2text))
                "filtertextlist",  "carttotal","bold"->setTextColor(context.resources.getColor(R.color.black))
                "menuappversion","submenutext"->setTextColor(context.resources.getColor(R.color.submenutext))
                "sort_head","count","menucopyright"->setTextColor(context.resources.getColor(R.color.menucopyright))
                "advancesearch"->setTextColor(context.resources.getColor(R.color.advancesearch))
                "myaccount_username","menuwelcometext","menuheadertext","menutext","previewdescription"->setTextColor(context.resources.getColor(R.color.normalgrey2text))
            "descriptionlight","myaccount_username","menuwelcometext","menuheadertext","menutext","previewdescription"->setTextColor(context.resources.getColor(R.color.normalgrey2text))
            }
            a.recycle()
        }
        catch (e:java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun init(attrs: AttributeSet?) {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.MageNativeTextView)
            val type = a.getString(R.styleable.MageNativeTextView_texttype)
            var typeface = Typeface.createFromAsset(context.assets, "fonts/popnormal.ttf")
            try {
                when (type) {
                    "light" ,"deallight","productslidersubheader","descriptionlight","filter","productlistdescription","productviewrating","shortdescriptionheading"-> {
                        typeface = Typeface.createFromAsset(context!!.assets, "fonts/poplight.ttf")
                    }
                    "sign_text",  "medium","productsliderheader","ratingvalue" ,"productlistname","previewtext","offersellingplan"-> {
                        typeface = Typeface.createFromAsset(context!!.assets, "fonts/popmedium.ttf")
                    }
                   "sort_heading", "normal","notificationtittle","deal","dealwhite","productslideraction" ,"productslideritemname","productslideritemprice","productslideritemname_hv"-> {
                        typeface = Typeface.createFromAsset(context!!.assets, "fonts/popnormal.ttf")
                    }
                    "whitetext","circleslidertext","standalonebannertext","collectionslideritem","previewdescription","submenutext"-> {
                        typeface = Typeface.createFromAsset(context.assets, "fonts/popnormal.ttf")
                    }
                   "newcarttext" ,"count","filtertextlist", "blacktext","filtertext","carttext","productlistaddtocart" ,"quickaddoptionname","menutext","menucopyright","varianttittle"-> {
                        typeface = Typeface.createFromAsset(context.assets, "fonts/popnormal.ttf")
                    }
                    "whitenormaltext" ,"recentsearchkeyword","cartitemname","cartiteoffertext","productnamespecial","productnamenormal"-> {
                        typeface = Typeface.createFromAsset(context.assets, "fonts/popnormal.ttf")
                    }
                    "whitebigtext","clearcart","notificationmessage","cartitemvarint","cartquantity","cartitemremove","menuappversion","sellinggroup" ,"subscribediscount"-> {
                        typeface = Typeface.createFromAsset(context.assets, "fonts/popnormal.ttf")
                    }
                  "bold","menushortform"-> {
                        typeface = Typeface.createFromAsset(context.assets, "fonts/popbold.ttf")
                    }
                    "boldtext","usernameshort" -> {
                        typeface = Typeface.createFromAsset(context.assets, "fonts/popbold.ttf")
                    }

                    "greentxt" ,"emptystring","cartitemapply","cartitembottom","menucategoryheading"-> {
                        typeface = Typeface.createFromAsset(context.assets, "fonts/popnormal.ttf")
                    }
                    "normalgrey1txt" ,"productlistspecialprice","productlistnormalprice","cartitemspecialprice","cartitemnormalprice"-> {
                        typeface = Typeface.createFromAsset(context.assets, "fonts/popmedium.ttf")
                    }
                    "normalgrey1txt16sp" ,"categorylistname","productname"-> {
                        typeface = Typeface.createFromAsset(context.assets, "fonts/popnormal.ttf")
                    }
                    "normalbluetxt","searchnormal" ,"clearrecentsearch"-> {
                        typeface = Typeface.createFromAsset(context.assets, "fonts/popnormal.ttf")
                    }
                    "normalgrey1bigtxt","recentsearchheading","toolcarttext","carttotalprice"  -> {
                        typeface = Typeface.createFromAsset(context.assets, "fonts/popmedium.ttf")
                    }
                    "normalgrey2txt","menuheadertext" -> {
                        typeface = Typeface.createFromAsset(context.assets, "fonts/popnormal.ttf")
                    }

                    "normalgrey2txt36sp","nodata" -> {
                        typeface = Typeface.createFromAsset(context.assets, "fonts/popnormal.ttf")
                    }

                    "normalgrey3txt","normalgrey3txtregular","advancesearch","menuwelcometext" -> {
                        typeface = Typeface.createFromAsset(context.assets, "fonts/popnormal.ttf")
                    }

                    "normalgrey2lighttxt" -> {
                        typeface = Typeface.createFromAsset(context.assets, "fonts/poplight.ttf")
                    }
                    "normalgrey2lighttxt14sp","productlistoffertext" -> {
                        typeface = Typeface.createFromAsset(context.assets, "fonts/poplight.ttf")
                    }
                    "secondary2txt","variantnamequick","toolcartsubtext" -> {
                        typeface = Typeface.createFromAsset(context.assets, "fonts/poplight.ttf")
                    }
                    "normalgrey3lighttxt" -> {
                        typeface = Typeface.createFromAsset(context.assets, "fonts/poplight.ttf")
                    }
                    "normalgrey3smalltxt","shippingtext" -> {
                        typeface = Typeface.createFromAsset(context.assets, "fonts/poplight.ttf")
                    }
                    "normalgrey2bigtxt" -> {
                        typeface = Typeface.createFromAsset(context.assets, "fonts/popnormal.ttf")
                    }
                    "tooltext","instafeed","instafeed_tag","nocarttext"->{
                        typeface = Typeface.createFromAsset(context.assets, "fonts/popmedium.ttf")
                    }
                    "tooltextnormal","carttotal","myaccount_username"->{
                        typeface = Typeface.createFromAsset(context.assets, "fonts/popnormal.ttf")
                    }
                }
                setTypeface(typeface)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            a.recycle()
        }
    }

    private fun setSize(attrs: AttributeSet) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.MageNativeTextView)
        val type = a.getString(R.styleable.MageNativeTextView_texttype)
        textSize=15f
        when (Objects.requireNonNull<String>(type)) {
            "sign_text",   "bold","productslidersubheader","productslideritemname" ,"productslideritemname_hv","menushortform","productviewrating"-> textSize = 14f
            "boldtext","deal","productsliderheader" ,"instafeed","instafeed_tag","previewtext","offersellingplan"-> textSize =16f
            "normal","notificationtittle","productslideraction","standalonebannertext","productlistspecialprice" ,"productlistnormalprice","varianttittle"-> textSize = 14f
            "normalgrey3smalltxt" ,"productlistoffertext"-> textSize = 10f
            "normalgrey1txt","productslideritemprice" ,"circleslidertext","descriptionlight","filter","menucopyright"-> textSize = 12f
            "newcarttext",   "normalgrey1txt16sp","carttext"-> textSize = 16f
            "normalgrey1bigtxt","productlistname","productlistaddtocart","submenutext","productname","productnamenormal" -> textSize = 14f
            "greentxt" ,"collectionslideritem","cartitemvarint","cartitemremove","previewdescription"-> textSize = 12f
            "normalbluetxt","tooltextnormal" ,"menuappversion","shortdescriptionheading"-> textSize = 12f
            "whitenormaltext","productlistdescription" ,"advancesearch"-> textSize = 12f
            "secondary2txt" ,"recentsearchkeyword"-> textSize = 12f
           "count", "whitebigtext","filtertext" ,"ratingvalue","nocarttext" ,"filtertextlist"-> textSize = 11f

            "normalgrey2txt" -> textSize = 12f
            "normalgrey2txt36sp" -> textSize = 36f
            "normalgrey2lighttxt","menuheadertext" -> textSize = 12f
            "normalgrey2lighttxt14sp" ,"emptystring","carttotal","myaccount_username","menucategoryheading","subscribediscount"-> textSize = 14f
            "normalgrey3txt" ,"toolcartsubtext","shippingtext"-> textSize = 12f
            "normalgrey3lighttxt","variantnamequick","clearrecentsearch","clearcart","notificationmessage" -> textSize = 12f
            "normalgrey2bigtxt","quickaddoptionname","cartitemname","cartitemapply","cartitembottom","productnamespecial" -> textSize = 14f
            "whitetext" -> textSize = 13f
            "sort_head", "blacktext" -> textSize = 13f
            "tooltext" ,"searchnormal","cartitemspecialprice","cartitemnormalprice","cartiteoffertext","cartquantity"-> textSize = 14f
            "dealwhite" -> textSize = 19f
            "deallight" -> textSize = 10f
          "menutext","sellinggroup" -> textSize = 14f
        "sign_text"  ,  "usernameshort" -> textSize = 25f
            "categorylistname","recentsearchheading","nodata","toolcarttext","carttotalprice", "menuwelcometext"-> textSize = 16f

        }
        a.recycle()
    }
}
