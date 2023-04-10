package com.rasmishopping.app.collectionsection.adapters
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.databinding.CategoryItemBinding
import com.rasmishopping.app.productsection.activities.ProductList
import com.rasmishopping.app.repositories.Repository
import com.rasmishopping.app.utils.Constant
import org.json.JSONArray
import javax.inject.Inject
class CategoryAdapter @Inject constructor() : RecyclerView.Adapter<CategoryAdapter.CategoryViewHodler>() {
    var list: JSONArray? =null
    var tagSelectionCallBack: TagSelectionCallBack? = null
    var callback: CallBackForNoSubmenu? = null
    var repository: Repository? = null
    var context:Activity?=null
    var selectedposition=0
    fun setData(arr: JSONArray, tagSelectionCallBack: TagSelectionCallBack,context:Activity,callback:CallBackForNoSubmenu) {
        this.list = arr
        this.tagSelectionCallBack = tagSelectionCallBack
        this.repository = repository
        this.context=context
        this.callback=callback
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHodler {
        var binding = DataBindingUtil.inflate<CategoryItemBinding>(LayoutInflater.from(parent.context), R.layout.category_item, parent, false)
        return CategoryViewHodler(binding)
    }
    interface TagSelectionCallBack {
        fun tagCallback(list: JSONArray,id:String)
    }
    interface CallBackForNoSubmenu {
        fun Callback(id:String)
    }
    override fun onBindViewHolder(holder: CategoryViewHodler, @SuppressLint("RecyclerView") position: Int) {
        val data = "gid://shopify/Collection/" + list!!.getJSONObject(position).getString("id")
        holder.binding?.catname?.text = list!!.getJSONObject(position).getString("title").replace("</tc>", "").replace("<tc>", "")
        holder.binding!!.root.setOnClickListener {
            holder.binding?.catname?.tag="selected"
            selectedposition=position
            if(list!!.getJSONObject(position).has("menus") && list!!.getJSONObject(position).getJSONArray("menus").length()>0){
                tagSelectionCallBack!!.tagCallback(list!!.getJSONObject(position).getJSONArray("menus"),data)
            }else{
                callback!!.Callback(data)
            }
            notifyDataSetChanged()
        }
        if(holder.binding?.catname?.tag.toString().isEmpty()){
            if(position==0){
                holder.binding?.catname?.tag="selected"
            }else{
                holder.binding?.catname?.tag="unselected"
            }
        }else{
            if(selectedposition==position){
                holder.binding?.catname?.tag="selected"
            }else{
                holder.binding?.catname?.tag="unselected"
            }
        }
        if(holder.binding?.catname?.tag=="selected"){
            holder.binding!!.background.setBackgroundColor(Color.WHITE)
            if(list!!.getJSONObject(position).has("menus") && list!!.getJSONObject(position).getJSONArray("menus").length()>0){
                tagSelectionCallBack!!.tagCallback(list!!.getJSONObject(position).getJSONArray("menus"),data)
            }else{
                callback!!.Callback(data)
            }
        }else{
            holder.binding!!.background.setBackgroundColor(Color.parseColor(NewBaseActivity.themeColor))
            holder.binding!!.background.alpha=0.05f
        }
    }
    override fun getItemCount(): Int {
        return list!!.length()
    }
    class CategoryViewHodler(CategoryItemBinding: CategoryItemBinding) :
        RecyclerView.ViewHolder(CategoryItemBinding.root) {
        var binding: CategoryItemBinding? = CategoryItemBinding
    }
}