package com.rasmishopping.app.productsection.adapters
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.shopify.buy3.Storefront
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.databinding.GroupsLayoutBinding
import com.rasmishopping.app.homesection.viewmodels.HomePageViewModel
import com.rasmishopping.app.productsection.viewholders.GroupItems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
class SellingPlanGroupAdapter : RecyclerView.Adapter<GroupItems>() {
    private var sellinggroupdata: MutableList<Storefront.SellingPlanGroupEdge>? = null
    private var offer_group_data: MutableList<String>? = null
    private var context: Context? = null
    private var selectedPosition = -1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupItems {
        val binding = DataBindingUtil.inflate<GroupsLayoutBinding>(
            LayoutInflater.from(parent.context),
            R.layout.groups_layout,
            parent,
            false
        )
        binding.variantName.textSize = 14f
        return GroupItems(binding)
    }
    companion object {
        var variantCallback: VariantCallback? = null
    }
    interface VariantCallback {
        fun clickVariant(variantName: String, optionName: MutableList<Storefront.SellingPlanEdge>)
    }
    override fun onBindViewHolder(holder: GroupItems, @SuppressLint("RecyclerView") position: Int) {
        try { holder.binding.variantName.text =sellinggroupdata?.get(position)?.node?.name
           if (selectedPosition == -1) {
                selectedPosition = 0
                GlobalScope.launch(Dispatchers.Main) {
                    holder.binding.variantName.performClick()
                    selectedPosition = position
                    holder.binding.variantName.tag = "selected"
                    variantCallback?.clickVariant(sellinggroupdata?.get(position)?.node?.name ?: "", sellinggroupdata?.get(position)?.node?.sellingPlans?.edges!!)
                    Log.d("options",""+offer_group_data)
                }
            }
            if (selectedPosition == position) {
                var back =ContextCompat.getDrawable(HomePageViewModel.themedContext!!, R.drawable.selector) as GradientDrawable
                back.setColor(Color.parseColor(NewBaseActivity.themeColor))
                back.setStroke(1,Color.parseColor(NewBaseActivity.themeColor))
                holder.binding.variantName.background=back
                holder.binding.variantName.setTextColor(Color.parseColor(NewBaseActivity.textColor))
                holder.binding.variantName.isEnabled = true
                holder.binding.variantName.tag = "selected"
            } else {
                holder.binding.variantName.background = ContextCompat.getDrawable(HomePageViewModel.themedContext!!, R.drawable.unselector)
                holder.binding.variantName.setTextColor(ContextCompat.getColor(HomePageViewModel.themedContext!!, R.color.normalgrey2text))
                holder.binding.variantName.isEnabled = true
                holder.binding.variantName.tag = "unselected"
            }
            holder.binding.variantName.setOnClickListener {
                if (it.tag.equals("unselected")) {
                    selectedPosition = position
                    variantCallback?.clickVariant(sellinggroupdata?.get(position)?.node?.name ?: "", sellinggroupdata?.get(position)?.node?.sellingPlans?.edges!!)
                    Log.d("offer_group_data",""+sellinggroupdata?.get(position)?.node?.name)
                    Log.d("offer_group",""+sellinggroupdata?.get(position)?.node?.sellingPlans?.edges!!)
                    notifyDataSetChanged()

                }
            }
            holder.setIsRecyclable(false)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun getItemCount(): Int {
        return sellinggroupdata?.size!!
    }
    fun setData(
        sellinggroupdata:
        MutableList<Storefront.SellingPlanGroupEdge>,context: Context,    variantCallback_:VariantCallback,offer_group_data: MutableList<String>
    ) {
        this.sellinggroupdata = sellinggroupdata
        this.offer_group_data=offer_group_data
        this.context = context
        variantCallback = variantCallback_
    }

}
