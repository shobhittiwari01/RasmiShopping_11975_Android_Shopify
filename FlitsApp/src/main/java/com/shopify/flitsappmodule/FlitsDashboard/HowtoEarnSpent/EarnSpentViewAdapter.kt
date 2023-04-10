package com.shopify.flitsappmodule.FlitsDashboard.HowtoEarnSpent

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonArray
import com.shopify.apicall.CurrencyFormatter
import com.shopify.flitsappmodule.R
import org.json.JSONObject


class EarnSpentViewAdapter: RecyclerView.Adapter<EarnSpentViewAdapter.MyViewHolder> {
    private var context: Context
    private var spentearndata: JSONObject

    var Position:Int?=null
    private var Currency: String?=null

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var image_view: ImageView
        var text_title: TextView
        var description: TextView
        var cardView: CardView



        init {
            image_view = view.findViewById(R.id.image_view) as ImageView
            cardView = view.findViewById(R.id.cardview) as CardView
            text_title = view.findViewById(R.id.heading)
            description = view.findViewById(R.id.desc)

        }
    }

    constructor(_context: Context, data: JSONObject,currencycode:String,pos:Int) {
        context = _context
        Position=pos
        Currency=currencycode
        spentearndata = data
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.earnspentdata, parent, false)
        // view.setOnClickListener(MainActivity.myOnClickListener);
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        try {


                var key=(spentearndata.getJSONArray("data").get(position) as JSONObject).getString("tab_to_append")
            var price:String = ""

            if(Position==0)
                    {
                        when(key) {
                            "flits_earning_rules" -> {
                                var module_key = (spentearndata.getJSONArray("data").get(position) as JSONObject).getString("module_on")
                                when (module_key) {
                                    "register" -> {
                                        holder.cardView.visibility=View.VISIBLE
                                        if((spentearndata.getJSONArray("data").get(position) as JSONObject).getString("is_fixed").equals("0"))
                                        {
                                            price =
                                                ((spentearndata.getJSONArray("data").get(position) as JSONObject).getString("credits").toString()
                                                    .toDouble() / 100).toDouble().toString()+"%"

                                        }
                                        else
                                        {
                                            price = CurrencyFormatter.setsymbol(
                                                ((spentearndata.getJSONArray("data").get(position) as JSONObject).getString("credits").toString()
                                                    .toDouble() / 100).toDouble().toString(), Currency!!
                                            )
                                        }
                                        holder.image_view.setImageDrawable(
                                            context.resources.getDrawable(
                                                R.drawable.registerflis
                                            )
                                        )
                                        holder.text_title.text = "Registration Credit"

                                        holder.description.text = "Register and get $price credit"
                                    }
                                    "monthly_date"->
                                    {
                                        holder.cardView.visibility=View.VISIBLE
                                        if((spentearndata.getJSONArray("data").get(position) as JSONObject).getString("is_fixed").equals("0"))
                                        {
                                            price =
                                                ((spentearndata.getJSONArray("data").get(position) as JSONObject).getString("credits").toString()
                                                    .toDouble() / 100).toDouble().toString()

                                        }
                                        else
                                        {
                                            price = CurrencyFormatter.setsymbol(
                                                ((spentearndata.getJSONArray("data").get(position) as JSONObject).getString("credits").toString()
                                                    .toDouble() / 100).toDouble().toString(), Currency!!
                                            )
                                        }
                                        holder.image_view.setImageDrawable(
                                            context.resources.getDrawable(
                                                R.drawable.calendar
                                            )
                                        )
                                        var date =(spentearndata.getJSONArray("data").get(position) as JSONObject).getString("column_value").split("/").toTypedArray()
                                        var day=date[2]
                                        holder.text_title.text = "Monthly Credit"

                                        holder.description.text = "you will get $$price credit on $day of every month"
                                    }
                                    "birthdate"->{

                                        holder.cardView.visibility=View.VISIBLE
                                        if((spentearndata.getJSONArray("data").get(position) as JSONObject).getString("is_fixed").equals("0"))
                                        {
                                            price =
                                                ((spentearndata.getJSONArray("data").get(position) as JSONObject).getString("credits").toString()
                                                    .toDouble() / 100).toDouble().toString()+"%"

                                        }
                                        else
                                        {
                                            price = CurrencyFormatter.setsymbol(
                                                ((spentearndata.getJSONArray("data").get(position) as JSONObject).getString("credits").toString()
                                                    .toDouble() / 100).toDouble().toString(), Currency!!
                                            )
                                        }
                                        holder.image_view.setImageDrawable(
                                            context.resources.getDrawable(
                                                R.drawable.bday
                                            )
                                        )
                                        holder.text_title.text = "Birthday Credit"

                                        holder.description.text = "Share your birthday with us and get $price credit on your birthday "+"\n"+"\n"+
                                        "*you can avail this credit only once in a year"
                                    }
                                    "referrer_friend"->{
                                        if((spentearndata.getJSONArray("data").get(position) as JSONObject).getString("is_fixed").equals("0"))
                                        {
                                            price =
                                                ((spentearndata.getJSONArray("data").get(position) as JSONObject).getString("credits").toString()
                                                    .toDouble() / 100).toDouble().toString()+"%"

                                        }
                                        else
                                        {
                                            price = CurrencyFormatter.setsymbol(
                                                ((spentearndata.getJSONArray("data").get(position) as JSONObject).getString("credits").toString()
                                                    .toDouble() / 100).toDouble().toString(), Currency!!
                                            )
                                        }
                                        holder.image_view.setImageDrawable(
                                            context.resources.getDrawable(
                                                R.drawable.referfren
                                            )
                                        )
                                        holder.text_title.text = "Refferal Program"

                                        holder.description.text = "Invite your friends and get $price credit when they sign up."
                                    }
                                    "referrals_total_number"->{
                                        if((spentearndata.getJSONArray("data").get(position) as JSONObject).getString("is_fixed").equals("0"))
                                        {
                                            price =
                                                ((spentearndata.getJSONArray("data").get(position) as JSONObject).getString("credits").toString()
                                                    .toDouble() / 100).toDouble().toString()+"%"

                                        }
                                        else
                                        {
                                            price = CurrencyFormatter.setsymbol(
                                                ((spentearndata.getJSONArray("data").get(position) as JSONObject).getString("credits").toString()
                                                    .toDouble() / 100).toString(), Currency!!
                                            )
                                        }
                                        holder.image_view.setImageDrawable(
                                            context.resources.getDrawable(
                                                R.drawable.referppoint
                                            )
                                        )
                                        holder.text_title.text = "Credit on number of refferals"

                                        holder.description.text = "When you reach 1 referrals you get  $price credit. "
                                    }
                                    "product_review"->{

                                        holder.cardView.visibility=View.VISIBLE
                                        if((spentearndata.getJSONArray("data").get(position) as JSONObject).getString("is_fixed").equals("0"))
                                        {
                                            price =
                                                ((spentearndata.getJSONArray("data").get(position) as JSONObject).getString("credits").toString()
                                                    .toDouble() / 100).toDouble().toString()+"%"

                                        }
                                        else
                                        {
                                            price = CurrencyFormatter.setsymbol(
                                                ((spentearndata.getJSONArray("data").get(position) as JSONObject).getString("credits").toString()
                                                    .toDouble() / 100).toDouble().toString(), Currency!!
                                            )
                                        }
                                        holder.image_view.setImageDrawable(
                                            context.resources.getDrawable(
                                                R.drawable.reviews
                                            )
                                        )
                                        holder.text_title.text = "Product Review Credit"

                                        holder.description.text = "write a review and get $price credit."
                                    }

                                    "product_tag"->{

                                        holder.cardView.visibility=View.VISIBLE
                                        if((spentearndata.getJSONArray("data").get(position) as JSONObject).getString("is_fixed").equals("0"))
                                        {
                                            price = ((spentearndata.getJSONArray("data").get(position) as JSONObject).getString("credits").toString()
                                                    .toDouble() / 100).toDouble().toString()+"%"

                                        }
                                        else
                                        {
                                            price = CurrencyFormatter.setsymbol(
                                                ((spentearndata.getJSONArray("data").get(position) as JSONObject).getString("credits").toString()
                                                    .toDouble() / 100).toDouble().toString(), Currency!!
                                            )
                                        }
                                        holder.image_view.setImageDrawable(
                                            context.resources.getDrawable(
                                                R.drawable.flitstag
                                            )
                                        )


                                        var avails=(spentearndata.getJSONArray("data").get(position) as JSONObject).getJSONArray("avails").get(0)
                                        holder.text_title.text = "Credit for specific product collection"

                                        holder.description.text = "Buy product's with (any tag)$avails and get $price credit."
                                    }

                                    "subscribe" -> {
                                        holder.cardView.visibility=View.VISIBLE
                                        if((spentearndata.getJSONArray("data").get(position) as JSONObject).getString("is_fixed").equals("0"))
                                        {
                                            price =
                                                ((spentearndata.getJSONArray("data").get(position) as JSONObject).getString("credits").toString()
                                                    .toDouble()/ 100).toDouble().toString()+"%"

                                        }
                                        else
                                        {
                                            price = CurrencyFormatter.setsymbol(
                                                ((spentearndata.getJSONArray("data").get(position) as JSONObject).getString("credits").toString()
                                                    .toDouble() / 100).toDouble().toString(), Currency!!
                                            )
                                        }
                                        holder.image_view.setImageDrawable(
                                            context.resources.getDrawable(
                                                R.drawable.subscriptionflits
                                            )
                                        )
                                        holder.text_title.text = "Subscriber credit"

                                        holder.description.text = "Subscribe and get $price credit"
                                    }
                                    "order_number"->{
                                        holder.cardView.visibility=View.VISIBLE
                                        if((spentearndata.getJSONArray("data").get(position) as JSONObject).getString("is_fixed").equals("0"))
                                        {
                                            price =
                                                ((spentearndata.getJSONArray("data").get(position) as JSONObject).getString("credits").toString()
                                                    .toDouble() / 100).toDouble().toString()+"%"

                                        }
                                        else
                                        {
                                            price = CurrencyFormatter.setsymbol(
                                                ((spentearndata.getJSONArray("data").get(position) as JSONObject).getString("credits").toString()
                                                    .toDouble() / 100).toDouble().toString(), Currency!!
                                            )
                                        }
                                        if((spentearndata.getJSONArray("data").get(position) as JSONObject).getString("relation")== "==") {
                                            holder.image_view.setImageDrawable(
                                                context.resources.getDrawable(
                                                    R.drawable.orderflits
                                                )
                                            )
                                            var orderno= (spentearndata.getJSONArray("data").get(position) as JSONObject).getString("column_value")
                                            holder.text_title.text = "Credit on specific order"

                                            holder.description.text = "Earn $price credit on your order number $orderno"
                                        }
                                        else
                                        {
                                            holder.cardView.visibility=View.VISIBLE
                                            holder.image_view.setImageDrawable(
                                                context.resources.getDrawable(
                                                    R.drawable.orderflits
                                                )
                                            )
                                            var orderno= (spentearndata.getJSONArray("data").get(position) as JSONObject).getString("column_value")
                                            holder.text_title.text = "credit on order number $orderno and next orders"

                                            holder.description.text = "you can earn $price credit on order number $orderno and next orders 3,4.....n"
                                        }

                                    }
                                    "add_product_to_wishlist"->{
                                        holder.cardView.visibility=View.VISIBLE
                                        if((spentearndata.getJSONArray("data").get(position) as JSONObject).getString("is_fixed").equals("0"))
                                        {
                                            price =
                                                ((spentearndata.getJSONArray("data").get(position) as JSONObject).getString("credits").toString()
                                                    .toDouble()/ 100).toDouble().toString()+"%"

                                        }
                                        else
                                        {
                                            price = CurrencyFormatter.setsymbol(
                                                ((spentearndata.getJSONArray("data").get(position) as JSONObject).getString("credits").toString()
                                                    .toDouble() / 100).toDouble().toString(), Currency!!
                                            )
                                        }
                                        holder.image_view.setImageDrawable(
                                            context.resources.getDrawable(
                                                R.drawable.wishlistflits
                                            )
                                        )
                                        holder.text_title.text = "Wishlisted products credit"

                                        holder.description.text = "you can earn $price credit when you add products in wishlist."

                                    }
                                    "referrals_order_number"->{
                                        holder.cardView.visibility=View.VISIBLE
                                        if((spentearndata.getJSONArray("data").get(position) as JSONObject).getString("is_fixed").equals("0"))
                                        {
                                            price =
                                                ((spentearndata.getJSONArray("data").get(position) as JSONObject).getString("credits").toString()
                                                    .toDouble() / 100).toDouble().toString()+"%"

                                        }
                                        else
                                        {
                                            price = CurrencyFormatter.setsymbol(
                                                ((spentearndata.getJSONArray("data").get(position) as JSONObject).getString("credits").toString()
                                                    .toDouble() / 100).toDouble().toString(), Currency!!
                                            )
                                        }
                                        if((spentearndata.getJSONArray("data").get(position) as JSONObject).getString("relation")== "==") {
                                            holder.image_view.setImageDrawable(
                                                context.resources.getDrawable(
                                                    R.drawable.referred_order
                                                )
                                            )
                                          
                                            holder.text_title.text = "Credit on number of referred order"

                                            holder.description.text = "Earn $price credit on your order "

                                        }
                                        else
                                        {
                                            holder.cardView.visibility=View.VISIBLE
                                            holder.image_view.setImageDrawable(
                                                context.resources.getDrawable(
                                                    R.drawable.referred_order
                                                )
                                            )

                                            holder.text_title.text = "Credit on number of referred order"

                                            holder.description.text ="Earn $price credit on your n number of orders "
                                        }

                                    }



                                }

                            }
                        }


                    }
                else
                    {
                        when(key) {
                        "flits_spent_rules" -> {

                            var price:String = ""
                            val module_key =(spentearndata.getJSONArray("data").get(position) as JSONObject).getString("module_on")

                            when (module_key) {

                                "cart" -> {
                                    if ((spentearndata.getJSONArray("data")
                                            .get(position) as JSONObject).getString("column_value")
                                            .contains("-")
                                    ) {
                                        holder.cardView.visibility = View.VISIBLE
                                        if ((spentearndata.getJSONArray("data")
                                                .get(position) as JSONObject).getString("is_fixed")
                                                .equals("0")
                                        ) {
                                            price =
                                                ((spentearndata.getJSONArray("data")
                                                    .get(position) as JSONObject).getString("credits")
                                                    .toString()
                                                    .toDouble() / 100.0f).toString().toString() + "%"

                                        } else {
                                            price = CurrencyFormatter.setsymbol(
                                                ((spentearndata.getJSONArray("data")
                                                    .get(position) as JSONObject).getString("credits")
                                                    .toString()
                                                    .toDouble() / 100.0f).toString().toString(),
                                                Currency!!
                                            )
                                        }
                                        var relation_min_value = CurrencyFormatter.setsymbol(
                                            (spentearndata.getJSONArray("data")
                                                .get(position) as JSONObject).getString("column_value")
                                                .split(":")[0], Currency!!
                                        )

                                        holder.image_view.setImageDrawable(
                                            context.resources.getDrawable(
                                                R.drawable.cartflits
                                            )
                                        )
                                        holder.text_title.text = "Spend on cart"

                                        holder.description.text =
                                            "your cart value is between $relation_min_value or more.Congratulations you are eligible to use $price credit"

                                    }
                                    else
                                    {
                                        holder.cardView.visibility = View.VISIBLE
                                        if ((spentearndata.getJSONArray("data")
                                                .get(position) as JSONObject).getString("is_fixed")
                                                .equals("0")
                                        ) {
                                            price =
                                                ((spentearndata.getJSONArray("data")
                                                    .get(position) as JSONObject).getString("credits")
                                                    .toString()
                                                    .toDouble()/ 100.0f).toString().toString() + "%"

                                        } else {
                                            price = CurrencyFormatter.setsymbol(
                                                ((spentearndata.getJSONArray("data")
                                                    .get(position) as JSONObject).getString("credits")
                                                    .toString()
                                                    .toDouble() / 100.0f).toString().toString(),
                                                Currency!!
                                            )
                                        }
                                        var relation_min_value = CurrencyFormatter.setsymbol(
                                            (spentearndata.getJSONArray("data")
                                                .get(position) as JSONObject).getString("column_value")
                                                .split(":")[0], Currency!!
                                        )
                                        var relation_max_value= CurrencyFormatter.setsymbol(
                                            (spentearndata.getJSONArray("data")
                                                .get(position) as JSONObject).getString("column_value")
                                                .split(":")[1], Currency!!
                                        )
                                        holder.image_view.setImageDrawable(
                                            context.resources.getDrawable(
                                                R.drawable.cartflits
                                            )
                                        )
                                        holder.text_title.text = "Spend on cart"

                                        holder.description.text =
                                            "your cart value is between $relation_min_value or $relation_max_value.Congratulations you are eligible to use $price credit"
                                    }
                                }
                            }
                                }


                            }
                    }









//            holder.itemView.setOnClickListener(object : OnClickListener() {
//                fun onClick(view: View?) {
////                    val intent = Intent(context, DetailActivity::class.java)
////                    intent.putExtra("dataSet", dataModel[position])
////                    //ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(context, holder.image_view, "robot");
////                    context.startActivity(intent)
//                }
//            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getImage(imageName: String): Int {
        return context.getResources()
            .getIdentifier(imageName, "drawable", context.getPackageName())
    }

    override fun getItemCount(): Int {
        return spentearndata.getJSONArray("data").length()
    }
}