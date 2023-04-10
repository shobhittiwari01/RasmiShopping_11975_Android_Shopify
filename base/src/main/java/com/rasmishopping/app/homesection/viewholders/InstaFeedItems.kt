package com.rasmishopping.app.homesection.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.rasmishopping.app.databinding.MInstafeeditemBinding

class InstaFeedItems : RecyclerView.ViewHolder {
    var binding: MInstafeeditemBinding? = null
    constructor(binding: MInstafeeditemBinding) : super(binding.root) {
        this.binding = binding
    }
}
