package com.rasmishopping.app.productsection.adapters

import android.app.Activity
import android.content.Intent
import android.graphics.Paint
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.models.CommanModel
import com.rasmishopping.app.basesection.models.ListData
import com.rasmishopping.app.databinding.MProductfilteritemBinding
import com.rasmishopping.app.productsection.activities.ProductView
import com.rasmishopping.app.productsection.viewholders.ProductFilterItem
import com.rasmishopping.app.quickadd_section.activities.QuickAddActivity
import com.rasmishopping.app.repositories.Repository
import com.rasmishopping.app.utils.Constant
import com.rasmishopping.app.utils.CurrencyFormatter
import org.json.JSONArray
import java.io.UnsupportedEncodingException
import java.math.BigDecimal
import java.nio.charset.Charset
import javax.inject.Inject

class ProductFilterRecylerAdapter @Inject constructor() :
    RecyclerView.Adapter<ProductFilterItem>() {
    lateinit var products: JSONArray
    private var activity: Activity? = null
    private var repository: Repository? = null

    fun setData(products: JSONArray?, activity: Activity, repository: Repository) {
        this.products = products as JSONArray
        this.activity = activity
        this.repository = repository
    }

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductFilterItem {
        val binding = DataBindingUtil.inflate<MProductfilteritemBinding>(
            LayoutInflater.from(parent.context),
            R.layout.m_productfilteritem, parent, false
        )
        return ProductFilterItem(binding)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onBindViewHolder(holder: ProductFilterItem, position: Int) {
        //val variant = this.products[position].node.variants.edges[0].node
        val variant =
            this.products.getJSONObject(position)//.getJSONArray("variants").getJSONObject(0)
        val data = ListData()

        //data.product = this.products[position].node

        val s1 = "gid://shopify/Product/" + this.products.getJSONObject(position).getString("id")
        data.id =s1
        data.textdata = this.products.getJSONObject(position).getString("title")
        data.description = this.products.getJSONObject(position).getString("description")
        data.regularprice = CurrencyFormatter.setsymbol(variant.getString("price"), "")
        if (variant.get("compare_at_price") != null && !variant.get("compare_at_price")
                .equals("")
        ) {
            val special = java.lang.Double.valueOf(variant.getString("compare_at_price"))
            val regular = java.lang.Double.valueOf(variant.getString("price"))
            if (BigDecimal.valueOf(special).compareTo(BigDecimal.valueOf(regular)) == 1) {
                data.regularprice =
                    CurrencyFormatter.setsymbol(variant.getString("compare_at_price"), "")
                data.specialprice = CurrencyFormatter.setsymbol(variant.getString("price"), "")
                data.offertext = getDiscount(special, regular).toString() + "%off"

            } else {
                data.regularprice = CurrencyFormatter.setsymbol(variant.getString("price"), "")
                data.specialprice =
                    CurrencyFormatter.setsymbol(variant.getString("compare_at_price"), "")
                data.offertext = getDiscount(regular, special).toString() + "%off"
            }
            holder.binding!!.regularprice.paintFlags =
                holder.binding!!.regularprice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.binding!!.specialprice.visibility = View.VISIBLE
            holder.binding!!.offertext.visibility = View.VISIBLE
            holder.binding!!.offertext.setTextColor(activity!!.resources.getColor(R.color.green))
        } else {
            holder.binding!!.specialprice.visibility = View.GONE
            holder.binding!!.offertext.visibility = View.GONE
            holder.binding!!.regularprice.paintFlags =
                holder.binding!!.regularprice.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
        holder.binding!!.listdata = data
        val model = CommanModel()
        if (this.products.getJSONObject(position).has("featured_image")) {
            Log.i(
                "imageurlre",
                "" + this.products.getJSONObject(position).getString("featured_image")
            )
            model.imageurl =
                "https:" + this.products.getJSONObject(position).getString("featured_image")
        }
        holder.binding!!.commondata = model
        holder.binding!!.clickproduct = Product(position)
    }

    private fun getBase64Encode(id: String): String {
        var id = id
        val data = Base64.encode(id.toByteArray(), Base64.DEFAULT)
        try {
            id = String(data, Charset.defaultCharset()).trim { it <= ' ' }
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        return id
    }

    override fun getItemCount(): Int {
        return products.length()
    }

    fun getDiscount(regular: Double, special: Double): Int {

        return ((regular - special) / regular * 100).toInt()
    }

    inner class Product(var position: Int) {
        fun productClick(view: View, data: ListData) {
            val productintent = Intent(view.context, ProductView::class.java)
            productintent.putExtra("ID", data.id)
            productintent.putExtra("tittle", data.textdata)
            //productintent.putExtra("product", data.product)
            productintent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            productintent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            productintent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            view.context.startActivity(productintent)
            Constant.activityTransition(view.context)
        }

        fun addCart(view: View, data: ListData) {
            var customQuickAddActivity = QuickAddActivity(
                context = activity!!,
                theme = R.style.WideDialogFull,
                product_id = data.product!!.id.toString(),
                repository = repository!!,
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
