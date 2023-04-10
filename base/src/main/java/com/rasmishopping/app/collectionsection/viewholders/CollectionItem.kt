package com.rasmishopping.app.collectionsection.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.rasmishopping.app.R
import com.rasmishopping.app.databinding.*

class CollectionItem : RecyclerView.ViewHolder {
    lateinit var gridbinding: MCategorygriditemBinding
    lateinit var gridbindings: MCategorygriditemsBinding
    lateinit var binding: MCategoryitemBinding
    lateinit var collectionbinding: MCollectionItemBinding
    lateinit var collectionbindings: MCollectionItemsBinding
    lateinit var collectionbindingSubMenu: MSubMenuTitleBinding
    constructor(gridbinding: MCategorygriditemBinding) : super(gridbinding.root) {
        this.gridbinding = gridbinding
    }
    constructor(gridbindings: MCategorygriditemsBinding) : super(gridbindings.root) {
        this.gridbindings = gridbindings
    }

    constructor(binding: MCategoryitemBinding) : super(binding.root) {
        this.binding = binding
    }

    constructor(collectionbinding: MCollectionItemBinding) : super(collectionbinding.root) {
        this.collectionbinding = collectionbinding
    }
    constructor(collectionbindings: MCollectionItemsBinding) : super(collectionbindings.root) {
        this.collectionbindings = collectionbindings
    }
    constructor(collectionbindingSubMenu: MSubMenuTitleBinding) : super(collectionbindingSubMenu.root) {
        this.collectionbindingSubMenu = collectionbindingSubMenu
    }
}
