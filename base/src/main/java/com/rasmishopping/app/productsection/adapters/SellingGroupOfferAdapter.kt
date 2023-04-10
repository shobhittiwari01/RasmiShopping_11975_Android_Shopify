package com.rasmishopping.app.productsection.adapters
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.shopify.buy3.Storefront
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.databinding.GroupSwatchesBinding
import com.rasmishopping.app.homesection.viewmodels.HomePageViewModel
import com.rasmishopping.app.productsection.activities.ProductView
import com.rasmishopping.app.productsection.viewholders.OfferItems
import com.rasmishopping.app.utils.CurrencyFormatter

class SellingGroupOfferAdapter : RecyclerView.Adapter<OfferItems>() {
    private var offers: MutableList<Storefront.SellingPlanEdge>? = null
    private var context: Context? = null
    private var selectedPosition = -1
    private var variant_data: MutableList<String>? = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfferItems {
        val binding = DataBindingUtil.inflate<GroupSwatchesBinding>(
            LayoutInflater.from(parent.context),
            R.layout.group_swatches,
            parent,
            false
        )
        binding.variantName.textSize = 14f
        return OfferItems(binding)
    }
    companion object {
        var variantCallback: VariantCallback? = null
    }
    interface VariantCallback {
        fun clickVariant(
            offername: String,
            offer_percentage: String,
            plan_id: String
        )
    }

    override fun onBindViewHolder(holder: OfferItems, position: Int) {
        try {
            var offer_value: String? = " "
            var color=NewBaseActivity.themeColor
            when(NewBaseActivity.themeColor){
                "#FFFFFF"->color=NewBaseActivity.textColor
            }
            holder.binding.variantName.text = offers?.get(position)?.node?.name
             if (selectedPosition == position) {
                var back =ContextCompat.getDrawable(HomePageViewModel.themedContext!!, R.drawable.selector_offer) as GradientDrawable
                back.setStroke(2,Color.parseColor(color))
                holder.binding.variantCard.background=back
                holder.binding.variantName.setTextColor(Color.parseColor(color))
                holder.binding.variantName.isEnabled = true
                holder.binding.variantCard.tag = "selected"
                holder.binding.successTick.visibility = View.VISIBLE
            } else {
                holder.binding.variantCard.background = ContextCompat.getDrawable(HomePageViewModel.themedContext!!, R.drawable.unselector)
                holder.binding.variantName.setTextColor(Color.parseColor(color))
                holder.binding.variantName.isEnabled = true
                holder.binding.variantCard.tag = "unselected"
                holder.binding.successTick.visibility = View.GONE
            }
            holder.binding.variantCard.setOnClickListener {
                if (offers?.get(position)?.node?.priceAdjustments?.size!! > 0) {
                    var discount = offers?.get(position)?.node?.priceAdjustments?.get(0)?.adjustmentValue
                    if (discount is Storefront.SellingPlanPercentagePriceAdjustment){
                        offer_value=context!!.resources.getString(R.string.availablediscount)+" "+
                                discount.adjustmentPercentage.toString()+"%"
                    }
                    if (discount is Storefront.SellingPlanFixedAmountPriceAdjustment){
                        offer_value= context!!.resources.getString(R.string.availablediscount)+" "+
                                CurrencyFormatter.setsymbol(discount.adjustmentAmount.amount,discount.adjustmentAmount.currencyCode.toString())
                    }
                } else {
                    offer_value = context!!.resources.getString(R.string.nooffer)
                }
                var offerSellingID= offers!![position].node.id
                Log.d("Prakhar",""+ProductView.VariantSellingID)
                Log.d("Prakhar",""+ offers!![position].node.id.toString())
                if(ProductView.VariantSellingID!!.size>0){
                    if (ProductView.VariantSellingID.containsKey(offerSellingID)){
                        var sellingplans=ProductView.VariantSellingID.get(offerSellingID)
                        if(ProductView.variantId!=null && sellingplans!!.contains(ProductView.variantId)){
                            clickManage(it,position,offer_value!!)
                        }else{
                            Toast.makeText(context,context!!.resources.getString(R.string.notavailble),Toast.LENGTH_LONG).show()
                        }
                    }else{
                        clickManage(it,position,offer_value!!)
                    }
                }else{
                    clickManage(it,position,offer_value!!)
                }

            }
            holder.setIsRecyclable(false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    override fun getItemCount(): Int {
        return offers?.size!!
    }
    fun setData(
        offers: MutableList<Storefront.SellingPlanEdge>,
        context: Context,
        offercallback: VariantCallback
    ) {
        this.offers = offers
        this.context = context
        variantCallback = offercallback
    }
     fun clickManage(it:View,position: Int,offer_value:String){
        if (it.tag.equals("unselected")) {
            selectedPosition = position
            variantCallback?.clickVariant(
                offers?.get(position)?.node?.name.toString(),
                offer_value!!,
                offers?.get(position)?.node?.id.toString()
            )
            notifyDataSetChanged()
            variant_data?.clear()
            variant_data?.add("")
        }
    }
}
