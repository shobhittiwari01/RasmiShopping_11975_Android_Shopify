package com.rasmishopping.app.productsection.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.DataBindingUtil
import com.shopify.buy3.Storefront
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.basesection.viewmodels.SplashViewModel
import com.rasmishopping.app.databinding.SwatchesListBinding
import com.rasmishopping.app.databinding.VariantpageBinding
import com.rasmishopping.app.productsection.activities.ProductView.Companion.WishlistVariantID
import com.rasmishopping.app.productsection.activities.ProductView.Companion.adapter
import com.rasmishopping.app.productsection.activities.ProductView.Companion.selectedVariants
import com.rasmishopping.app.productsection.activities.ProductView.Companion.totalVariant
import com.rasmishopping.app.productsection.activities.ProductView.Companion.variant_data
import com.rasmishopping.app.productsection.adapters.VariantAdapter
import com.rasmishopping.app.utils.Constant
import kotlinx.android.synthetic.main.swatches_list.view.*

class VariationsActivity: NewBaseActivity() {
    lateinit var variantPageBinding: VariantpageBinding
    @SuppressLint("LogNotTimber")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val group = findViewById<ViewGroup>(R.id.container)
        variantPageBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.variantpage, group, true)
        showBackButton()
        showTittle("")
        hidenavbottom()
        hidethemeselector()
        filterOptionList(ProductView.varproductedge!!.options , ProductView.varproductedge!!.variants.edges)
    }

    private fun filterOptionList(
        options: List<Storefront.ProductOption>,
        edges: MutableList<Storefront.ProductVariantEdge>
    ) {
        var variant_options: MutableList<Storefront.SelectedOption>? = null
        var name = "Name"
        var value = "Value"
        var swatechView: SwatchesListBinding? = null
        var outofStockList: MutableList<String> = mutableListOf()
        for (i in 0 until edges.size) {
            if (!edges.get(i).node.availableForSale) {
                outofStockList.add(edges.get(i).node.title)
            }
            if (WishlistVariantID == edges[i].node.id.toString()) {
                variant_options = edges[i].node.selectedOptions
            }
        }
        totalVariant = options.size
        var variant_pair: MutableMap<String, String> = mutableMapOf()

        if(SplashViewModel.featuresModel.Spinner_Varient==true){
            for (j in 0 until options.size) {
                swatechView =
                    DataBindingUtil.inflate(layoutInflater, R.layout.swatches_list, null, false)
                swatechView.variantTitle.text = options.get(j).name
                adapter = VariantAdapter()
                swatechView.variantList.tag = options.get(j).name
                swatechView.variantList.adapter =
                    ArrayAdapter<String>(
                        this,
                        android.R.layout.simple_list_item_1,
                        options.get(j).values
                    )

                if (!ProductView.selectedvariant_pair.isEmpty()) {
                    for (k in 0 until ProductView.selectedvariant_pair.keys.toList().size) {
                        if (ProductView.selectedvariant_pair.keys.toList().get(k)
                                .equals(options.get(j).name)
                        ) {
                            for (l in 0 until options.get(j).values.size) {
                                if (options.get(j).values.get(l)
                                        .equals(
                                            ProductView.selectedvariant_pair.values.toList().get(k)
                                        )
                                ) {
                                    swatechView.variantList.setSelection(l)
                                    break
                                }
                            }
                        }
                    }
                }

                swatechView.variantList.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            p0: AdapterView<*>?,
                            p1: View?,
                            p2: Int,
                            p3: Long
                        ) {
                            Log.d(
                                "variant",
                                "onItemSelected: " + (p1 as AppCompatTextView).text + "name : " + p0?.tag
                            )
                            variant_pair.put(p0?.tag.toString(), p1.text.toString())
                            if (ProductView.totalVariant == variant_pair.size) {
                            }
                        }

                        override fun onNothingSelected(p0: AdapterView<*>?) {}
                    }
                variantPageBinding.variantContainer.addView(swatechView.root)
            }
        }else {

            Log.d("javed", "filterOptionList: "+ ProductView.selectedvariant_pair)
            if (!ProductView.selectedvariant_pair.isEmpty()) {
                for (k in 0 until ProductView.selectedvariant_pair.values.toList().size) {
                    variant_data?.add(ProductView.selectedvariant_pair.values.toList().get(k))
                    Log.d("javed", "filterOptionList: "+ variant_data)
                }
            }
            else if (variant_options != null) {
                for (i in 0 until variant_options.size) {
                    name = variant_options.get(i).name
                    value = variant_options.get(i).value
                    variant_data?.add(value)
                }
            } else {
                Log.d("javed", "clear1: ")
                variant_data?.add("")
            }

            for (j in 0 until options.size) {
                swatechView =
                    DataBindingUtil.inflate(layoutInflater, R.layout.swatches_list, null, false)
                swatechView.variantTitle.text = options.get(j).name
                swatechView.variantList.visibility = View.GONE
                swatechView.variantListRecyclerView.visibility = View.VISIBLE
                var data=HashMap<String,String>()
                for(k in 0 until options.get(j).values.size){
                    data.put(options.get(j).values.get(k),"true")
                }
                adapter = VariantAdapter()
                adapter.setData(
                    variant_data!!,
                    options.get(j).name,
                    options.get(j).values,
                    outofStockList,
                    this,
                    object : VariantAdapter.VariantCallback {
                        override fun clickVariant(variantName: String, optionName: String) {
                            variant_pair.put(optionName, variantName)
                            if (totalVariant == variant_pair.size) { }
                        }
                    })
                swatechView.variantListRecyclerView.adapter = adapter
                variantPageBinding.variantContainer.addView(swatechView.root)
            }
        }

        variantPageBinding.done.setOnClickListener{
            ProductView.selectedvariant_pair=variant_pair
            selectedVariants = variant_pair
            val returnIntent = Intent()
            setResult(RESULT_OK, returnIntent)
            finish()
        }
    }
}