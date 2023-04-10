package com.shopify.apicall.StoreCredits


import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.DrawableCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.shopify.apicall.CurrencyFormatter
import com.shopify.flitsappmodule.R
import com.shopify.flitsappmodule.databinding.TransactionHistoryLayoutFileBinding
import org.json.JSONArray
import org.json.JSONObject


class StoreCreditTransactionAdapter :
    RecyclerView.Adapter<StoreCreditTransactionAdapter.StoreRewardsViewHolder>() {
    var credit_log_data: JSONArray = JSONArray()
    private var layoutInflater: LayoutInflater? = null
    var context:Context?=null
    var currency:String?=null
    fun setData(credit_log_data: JSONArray,context:Context,currency:String) {

        this.credit_log_data = credit_log_data
        this.context=context
        this.currency=currency

    }

    class StoreRewardsViewHolder : RecyclerView.ViewHolder {
        var binding: TransactionHistoryLayoutFileBinding

        constructor(itemView: TransactionHistoryLayoutFileBinding) : super(itemView.root) {
            this.binding = itemView
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreRewardsViewHolder {
        layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<TransactionHistoryLayoutFileBinding>(
            layoutInflater!!,
            R.layout.transaction_history_layout_file, parent, false
        )
        return StoreRewardsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoreRewardsViewHolder, position: Int) {
        if ((credit_log_data.get(position) as JSONObject).getString("credits").toInt() > 0) {
            holder.binding.debitCreditAmt.text =CurrencyFormatter.setsymbol(
                ((credit_log_data.get(position) as JSONObject).getString("credits")
                    .toDouble() / 100).toString(),currency!!
            )

            holder.binding.plus.visibility = View.VISIBLE

        } else {
            holder.binding.debitCreditAmt.text =CurrencyFormatter.setsymbol(
                ((credit_log_data.get(position) as JSONObject).getString("credits")
                    .toDouble() / 100).toString(),currency!!
            )

            holder.binding.plus.setImageDrawable(context?.getDrawable(R.drawable.minus))
            val backgroundDrawable = DrawableCompat.wrap( holder.binding.plus.drawable).mutate()
            DrawableCompat.setTint(backgroundDrawable, Color.RED)


        }

        holder.binding.summary.text =
            (credit_log_data.get(position) as JSONObject).getString("comment")
        holder.binding.date.text =
            (credit_log_data.get(position) as JSONObject).getString("created_at")

    }


    override fun getItemCount(): Int {
        return credit_log_data.length()
    }
}