package com.shopify.rewardifyappmodule

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.shopify.rewardifyappmodule.databinding.ItemTransactionsBinding


class TransactionAdapter constructor() :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {
    private var transactionModel: GetTransactionModel? = null

    fun setData(transactionModel: GetTransactionModel) {
        this.transactionModel = transactionModel
    }

    class TransactionViewHolder : RecyclerView.ViewHolder {
        var ItemTransactionsBinding: ItemTransactionsBinding

        constructor(itemView: ItemTransactionsBinding) : super(itemView.root) {
            this.ItemTransactionsBinding = itemView
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        var view = DataBindingUtil.inflate<ItemTransactionsBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_transactions,
            parent,
            false
        )
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.ItemTransactionsBinding.transactionmodel = transactionModel?.get(position)
    }

    override fun getItemCount(): Int {
        return transactionModel?.size ?: 0
    }
}