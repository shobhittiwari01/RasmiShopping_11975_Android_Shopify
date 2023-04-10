package com.rasmishopping.app.ordersection.adapters
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.shopify.buy3.Storefront
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.models.CommanModel
import com.rasmishopping.app.databinding.OrderListItemBinding
import com.rasmishopping.app.productsection.activities.ProductView
import com.rasmishopping.app.utils.Constant
import javax.inject.Inject
class OrderDetailsListAdapter @Inject constructor() :
    RecyclerView.Adapter<OrderDetailsListAdapter.OrderDetailsListViewHolder>() {
    private var orderlineItem: List<Storefront.OrderLineItemEdge>? = null
    fun setData(orderlineItem: List<Storefront.OrderLineItemEdge>?) {
        this.orderlineItem = orderlineItem
    }
    class OrderDetailsListViewHolder : RecyclerView.ViewHolder {
        var orderListItemBinding: OrderListItemBinding
        constructor(itemView: OrderListItemBinding) : super(itemView.root) {
            this.orderListItemBinding = itemView
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderDetailsListViewHolder {
        var view = DataBindingUtil.inflate<OrderListItemBinding>(
            LayoutInflater.from(parent.context),
            R.layout.order_list_item,
            parent,
            false
        )
        return OrderDetailsListViewHolder(view)
    }
    override fun getItemCount(): Int {
        return orderlineItem?.size ?: 0
    }
    override fun onBindViewHolder(holder: OrderDetailsListViewHolder, position: Int) {
        var commanModel = CommanModel()
        commanModel.imageurl = orderlineItem?.get(position)?.node?.variant?.image?.url
        holder.orderListItemBinding.commondata = commanModel
        holder.orderListItemBinding.productName.text = orderlineItem?.get(position)?.node?.title
        holder.orderListItemBinding.productquantityTxt.text =
            orderlineItem?.get(position)?.node?.quantity.toString()
        var variantTitle = StringBuffer()
        for (i in 0 until orderlineItem?.get(position)?.node?.variant?.selectedOptions?.size!!) {
            if (variantTitle.length > 0) {
                variantTitle.append("/")
            }
            variantTitle.append(orderlineItem?.get(position)?.node?.variant?.selectedOptions?.get(i)?.value)
        }
        holder.orderListItemBinding.productvariant.text = variantTitle.toString()
        holder?.orderListItemBinding.itemImage.setOnClickListener {

            val productintent = Intent(it.context, ProductView::class.java)
            productintent.putExtra("ID",  orderlineItem?.get(position)?.node?.variant?.product?.id.toString())
            productintent.putExtra("tittle", orderlineItem?.get(position)?.node?.variant?.product?.title)
            productintent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            productintent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            productintent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            it.context.startActivity(productintent)
            Constant.activityTransition(it.context)
        }
    }
}