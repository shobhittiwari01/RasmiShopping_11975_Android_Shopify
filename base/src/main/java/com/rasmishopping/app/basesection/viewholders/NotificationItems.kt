package com.rasmishopping.app.basesection.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.rasmishopping.app.databinding.MNotificationlistingBinding

class NotificationItems : RecyclerView.ViewHolder{
    lateinit var binding: MNotificationlistingBinding
    constructor(binding: MNotificationlistingBinding) : super(binding.root) {
        this.binding = binding
    }
}
