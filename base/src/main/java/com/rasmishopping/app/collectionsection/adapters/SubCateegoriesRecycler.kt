package com.rasmishopping.app.collectionsection.adapters
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.models.CommanModel
import com.rasmishopping.app.databinding.SubcatItemBinding
import com.rasmishopping.app.productsection.activities.ProductList
import org.json.JSONArray
import java.lang.Exception
import javax.inject.Inject
class SubCategoriesRecycler @Inject constructor() : RecyclerView.Adapter<SubCategoriesRecycler.MyViewHolder>() {
    private var layoutInflater: LayoutInflater? = null
    private var array: JSONArray? = null
    private var context: Context? = null
    var menuObject: String? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.context)
        }
        val binding = DataBindingUtil.inflate<SubcatItemBinding>(layoutInflater!!, R.layout.subcat_item, parent, false)
        return MyViewHolder(binding)
    }
    fun setSubCatRecylerData(array: JSONArray?=null, context: Context) {
        this.array = array
        this.context = context
    }
    override fun onBindViewHolder(holder: SubCategoriesRecycler.MyViewHolder, position: Int) {
        try {
            Log.d("subcatdata", array!!.getJSONObject(position).getString("title"))
            holder.binding!!.catname.text = array!!.getJSONObject(position).getString("title")
            if(array?.getJSONObject(position)!!.has("image-src")) {
                Glide.with(context!!)
                    .load(array!!.getJSONObject(position).getString("image-src"))
                    .placeholder(R.drawable.image_placeholder)
                    .into(holder.binding?.subcatimage!!)
            }else{
                Glide.with(context!!)
                    .load(R.drawable.image_placeholder)
                    .into(holder.binding?.subcatimage!!)
            }
            if(array!!.getJSONObject(position).has("menus")){
                menuObject = array!!.getJSONObject(position).getJSONArray("menus").toString()
            }
            val data = "gid://shopify/Collection/" + array!!.getJSONObject(position).getString("id")
            val id = data
            holder.binding!!.catname.tag = id
            holder.binding!!.subcatimage.setOnClickListener {
                val intent = Intent(context, ProductList::class.java)
                intent.putExtra("ID", holder.binding!!.catname.tag.toString())
                intent.putExtra("tittle", holder.binding!!.catname.text)
                intent.putExtra("menu", menuObject)
                context!!.startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    override fun getItemCount(): Int {
        return array!!.length()
    }
    class MyViewHolder(binding: SubcatItemBinding) : RecyclerView.ViewHolder(binding.root) {
        var binding: SubcatItemBinding? = binding
    }
}