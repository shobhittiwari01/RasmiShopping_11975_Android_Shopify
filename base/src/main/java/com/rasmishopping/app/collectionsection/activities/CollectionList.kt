package com.rasmishopping.app.collectionsection.activities

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.shopify.buy3.Storefront
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.collectionsection.adapters.CollectionRecylerAdapter
import com.rasmishopping.app.collectionsection.viewmodels.CollectionViewModel
import com.rasmishopping.app.databinding.MCollectionlistBinding
import com.rasmishopping.app.utils.ViewModelFactory
import kotlinx.android.synthetic.main.shimmer_layout_collection_list.*
import javax.inject.Inject

class CollectionList : NewBaseActivity() {
    private var binding: MCollectionlistBinding? = null

    @Inject
    lateinit var factory: ViewModelFactory
    private var model: CollectionViewModel? = null

    @Inject
    lateinit var adapter: CollectionRecylerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val group = findViewById<ViewGroup>(R.id.container)
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.m_collectionlist, group, true)
        shimmerStart()
        showBackButton()
        showTittle(resources.getString(R.string.collection))
        hidethemeselector()
        setLayout(binding!!.categorylist, "collectionvertical")
        (application as MyApplication).mageNativeAppComponent!!.doCollectionInjection(this)
        model = ViewModelProvider(this, factory).get(CollectionViewModel::class.java)
        model!!.context = this
        model!!.Response()
            .observe(this, Observer<List<Storefront.CollectionEdge>> { this.setRecylerData(it) })
        model!!.message.observe(this, Observer<String> { this.showToast(it) })
        binding?.clickHandler = this
    }

    fun clickSearch(view: View) {
        moveToSearch(this)
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    private fun setRecylerData(collections: List<Storefront.CollectionEdge>) {
        try {
            if (collections.size > 0) {
                Log.i("MageNative", "images" + collections.size)
                Log.i("MageNative", "collection id" + collections.get(0).node.id)
                adapter.setData(collections, this)
                binding!!.categorylist.adapter = adapter
                adapter.notifyDataSetChanged()
            } else {
                showToast(resources.getString(R.string.nocollection))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Handler().postDelayed({
            shimmerStop()
        }, 500)
    }

    fun shimmerStart() {
        shimmer_view_container_collection_list.startShimmer()
    }

    override fun shimmerStop() {
        shimmer_view_container_collection_list.stopShimmer()
        shimmer_view_container_collection_list.visibility = View.GONE
        binding!!.categorylist.visibility = View.VISIBLE
        binding!!.searchsection.visibility = View.VISIBLE
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return false
    }
}
