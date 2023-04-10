package com.rasmishopping.app.productsection.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.shopify.buy3.Storefront
import com.rasmishopping.app.MyApplication.Companion.context
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.basesection.activities.Weblink
import com.rasmishopping.app.basesection.models.ListData
import com.rasmishopping.app.basesection.models.MenuData
import com.rasmishopping.app.databinding.SubcategoryItemLayoutBinding
import com.rasmishopping.app.productsection.activities.ProductList

class SubCategoryAdapter  : RecyclerView.Adapter<SubCategoryAdapter.MyViewHolder>() {
    private var layoutInflater: LayoutInflater? = null
    private var array: ArrayList<Storefront.MenuItem>? = null
    private var context: Context? = null
    var menuObject: String? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.context)
        }
        val binding = DataBindingUtil.inflate<SubcategoryItemLayoutBinding>(
            layoutInflater!!,
            R.layout.subcategory_item_layout,
            parent,
            false
        )
        return MyViewHolder(binding)
    }

    fun setSubCatRecylerData(array: ArrayList<Storefront.MenuItem>, context: Context) {
        this.array = array
        this.context = context
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.binding!!.textViewName.text = array!!.get(position).title
            holder.binding!!.cardView.setCardBackgroundColor(Color.parseColor(NewBaseActivity.themeColor))
            holder.binding!!.textViewName.setTextColor(Color.parseColor(NewBaseActivity.textColor))
            holder.binding!!.cardView.setOnClickListener {
                if(array!!.get(position).type.toString().equals("HTTP")){
                    val intent = Intent(context, Weblink::class.java)
                    intent.putExtra("name", holder.binding!!.textViewName.text)
                    intent.putExtra("link", array!!.get(position).url)
                    context!!.startActivity(intent)
                }else{
                    val intent = Intent(context, ProductList::class.java)
                    intent.putExtra("tittle", holder.binding!!.textViewName.text)
                    intent.putExtra("ID", array!!.get(position).resourceId.toString())
                    context!!.startActivity(intent)
                }
        }
    }


    override fun getItemCount(): Int {

        return array!!.size
    }

    class MyViewHolder : RecyclerView.ViewHolder {
        var binding: SubcategoryItemLayoutBinding? = null

        constructor(binding: SubcategoryItemLayoutBinding) : super(binding.root) {
            this.binding = binding
        }
        inner class Product(var position: Int) {
            @SuppressLint("LogNotTimber")
            fun productClick(view: View, data: ListData?) {
                try {
                    val menuData = MenuData()

                } catch (e: Exception) {
                    Toast.makeText(context, "Something went wrong.Please try again.", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }
}