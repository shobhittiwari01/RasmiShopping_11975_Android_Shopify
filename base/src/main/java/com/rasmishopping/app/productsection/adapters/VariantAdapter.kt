package com.rasmishopping.app.productsection.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.basesection.activities.NewBaseActivity.Companion.themeColor
import com.rasmishopping.app.databinding.SwatchesListItemBinding
import com.rasmishopping.app.homesection.viewmodels.HomePageViewModel
import com.rasmishopping.app.productsection.viewholders.VariantItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class VariantAdapter : RecyclerView.Adapter<VariantItem>() {
    private var variants: MutableList<String>? = null
    private var context: Context? = null
    private val TAG = "VariantAdapter"
    private var outofStockList: MutableList<String>? = null
    private var selectedPosition = -1
    private var optionName: String? = null
    private var variant_data: MutableList<String>? = mutableListOf()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VariantItem {
        val binding = DataBindingUtil.inflate<SwatchesListItemBinding>(
            LayoutInflater.from(parent.context),
            R.layout.swatches_list_item,
            parent,
            false
        )
        binding.variantName.textSize = 14f
        return VariantItem(binding)
    }

    companion object {
        var variantCallback: VariantCallback? = null
    }

    interface VariantCallback {
        fun clickVariant(variantName: String, optionName: String)
    }

    override fun onBindViewHolder(holder: VariantItem, @SuppressLint("RecyclerView") position: Int) {
        holder.binding.variantName.text = variants?.get(position)
        if (selectedPosition == position) {
            holder.binding.variantName.setBackgroundColor(Color.parseColor(themeColor))
            holder.binding.variantName.setTextColor(Color.parseColor(NewBaseActivity.textColor))
        } else {
            holder.binding.variantName.background = ContextCompat.getDrawable(HomePageViewModel.themedContext!!, R.drawable.lineborder)
            holder.binding.variantName.setTextColor(ContextCompat.getColor(HomePageViewModel.themedContext!!, R.color.normalgrey3text))
        }
        holder.binding.variantName.setOnClickListener {
            selectedPosition = position
            variantCallback?.clickVariant(variants?.get(position) ?: "", optionName ?: "")
            notifyDataSetChanged()
        }
        holder.setIsRecyclable(false)
    }

    override fun getItemCount(): Int {
        return variants?.size!!
    }

    fun setData(
        variant_data: MutableList<String>,
        optionName: String,
        variants: MutableList<String>,
        outofStockList: MutableList<String>,
        context: Context,
        variantCallback_: VariantCallback
    ) {
        this.variant_data = variant_data
        this.optionName = optionName
        this.variants = variants
        this.outofStockList = outofStockList
        variantCallback = variantCallback_
        this.context = context
    }
    fun setData(
        optionName: String,
        variants: MutableList<String>,
        context: Context,
        variantCallback_: VariantCallback
    ) {
        this.optionName = optionName
        this.variants = variants
        variantCallback = variantCallback_
        this.context = context
    }

}
