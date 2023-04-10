package com.rasmishopping.app.homesection.models

import androidx.recyclerview.widget.RecyclerView
import com.rasmishopping.app.databinding.CollectionCircleBinding
import com.rasmishopping.app.databinding.MCategorygriditemBinding
import com.rasmishopping.app.databinding.MCategoryitemBinding
import com.rasmishopping.app.databinding.MCollectionItemBinding

class CategoryItem : RecyclerView.ViewHolder {
    lateinit var circlebinding: CollectionCircleBinding


    constructor(circlebinding: CollectionCircleBinding) : super(circlebinding.root) {
        this.circlebinding = circlebinding
    }




}