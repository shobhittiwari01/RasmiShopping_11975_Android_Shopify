package com.rasmishopping.app.homesection.models

import androidx.recyclerview.widget.RecyclerView
import com.rasmishopping.app.databinding.CategorySquareSliderBinding
import com.rasmishopping.app.databinding.CollectionCircleBinding

class SquareItem : RecyclerView.ViewHolder {
    lateinit var squarebinding: CategorySquareSliderBinding


    constructor(squarebinding: CategorySquareSliderBinding) : super(squarebinding.root) {
        this.squarebinding = squarebinding
    }




}