package com.rasmishopping.app.basesection.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.rasmishopping.app.databinding.MCircleitemBinding

class CircleData : RecyclerView.ViewHolder {
    lateinit var binding: MCircleitemBinding

    constructor(binding: MCircleitemBinding) : super(binding.root) {
        this.binding = binding
    }
}
