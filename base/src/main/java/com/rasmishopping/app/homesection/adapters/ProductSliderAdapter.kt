package com.rasmishopping.app.homesection.adapters

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.shopify.buy3.Storefront
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.models.CommanModel
import com.rasmishopping.app.basesection.models.ListData
import com.rasmishopping.app.customviews.MageNativeTextView
import com.rasmishopping.app.databinding.MSlideritemoneBinding
import com.rasmishopping.app.databinding.MSlideritemtwoBinding
import com.rasmishopping.app.homesection.viewholders.SliderItemTypeOne
import com.rasmishopping.app.productsection.activities.ProductView
import com.rasmishopping.app.quickadd_section.activities.QuickAddActivity
import com.rasmishopping.app.repositories.Repository
import com.rasmishopping.app.utils.Constant
import com.rasmishopping.app.utils.CurrencyFormatter
import org.json.JSONObject
import java.math.BigDecimal
import javax.inject.Inject

class ProductSliderAdapter
constructor() : RecyclerView.Adapter<SliderItemTypeOne>() {
    private var layoutInflater: LayoutInflater? = null
    private var products: List<Storefront.ProductEdge>? = null
    private var activity: Activity? = null

    var jsonObject: JSONObject? = null
    lateinit var repository: Repository
    fun setData(
        products: List<Storefront.ProductEdge>?,
        activity: Activity,
        jsonObject: JSONObject,
        repository: Repository
    ) {
        this.products = products
        this.activity = activity
        this.jsonObject = jsonObject
        this.repository = repository
    }

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderItemTypeOne {

        when (jsonObject!!.getString("item_shape")) {
            "square" -> {
                var binding = DataBindingUtil.inflate<MSlideritemtwoBinding>(
                    LayoutInflater.from(parent.context),
                    R.layout.m_slideritemtwo,
                    parent,
                    false
                )
                return SliderItemTypeOne(binding)
            }
            else -> {
                var binding = DataBindingUtil.inflate<MSlideritemoneBinding>(
                    layoutInflater!!,
                    R.layout.m_slideritemone,
                    parent,
                    false
                )
                return SliderItemTypeOne(binding)
            }
        }
    }

    override fun onBindViewHolder(item: SliderItemTypeOne, position: Int) {
        val variant = products?.get(position)?.node?.variants?.edges?.get(0)?.node
        val data = ListData()
        var view: View
        var card: View
        var tittle: MageNativeTextView
        var price: MageNativeTextView
        var special: MageNativeTextView
        data.product = products?.get(position)?.node
        data.textdata = products?.get(position)?.node?.title.toString().trim()
        data.regularprice = CurrencyFormatter.setsymbol(
            variant!!.price.amount,
            variant.price.currencyCode.toString()
        )
        if (variant.compareAtPrice != null) {
            val special = java.lang.Double.valueOf(variant.compareAtPrice.amount)
            val regular = java.lang.Double.valueOf(variant.price.amount)
            if (BigDecimal.valueOf(special).compareTo(BigDecimal.valueOf(regular)) == 1) {
                data.regularprice = CurrencyFormatter.setsymbol(
                    variant.compareAtPrice.amount,
                    variant.compareAtPrice.currencyCode.toString()
                )
                data.specialprice = CurrencyFormatter.setsymbol(
                    variant.price.amount,
                    variant.price.currencyCode.toString()
                )
                when (jsonObject!!.getString("item_shape")) {
                    "square" -> {
                        item.bindingtwo.regularprice.paintFlags =
                            item.bindingtwo.regularprice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                        item.bindingtwo.specialprice.visibility = View.VISIBLE
                    }
                    else -> {
                        item.bindingtwo.regularprice.paintFlags =
                            item.bindingtwo.regularprice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                        item.bindingtwo.specialprice.visibility = View.VISIBLE
                    }
                }
            } else {
                when (jsonObject!!.getString("item_shape")) {
                    "square" -> {
                        item.bindingtwo.specialprice.visibility = View.GONE
                        item.bindingtwo.regularprice.paintFlags =
                            item.bindingtwo.regularprice.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    }
                    else -> {
                        item.binding.specialprice.visibility = View.GONE
                        item.binding.regularprice.paintFlags =
                            item.binding.regularprice.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    }
                }

            }
        } else {
            when (jsonObject!!.getString("item_shape")) {
                "square" -> {
                    item.bindingtwo.specialprice.visibility = View.GONE
                    item.bindingtwo.regularprice.paintFlags =
                        item.bindingtwo.regularprice.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
                else -> {
                    item.bindingtwo.specialprice.visibility = View.GONE
                    item.bindingtwo.regularprice.paintFlags =
                        item.bindingtwo.regularprice.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
            }
        }
        val model = CommanModel()
        model.imageurl = products?.get(position)?.node?.images?.edges?.get(0)?.node?.url
        when (jsonObject!!.getString("item_shape")) {
            "square" -> {
                item.bindingtwo.listdata = data
                item.bindingtwo.commondata = model
                item.bindingtwo.clickproduct = Product(repository, activity!!)
            }
            else -> {
                item.binding.listdata = data
                item.binding.commondata = model
                //item.binding.clickproduct = Product(repository, activity!!)
            }
        }
        val params: ConstraintLayout.LayoutParams
        when (jsonObject!!.getString("item_shape")) {
            "square" -> {
                view = item.bindingtwo.main
                card = item.bindingtwo.card
                tittle = item.bindingtwo.name
                price = item.bindingtwo.regularprice
                special = item.bindingtwo.specialprice
                params =
                    item.bindingtwo.nameandpricesection.layoutParams as ConstraintLayout.LayoutParams
            }
            else -> {
               // view = item.binding.main
               // card = item.binding.card
                tittle = item.binding.name
                price = item.binding.regularprice
                special = item.binding.specialprice
               // params =
                  //  item.binding.nameandpricesection.layoutParams as ConstraintLayout.LayoutParams
            }
        }
        var alignment: String
        if (jsonObject!!.has("item_text_alignment")) {
            alignment = jsonObject!!.getString("item_text_alignment")
        } else {
            alignment = jsonObject!!.getString("item_alignment")
        }
        when (alignment) {
            "right" -> {
              //  params.endToEnd = ConstraintSet.PARENT_ID
              //  params.startToStart = ConstraintSet.GONE
            }
            "center" -> {
              //  params.endToEnd = ConstraintSet.PARENT_ID
               // params.startToStart = ConstraintSet.PARENT_ID
            }
        }
        var tittlevisibility: Int = View.GONE
        if (jsonObject!!.getString("item_title").equals("1")) {
            tittlevisibility = View.VISIBLE
        } else {
            tittlevisibility = View.GONE
        }
        var productpricevisibility: Int = View.GONE
        if (jsonObject!!.getString("item_price").equals("1")) {
            productpricevisibility = View.VISIBLE
        } else {
            productpricevisibility = View.GONE
        }
        var specialpricevisibility: Int = View.GONE
        if (jsonObject!!.getString("item_compare_at_price").equals("1")) {
            specialpricevisibility = View.VISIBLE
        } else {
            specialpricevisibility = View.GONE
        }
        when (jsonObject!!.getString("item_shape")) {
            "square" -> {
                item.bindingtwo.name.visibility = tittlevisibility
                item.bindingtwo.regularprice.visibility = productpricevisibility
                item.bindingtwo.specialprice.visibility = specialpricevisibility
            }
            else -> {
                item.binding.name.visibility = tittlevisibility
                item.binding.regularprice.visibility = productpricevisibility
                item.binding.specialprice.visibility = specialpricevisibility
            }
        }
        var cell_background_color = JSONObject(jsonObject!!.getString("cell_background_color"))
        var item_border_color = JSONObject(jsonObject!!.getString("item_border_color"))
        var item_title_color = JSONObject(jsonObject!!.getString("item_title_color"))
        var item_price_color = JSONObject(jsonObject!!.getString("item_price_color"))
        var item_compare_at_price_color =
            JSONObject(jsonObject!!.getString("item_compare_at_price_color"))
       // view.setBackgroundColor(Color.parseColor(cell_background_color.getString("color")))
      //  card.setBackgroundColor(Color.parseColor("#333333"))
        tittle.setTextColor(Color.parseColor(item_title_color.getString("color")))
        price.setTextColor(Color.parseColor(item_price_color.getString("color")))
        special.setTextColor(Color.parseColor(item_compare_at_price_color.getString("color")))
        val face: Typeface
        when (jsonObject!!.getString("item_title_font_weight")) {
            "bold" -> {
                face = Typeface.createFromAsset(activity!!.assets, "fonts/popbold.ttf")
            }
            else -> {
                face = Typeface.createFromAsset(activity!!.assets, "fonts/popnormal.ttf")
            }
        }
        tittle.typeface = face
        if (jsonObject!!.getString("item_title_font_style").equals("italic")) {
            tittle.setTypeface(tittle.typeface, Typeface.ITALIC)
        }
        val priceface: Typeface
        when (jsonObject!!.getString("header_subtitle_font_weight")) {
            "bold" -> {
                priceface = Typeface.createFromAsset(activity!!.assets, "fonts/popbold.ttf")
            }
            else -> {
                priceface = Typeface.createFromAsset(activity!!.assets, "fonts/popnormal.ttf")
            }
        }
        price.typeface = priceface
        if (jsonObject!!.getString("item_price_font_style").equals("italic")) {
            price.setTypeface(price.typeface, Typeface.ITALIC)
        }
        val specialpriceface: Typeface
        when (jsonObject!!.getString("item_compare_at_price_font_weight")) {
            "bold" -> {
                specialpriceface =
                    Typeface.createFromAsset(activity!!.assets, "fonts/popbold.ttf")
            }
            else -> {
                specialpriceface =
                    Typeface.createFromAsset(activity!!.assets, "fonts/popnormal.ttf")
            }
        }
        special.typeface = specialpriceface
        if (jsonObject!!.getString("item_compare_at_price_font_style").equals("italic")) {
            special.setTypeface(special.typeface, Typeface.ITALIC)
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return products!!.size
    }


    inner class Product(var repository: Repository, var activity: Activity) {
        fun productClick(view: View, data: ListData) {
            val productintent = Intent(view.context, ProductView::class.java)
            productintent.putExtra("ID", data.product!!.id.toString())
            productintent.putExtra("tittle", data.textdata)
            productintent.putExtra("product", data.product)
            view.context.startActivity(productintent)
            Constant.activityTransition(view.context)
        }

        fun addCart(view: View, data: ListData) {
            var customQuickAddActivity = QuickAddActivity(
                context = activity,
                theme = R.style.WideDialogFull,
                product_id = data.product!!.id.toString(),
                repository = repository,
                product = data.product!!

            )
            if(data.product!!.variants.edges.size>1){
                customQuickAddActivity.show()
            }else{
                customQuickAddActivity.addToCart(data.product!!.variants.edges.get(0).node.id.toString(),1)
            }
        }
    }
}
