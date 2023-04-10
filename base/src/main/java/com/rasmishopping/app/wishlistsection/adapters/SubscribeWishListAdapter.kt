package com.rasmishopping.app.wishlistsection.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.shopify.buy3.Storefront
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.models.CommanModel
import com.rasmishopping.app.databinding.MWishitemBinding
import com.rasmishopping.app.databinding.SubscribeWishItemBinding
import com.rasmishopping.app.productsection.activities.ProductView
import com.rasmishopping.app.utils.Constant
import com.rasmishopping.app.wishlistsection.models.WishListItem
import com.rasmishopping.app.wishlistsection.viewholders.SubscribeWishItem
import com.rasmishopping.app.wishlistsection.viewholders.WishItem
import com.rasmishopping.app.wishlistsection.viewmodels.SubscribeWishListVIewModel
import com.rasmishopping.app.wishlistsection.viewmodels.WishListViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class SubscribeWishListAdapter@Inject
constructor() : RecyclerView.Adapter<SubscribeWishItem>() {
    var data: MutableList<Storefront.CartLineEdge>? = null
    private var context: Context? = null
    private var layoutInflater: LayoutInflater? = null
    var VariantID: String? = null
    var Position: Int? = null

    companion object {
        var variantCallback: variantCallback? = null
    }

    private var wish_model: SubscribeWishListVIewModel? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubscribeWishItem {
        val binding = DataBindingUtil.inflate<SubscribeWishItemBinding>(
            LayoutInflater.from(parent.context),
            R.layout.subscribe_wish_item,
            parent,
            false
        )
        return SubscribeWishItem(binding)
    }

    override fun onBindViewHolder(holder: SubscribeWishItem, position: Int) {

        val item = WishListItem()
        val model = CommanModel()
        var merchant=data?.get(position)!!.node.merchandise as Storefront.ProductVariant
        item.position = position
        item.product_id = merchant.product.id.toString()
        item.variant_id = merchant.id.toString()
        VariantID = merchant.id.toString()
        Position = position

            if (merchant.sellingPlanAllocations.edges.size > 0) {
                item.selling_price_id =
                    merchant.sellingPlanAllocations.edges[0].node.sellingPlan.id.toString()
            }
            else
            {
                item.selling_price_id=""
            }



        item.productname = merchant.product.title

        val variant = merchant


        model.imageurl = variant?.image?.url
        holder.binding.commondata = model

//        item.image = variant?.image?.originalSrc

        holder.binding.name.textSize = 14f

        holder.binding.variantOne.textSize = 11f
        holder.binding.variantTwo.textSize = 11f
        holder.binding.variantThree.textSize = 11f
        setVariants(item, holder, variant.selectedOptions)
        //val wishdata = WishListItem()
//        Log.i("MageNative : " + data?.get(position)?.title, "" + position)
        holder.binding.movetocart.setTextColor(
            holder.binding.movetocart.context.resources.getColor(
                R.color.colorAccent
            )
        )
        holder.binding.movetocart.textSize = 11f
        holder.binding.name.textSize = 12f
        holder.binding.handler = ClickHandlers()

    }

    private fun setVariants(
        item: WishListItem,
        holder: SubscribeWishItem,
        selectedOptions: List<Storefront.SelectedOption>
    ) {
        try {
            val iterator1 = selectedOptions.iterator()
            var counter = 0
            var option: Storefront.SelectedOption
            while (iterator1.hasNext()) {
                counter = counter + 1
                option = iterator1.next()
                if (!option.value.equals("Default Title", true)) {
                    val finalvalue = option.name + " : " + option.value
                    if (counter == 1) {
                        item.variant_one = finalvalue
                    }
                    if (counter == 2) {
                        item.variant_two = finalvalue
                    }
                    if (counter == 3) {
                        item.variant_three = finalvalue
                    }
                    if (counter > 3) {
                        break
                    }
                }
            }
            holder.binding.variantdata = item
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun getItemCount(): Int {
        return data!!.size
    }

    interface variantCallback {
        fun callback(variantSize: Int)
    }

    fun setData(
        data: MutableList<Storefront.CartLineEdge>,
        context: Context,
        wish_model: SubscribeWishListVIewModel
    ) {
        this.data = data
        this.wish_model = wish_model
        this.context = context
        Log.i("MageNative", "wishcount 2 : " + this.data!!.size)
    }

    inner class ClickHandlers {
        fun removeWishList(view: View, item: WishListItem) {
            try {
//                Log.i("MageNative", "Position : " + position)
                wish_model!!.removeFromWishlist(item)
                data!!.removeAt(Position!!)
                notifyItemRemoved(Position!!)
                notifyItemRangeChanged(Position!!, data!!.size)
                wish_model!!.update(true)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        fun productClick(view: View, data: WishListItem) {
            val productintent = Intent(view.context, ProductView::class.java)
            productintent.putExtra("ID", data.product_id)
            productintent.putExtra("tittle", data.productname)
            productintent.putExtra("Variant_ID", data.variant_id)
            productintent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            productintent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            productintent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            view.context.startActivity(productintent)
            Constant.activityTransition(view.context)
        }

        fun moveToCart(view: View, item: WishListItem) {

            wish_model?.addToCartFromWishlist(item.variant_id.toString(), 1,item.selling_price_id.toString())
            Toast.makeText(context, R.string.success_moved, Toast.LENGTH_SHORT).show()
            removeWishList(view, item)
//            var customQuickAddActivity = QuickAddActivity(context = context!!, activity = context, theme = R.style.WideDialogFull, product_id = product_id, repository = wish_model?.repository!!, wishListViewModel = wish_model, position = position, wishlistData = data)
//            if (variantData.variants.edges.size == 1) {
//                customQuickAddActivity.addToCart(variantData.variants.edges[0].node.id.toString(), 1)
//                //removeWishList(view, product_id, position)
//            } else {
//                customQuickAddActivity.show()
//            }
        }
    }
}
