package com.rasmishopping.app.basesection.adapters
import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.Weblink
import com.rasmishopping.app.basesection.models.CommanModel
import com.rasmishopping.app.basesection.models.Notification
import com.rasmishopping.app.basesection.viewholders.NotificationItems
import com.rasmishopping.app.databinding.MNotificationlistingBinding
import com.rasmishopping.app.productsection.activities.ProductList
import com.rasmishopping.app.productsection.activities.ProductView
import org.json.JSONObject
import javax.inject.Inject
class NotificationListAdapter @Inject
constructor() : RecyclerView.Adapter<NotificationItems>() {
    private var activity: Activity? = null
    var tag: String = "noexpand"
    var discarr = ArrayList<Notification>()
    var selectedPosition = -1
    fun setData(activity: Activity, discarr: ArrayList<Notification>) {
        this.activity = activity
        this.discarr = discarr
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationItems {
        var listbinding = DataBindingUtil.inflate<MNotificationlistingBinding>(
            LayoutInflater.from(parent.context),
            R.layout.m_notificationlisting,
            parent,
            false
        )
        return NotificationItems(listbinding)
    }

    override fun onBindViewHolder(holder: NotificationItems, position: Int) {
        holder.binding.clickproduct = Product(holder, position,discarr.get(position).notification_data!!)
        var notificationtitle = JSONObject(discarr.get(position).notification_data)
        holder.binding.titlesection.text = notificationtitle.getString("title")
        holder.binding.messagesection.text = notificationtitle.getString("message")
        var common=CommanModel()
        if (notificationtitle.has("image")) {
            if (notificationtitle.getString("image").isEmpty()) {
                holder.binding.arrowsection.visibility = View.GONE
                holder.binding.imagesection.visibility=View.GONE
            } else {
                holder.binding.arrowsection.visibility = View.VISIBLE
                common.imageurl=notificationtitle.getString("image")
               /* if (selectedPosition>0&&selectedPosition==position){
                    if (holder.binding.imagesection.isVisible){
                        holder.binding.imagesection.visibility=View.GONE
                        holder.binding.expandCollapse.setImageResource(R.drawable.ic_arrow_down_icon)
                        holder.binding.messagesection.maxLines= Int.MAX_VALUE
                    }else{
                        holder.binding.imagesection.visibility=View.VISIBLE
                        holder.binding.expandCollapse.setImageResource(R.drawable.ic_arrow_up_icon)
                        holder.binding.messagesection.maxLines=2
                    }
                }else{
                    holder.binding.imagesection.visibility=View.GONE
                }*/
            }
        }else{
            holder.binding.imagesection.visibility=View.GONE
        }
        if(common.imageurl.isNullOrEmpty()){
            common.imageurl=""
        }
        holder.binding.commondata=common
    }
    override fun getItemCount(): Int {
        return discarr.size
    }
    inner class Product(var holder: NotificationItems, var position: Int, var notification:String) {
        fun productClick() {
            selectedPosition = position
            if (holder.binding.imagesection.isVisible){
                holder.binding.imagesection.visibility=View.GONE
                holder.binding.expandCollapse.setImageResource(R.drawable.ic_arrow_down_icon)
                holder.binding.messagesection.maxLines= Int.MAX_VALUE
            }else{
                holder.binding.imagesection.visibility=View.VISIBLE
                holder.binding.expandCollapse.setImageResource(R.drawable.ic_arrow_up_icon)
                holder.binding.messagesection.maxLines=2
            }
        }
        fun notificationClick(){
            var notificationtitle = JSONObject(notification)
            val link_type = notificationtitle.getString("link_type")
            val link_id = notificationtitle.getString("link_id")
            var resultIntent: Intent? = null
            when (link_type) {
                "product" -> {
                    val product_id = link_id
                    resultIntent = Intent(activity, ProductView::class.java)
                    resultIntent.putExtra("ID", "gid://shopify/Product/"+product_id)
                    resultIntent.putExtra("type", "product")
                    activity!!.startActivity(resultIntent)
                }
                "collection" -> {
                    val s1 = "gid://shopify/Collection/"+link_id
                    resultIntent = Intent(activity, ProductList::class.java)
                    resultIntent.putExtra("ID", s1)
                    resultIntent.putExtra("tittle", "")
                    resultIntent.putExtra("type", "collection")
                    activity!!.startActivity(resultIntent)
                }
                "web_address", "web_link" -> {
                    resultIntent = Intent(activity, Weblink::class.java)
                    resultIntent.putExtra("link", link_id)
                    resultIntent.putExtra("name", " ")
                    resultIntent.putExtra("type", "weblink")
                    activity!!.startActivity(resultIntent)
                }
            }
        }
    }
}
