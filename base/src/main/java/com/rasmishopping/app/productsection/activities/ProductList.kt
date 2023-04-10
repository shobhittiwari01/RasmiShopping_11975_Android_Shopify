package com.rasmishopping.app.productsection.activities

import BrandFilters
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.transition.Fade
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.shopify.buy3.Storefront
import com.rasmishopping.app.FlitsDashboard.WishlistSection.FlitsWishlistViewModel
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.basesection.fragments.LeftMenu
import com.rasmishopping.app.basesection.models.MenuData
import com.rasmishopping.app.basesection.viewmodels.SplashViewModel
import com.rasmishopping.app.cartsection.activities.CartList
import com.rasmishopping.app.cartsection.activities.SubscribeCartList
import com.rasmishopping.app.databinding.MProductlistitemBinding
import com.rasmishopping.app.databinding.SortDialogLayoutBinding
import com.rasmishopping.app.homesection.activities.HomePage
import com.rasmishopping.app.homesection.viewmodels.HomePageViewModel
import com.rasmishopping.app.productsection.adapters.ProductRecyclerListAdapter
import com.rasmishopping.app.productsection.adapters.ProductRecylerGridAdapter
import com.rasmishopping.app.productsection.adapters.SubCategoryAdapter
import com.rasmishopping.app.productsection.viewmodels.ProductListModel
import com.rasmishopping.app.sharedprefsection.MagePrefs
import com.rasmishopping.app.utils.Constant
import com.rasmishopping.app.utils.SpacesItemDecoration
import com.rasmishopping.app.utils.ViewModelFactory
import kotlinx.android.synthetic.main.m_productmain.*
import kotlinx.android.synthetic.main.m_productmain.view.*
import kotlinx.android.synthetic.main.subcategory_item_layout.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.HashMap
import javax.inject.Inject

class ProductList : NewBaseActivity() {
    private var binding: MProductlistitemBinding? = null
    private var productlist: RecyclerView? = null
    private var myRecycler: RecyclerView? = null
    @Inject
    lateinit var factory: ViewModelFactory
    var productListModel: ProductListModel? = null
    private var flistwishmodel: FlitsWishlistViewModel? = null
    private var products: MutableList<Storefront.ProductEdge>? = null
    private var listEnabled: Boolean = false
    var product_id: String? = null
    var subCategoryAdapter = SubCategoryAdapter()
    var subcategory_array: JSONArray? = null
    @Inject
    lateinit var product_grid_adapter: ProductRecylerGridAdapter
    @Inject
    lateinit var product_list_adapter: ProductRecyclerListAdapter
    private var flag = true
    private var filter_by: LinearLayout? = null
    private var handle: String? = null
    private var isfirsttimeloaded = false
    private var currentsorting = "ntoo"
    var tag_list = ArrayList<String>()
    /*init {
        updateconfig(this)
    }*/
    //  @Inject lateinit var product_filter_adapter: ProductFilterRecylerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val group = findViewById<ViewGroup>(R.id.container)
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.m_productlistitem, group, true)
        filter_by = binding!!.root.findViewById(R.id.filtersection)
        productlist = setLayout(binding!!.root.findViewById(R.id.productlist), "grid")
        productlist!!.addItemDecoration(SpacesItemDecoration(15, 25))
        myRecycler = setLayout(binding!!.root.findViewById(R.id.subcategory_recycler), "horizontal")
        showBackButton()
        hidenavbottom()
        hidethemeselector()
        //Shimmer
        shimmerStartGridProductList()
        CoroutineScope(Dispatchers.Main).launch {
            applyColors()
        }
        if (intent.getSerializableExtra("menudata") != null) {
            myRecycler!!.visibility = View.VISIBLE
            var menuData = intent.getSerializableExtra("menudata") as MenuData?
            subCategoryAdapter.setSubCatRecylerData(menuData!!.menuitems!!, this)
            myRecycler!!.adapter = subCategoryAdapter
        }
        (application as MyApplication).mageNativeAppComponent!!.doProductListInjection(this)
        productListModel = ViewModelProvider(this, factory).get(ProductListModel::class.java)
        productListModel!!.context = this
        flistwishmodel = ViewModelProvider(this, factory).get(FlitsWishlistViewModel::class.java)
        flistwishmodel!!.context = this
        productListModel?.collectionData?.observe(this, Observer { this.collectionResponse(it) })
        /***************** Intent for Tittle *********************/
        if (intent.hasExtra("tittle") && intent.getStringExtra("tittle") != null) {
            showTittle(intent.getStringExtra("tittle") ?: "")
        }
        /***************** Intent for ProductsByID *********************/
        if (intent.getStringExtra("ID") != null) {
            productListModel!!.setcategoryID(intent.getStringExtra("ID")!!)
            product_id = intent.getStringExtra("ID")
            listEnabled = false
            productlist = setLayout(binding!!.root.findViewById(R.id.productlist), "grid")
            setMargins(productlist!!, 30, 0, 30, 0)
            binding?.mainview?.grid_but?.setImageResource(R.drawable.gridiconselected)
            binding?.mainview?.list_but?.setImageResource(R.drawable.listicon)
            productlist!!.visibility = View.GONE
            //binding?.mainview?.productListContainer?.visibility = View.GONE
            Log.i("RECEIVEDHANDLE", "" + intent.getStringExtra("ID")!!)
        }
        /***************** Intent for ProductsByHandle *********************/
        if (intent.getStringExtra("handle") != null) {
            productListModel!!.setcategoryHandle(intent.getStringExtra("handle")!!)
            Log.i("RECEIVEDHANDLE", "" + intent.getStringExtra("handle")!!)
            handle = intent.getStringExtra("handle")
            listEnabled = false
            productlist = setLayout(binding!!.root.findViewById(R.id.productlist), "grid")
            setMargins(productlist!!, 30, 0, 30, 0)
            binding?.mainview?.grid_but?.setImageResource(R.drawable.gridiconselected)
            binding?.mainview?.list_but?.setImageResource(R.drawable.listicon)
            productlist!!.visibility = View.GONE
            // binding?.mainview?.productListContainer?.visibility = View.GONE
            Log.i("RECEIVEDHANDLE", "" + intent.getStringExtra("ID")!!)
        }
        /***************** Intent for switching to listview *********************/
        if (intent.getStringExtra("List_Product_Id") != null) {
            if (intent.getStringExtra("type").equals("handle")) {
                productListModel!!.setcategoryHandle(intent.getStringExtra("List_Product_Id")!!)
                handle = intent.getStringExtra("List_Product_Id")
            }
            if (intent.getStringExtra("type").equals("product_id")) {
                productListModel!!.setcategoryID(intent.getStringExtra("List_Product_Id")!!)
                product_id = intent.getStringExtra("List_Product_Id")
            }
            if (intent.getStringExtra("type").equals("allproduct")) {
                productListModel!!.shopID = "allproduct"
                flag = false
            }
//            if (intent.hasExtra("menu") && intent.getStringExtra("menu") != null) {
//                //Toast.makeText(this,"Cat not found",Toast.LENGTH_LONG).show()
//                Log.e("SUBCAT", "" + intent.getStringExtra("menu"))
//                subcategory_array = JSONArray(intent.getStringExtra("menu"))
//                subCategoryAdapter.setSubCatRecylerData(subcategory_array!!, this@ProductList)
//                myRecycler!!.adapter = subCategoryAdapter
//                myRecycler!!.visibility = View.VISIBLE
//            }
//            else {
//                myRecycler!!.visibility = View.GONE
//            }
            // productListModel!!.setcategoryID(intent.getStringExtra("List_Product_Id")!!)
            productlist?.layoutManager = LinearLayoutManager(this)
            setMargins(productlist!!, 0, 15, 0, 15)
            binding?.mainview?.grid_but?.setImageResource(R.drawable.gridicon)
            binding?.mainview?.list_but?.setImageResource(R.drawable.square)
            productlist!!.visibility = View.GONE
            // binding?.mainview?.productListContainer?.visibility = View.GONE
            listEnabled = true
        } else {
            /***************** Intent for loading all products *********************/
            if (intent.getStringExtra("ID") == null && intent.getStringExtra("handle") == null) {
                productListModel!!.shopID = "allproduct"
                flag = false
                listEnabled = false
                productlist = setLayout(binding!!.root.findViewById(R.id.productlist), "grid")
                setMargins(productlist!!, 30, 0, 30, 0)

                productlist!!.visibility = View.GONE
                //   binding?.mainview?.productListContainer?.visibility = View.GONE
            }
        }
        /***************** Intent for sorting *********************/
        if (intent.getStringExtra("SortType") != null) {
            currentsorting = intent.getStringExtra("SortType")!!
            when (intent.getStringExtra("SortType")) {
                "atoz" -> {
                    if (flag) {
                        productListModel!!.sortKeys = Storefront.ProductCollectionSortKeys.TITLE
                    } else {
                        productListModel!!.keys = Storefront.ProductSortKeys.TITLE
                    }
                    productListModel!!.isDirection = false
                }
                "ztoa" -> {
                    if (flag) {
                        productListModel!!.sortKeys = Storefront.ProductCollectionSortKeys.TITLE
                    } else {
                        productListModel!!.keys = Storefront.ProductSortKeys.TITLE
                    }
                    productListModel!!.isDirection = true
                }
                "htol" -> {

                    if (flag) {
                        productListModel!!.sortKeys = Storefront.ProductCollectionSortKeys.PRICE
                    } else {
                        productListModel!!.keys = Storefront.ProductSortKeys.PRICE
                    }
                    productListModel!!.isDirection = true
                }
                "ltoh" -> {

                    if (flag) {
                        productListModel!!.sortKeys = Storefront.ProductCollectionSortKeys.PRICE
                    } else {
                        productListModel!!.keys = Storefront.ProductSortKeys.PRICE
                    }
                    productListModel!!.isDirection = false
                }
                "oton" -> {
                    if (flag) {
                        productListModel!!.sortKeys = Storefront.ProductCollectionSortKeys.CREATED
                    } else {
                        productListModel!!.keys = Storefront.ProductSortKeys.CREATED_AT
                    }
                    productListModel!!.isDirection = false
                }
                "ntoo" -> {
                    if (flag) {
                        productListModel!!.sortKeys = Storefront.ProductCollectionSortKeys.CREATED
                    } else {
                        productListModel!!.keys = Storefront.ProductSortKeys.CREATED_AT
                    }
                    productListModel!!.isDirection = true
                }
                "featured" -> {
                    if (flag) {
                        productListModel!!.sortKeys = Storefront.ProductCollectionSortKeys.MANUAL
                    } else {
                        productListModel!!.keys = Storefront.ProductSortKeys.RELEVANCE
                    }
                    productListModel!!.isDirection = false
                }
                "bestseller" -> {
                    if (flag) {
                        productListModel!!.sortKeys =
                            Storefront.ProductCollectionSortKeys.BEST_SELLING
                    } else {
                        productListModel!!.keys = Storefront.ProductSortKeys.BEST_SELLING
                    }
                    productListModel!!.isDirection = false
                }
            }

        }
        productListModel!!.message.observe(this, Observer { this.showToast(it) })
        productListModel!!.Response("nocursor")
        productListModel!!.filteredproducts.observe(
            this,
            Observer<MutableList<Storefront.ProductEdge>> { this.setRecylerData(it!!) })
        binding?.mainview?.sortsection?.setOnClickListener {
            productListModel?.stop = true
            openSortDialog()
        }
        binding?.mainview?.grid_but?.setOnClickListener {

            setLayout(binding?.mainview?.productlist!!,"grid")

            binding?.mainview?.grid_but?.setImageResource(R.drawable.gridiconselected)
            binding?.mainview?.list_but?.setImageResource(R.drawable.listicon)
            applyColors()
//            listEnabled = false
//            var gridintent = Intent(this, ProductList::class.java)
//            if (product_id != null) {
//                gridintent.putExtra("ID", product_id)
//            }
//            if (handle != null) {
//                gridintent.putExtra("handle", handle)
//            }
//            if (intent.hasExtra("tittle") && intent.getStringExtra("tittle") != null) {
//                gridintent.putExtra("tittle", intent.getStringExtra("tittle"))
//            }
//            if (intent.hasExtra("SortType") && intent.getStringExtra("SortType") != null) {
//                gridintent.putExtra("SortType", intent.getStringExtra("SortType"))
//            }
//            this.startActivity(gridintent)
//            finish()
        }
        binding?.mainview?.list_but?.setOnClickListener {
            setLayout(binding?.mainview?.productlist!!,"vertical")
            binding?.mainview?.grid_but?.setImageResource(R.drawable.gridicon)
            binding?.mainview?.list_but?.setImageResource(R.drawable.square)
            applyColors()
//            var listintent = Intent(this, ProductList::class.java)
//            var id: String? = null
//            var type: String? = null
//            if (product_id != null) {
//                id = product_id
//                type = "product_id"
//            }
//            if (handle != null) {
//                id = handle
//                type = "handle"
//            }
//            if (product_id == null && handle == null) {
//                id = "allproduct"
//                type = "allproduct"
//            }
//            if (intent.hasExtra("tittle") && intent.getStringExtra("tittle") != null) {
//                listintent.putExtra("tittle", intent.getStringExtra("tittle"))
//            }
//            listintent.putExtra("List_Product_Id", id)
//            listintent.putExtra("type", type)
//            if (intent.hasExtra("SortType") && intent.getStringExtra("SortType") != null) {
//                listintent.putExtra("SortType", intent.getStringExtra("SortType"))
//            }
//            this.startActivity(listintent)
//            finish()
        }
        filter_by?.setOnClickListener {
            productListModel!!.thread?.cancel()
            openfilterpage()
        }
        binding!!.mainview.continue_shopping.setOnClickListener {
            onBackPressed()
        }

    }

    private fun applyColors() {
        try {
            when (themeColor) {
                "#FFFFFF","#ffffff","#000000"->{
                    when(textColor){
                        "#FFFFFF","#ffffff","#000000"->{
                            if(HomePageViewModel.isLightModeOn()){
                                applyColor("#000000")
                            }else{
                                applyColor("#FFFFFF")
                            }
                        }
                        else->{
                            applyColor(textColor)
                        }
                    }
                }
                else -> {
                    applyColor(themeColor)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
     fun applyColor(color:String){
        try {
            binding!!.mainview.grid_but.setColorFilter(
                Color.parseColor(color),
                PorterDuff.Mode.SRC_IN
            )
            binding!!.mainview.list_but.setColorFilter(
                Color.parseColor(color),
                PorterDuff.Mode.SRC_IN
            )
            binding!!.mainview.sort_icon.setColorFilter(
                Color.parseColor(color),
                PorterDuff.Mode.SRC_IN
            )
            binding!!.mainview.filter_icon.setColorFilter(
                Color.parseColor(color),
                PorterDuff.Mode.SRC_IN
            )
            binding!!.mainview.sep_1.setTextColor(Color.parseColor(color))
            binding!!.mainview.sort_but.setTextColor(Color.parseColor(color))
            binding!!.mainview.filterdiv.setTextColor(Color.parseColor(color))
            binding!!.mainview.filter_but.setTextColor(Color.parseColor(color))
        }catch (e:java.lang.Exception){
            e.printStackTrace()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
            finish()
            var sort_Intent = Intent(this, ProductList::class.java)
            if (intent.getStringExtra("SortType") != null) {
                sort_Intent.putExtra("SortType", intent.getStringExtra("SortType"))
            }
            if (listEnabled) {
                var id: String? = null
                var type: String? = null
                if (product_id != null) {
                    id = product_id
                    type = "product_id"
                }
                if (handle != null) {
                    id = handle
                    type = "handle"
                }
                if (product_id == null && handle == null) {
                    id = "allproduct"
                    type = "allproduct"
                }
                sort_Intent.putExtra("List_Product_Id", id)
                sort_Intent.putExtra("type", type)
            } else {
                if (product_id != null) {
                    sort_Intent.putExtra("ID", product_id)
                }
                if (handle != null) {
                    sort_Intent.putExtra("handle", handle)
                }
            }
            if (intent.hasExtra("tittle") && intent.getStringExtra("tittle") != null) {
                sort_Intent.putExtra("tittle", intent.getStringExtra("tittle"))
            }
            startActivity(sort_Intent)

        }
    }

    @SuppressLint("ResourceType")
    private fun openSortDialog() {
        var dialog = BottomSheetDialog(this, R.style.WideDialog)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        var sortDialogLayoutBinding = DataBindingUtil.inflate<SortDialogLayoutBinding>(
            layoutInflater,
            R.layout.sort_dialog_layout,
            null,
            false
        )
        sortDialogLayoutBinding!!.closedrawer?.imageTintList=resources.getColorStateList(R.color.black)
        sortDialogLayoutBinding!!.closedrawer?.setOnClickListener {

            dialog.cancel()
        }
        when (currentsorting) {
            "atoz" -> {
                sortDialogLayoutBinding.atoz.isChecked = true
                sortDialogLayoutBinding.atoz.buttonTintList=ColorStateList.valueOf(Color.parseColor(themeColor))
            }
            "ztoa" -> {
                sortDialogLayoutBinding.ztoa.isChecked = true
                sortDialogLayoutBinding.ztoa.buttonTintList=ColorStateList.valueOf(Color.parseColor(themeColor))
            }
            "htol" -> {
                sortDialogLayoutBinding.htol.isChecked = true
                sortDialogLayoutBinding.htol.buttonTintList=ColorStateList.valueOf(Color.parseColor(themeColor))
            }
            "ltoh" -> {
                sortDialogLayoutBinding.ltoh.isChecked = true
                sortDialogLayoutBinding.ltoh.buttonTintList=ColorStateList.valueOf(Color.parseColor(themeColor))
            }
            "oton" -> {
                sortDialogLayoutBinding.oton.isChecked = true
                sortDialogLayoutBinding.oton.buttonTintList=ColorStateList.valueOf(Color.parseColor(themeColor))
            }
            "ntoo" -> {
                sortDialogLayoutBinding.ntoo.isChecked = true

                sortDialogLayoutBinding.ntoo.buttonTintList=ColorStateList.valueOf(Color.parseColor(themeColor))
            }
            "featured" -> {
                sortDialogLayoutBinding.featured.isChecked = true
                sortDialogLayoutBinding.featured.buttonTintList=ColorStateList.valueOf(Color.parseColor(themeColor))
            }
            "bestseller" -> {
                sortDialogLayoutBinding.bestSelling.isChecked = true
                sortDialogLayoutBinding.bestSelling.buttonTintList=ColorStateList.valueOf(Color.parseColor(themeColor))
            }
        }
        dialog.setContentView(sortDialogLayoutBinding.root)
        sortDialogLayoutBinding.atoz.setOnClickListener {
            dialog.dismiss()
            sortLoading("atoz")
        }
        sortDialogLayoutBinding.ztoa.setOnClickListener {
            dialog.dismiss()
            sortLoading("ztoa")
        }
        sortDialogLayoutBinding.htol.setOnClickListener {
            dialog.dismiss()
            sortLoading("htol")
        }
        sortDialogLayoutBinding.ltoh.setOnClickListener {
            dialog.dismiss()
            sortLoading("ltoh")
        }
        sortDialogLayoutBinding.featured.setOnClickListener {
            dialog.dismiss()
            sortLoading("featured")
        }
        sortDialogLayoutBinding.bestSelling.setOnClickListener {
            dialog.dismiss()
            sortLoading("bestseller")
        }
        sortDialogLayoutBinding.oton.setOnClickListener {
            dialog.dismiss()
            sortLoading("oton")
        }
        sortDialogLayoutBinding.ntoo.setOnClickListener {
            dialog.dismiss()
            sortLoading("ntoo")
        }
        dialog.show()

    }

    private fun sortLoading(sorttype: String) {
        var sort_Intent = Intent(this, ProductList::class.java)
        sort_Intent.putExtra("SortType", sorttype)
        if (listEnabled) {
            var id: String? = null
            var type: String? = null
            if (product_id != null) {
                id = product_id
                type = "product_id"
            }
            if (handle != null) {
                id = handle
                type = "handle"
            }
            if (product_id == null && handle == null) {
                id = "allproduct"
                type = "allproduct"
            }
            sort_Intent.putExtra("List_Product_Id", id)
            sort_Intent.putExtra("type", type)
        } else {
            if (product_id != null) {
                sort_Intent.putExtra("ID", product_id)
            }
            if (handle != null) {
                sort_Intent.putExtra("handle", handle)
            }
        }
        if (intent.hasExtra("tittle") && intent.getStringExtra("tittle") != null) {
            sort_Intent.putExtra("tittle", intent.getStringExtra("tittle"))
        }
        startActivity(sort_Intent)
        finish()
    }

    private fun collectionResponse(it: Storefront.Collection?) {
        if (it?.title != null) {
            showTittle(it.title)
            Constant.FirebaseEvent_CategoryClicked(it.title)
        }
        MagePrefs.saveHandle(it?.handle)
    }

    private fun openfilterpage() {
        if (MagePrefs.getHandle() != null) {
            productListModel?.stop = true
            val intent = Intent(this, FilterPage::class.java)
            startActivityForResult(intent, 200)
        }
    }

    override fun onResume() {
        super.onResume()
        if (textView != null) {
            textView!!.text = "" + productListModel!!.cartCount
        }
        if (listEnabled)
            product_list_adapter.notifyDataSetChanged()
        else
            product_grid_adapter.notifyDataSetChanged()
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    private fun setRecylerData(products: MutableList<Storefront.ProductEdge>) {
        try {

            Log.i("PRODUCTSSIZE", "" + products.size)
            Log.i("showresp", "sixe : " + products.size)
            if (products.size > 0) {
                binding!!.mainview.nocartsection.visibility = View.GONE
                binding!!.mainview.productListContainer.visibility = View.VISIBLE
                isfirsttimeloaded = true
                productlist!!.visibility = View.VISIBLE
                binding?.mainview?.gridsection?.visibility = View.VISIBLE
                binding?.mainview?.sortsection?.visibility = View.VISIBLE
                binding?.mainview?.filtersection?.visibility = View.VISIBLE
                if (!listEnabled) {
                    if (this.products == null) {
                        this.products = products
                        product_grid_adapter.setData(
                            flistwishmodel!!,
                            this.products, productListModel,
                            this@ProductList,
                            productListModel!!.repository
                        )
                        productlist!!.adapter = product_grid_adapter
                        product_grid_adapter.notifyDataSetChanged()
                    } else {
                        product_grid_adapter.products.addAll(products)
                        product_grid_adapter.notifyDataSetChanged()
                    }
//                    productcursor = this.products!![this.products!!.size - 1].cursor
                } else {
                    if (this.products == null) {
                        this.products = products
                        product_list_adapter.setData(
                            this.products, productListModel!!,
                            this@ProductList,
                            productListModel!!.repository
                        )
                        productlist!!.adapter = product_list_adapter
                        product_list_adapter.notifyDataSetChanged()
                    } else {
                        product_list_adapter.products.addAll(products)
                        product_list_adapter.notifyDataSetChanged()
                    }
                    Log.i("showresp", "sixe12 : " + this.products!!.size)
                    //  productcursor = this.products!![this.products!!.size - 1].cursor
                }
            } else {
                if (!isfirsttimeloaded) {
                    // showToast(resources.getString(R.string.noproducts))
                    // onBackPressed()
                    binding!!.mainview.nocartsection.visibility = View.VISIBLE
                    binding!!.mainview.productListContainer.visibility = View.GONE
                }
            }
            shimmerStopGridProductList()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        productListModel?.stop = true
        super.onDestroy()
    }

    fun setMargins(view: View, left: Int, top: Int, right: Int, bottom: Int) {
        if (view.layoutParams is ViewGroup.MarginLayoutParams) {
            val p = view.layoutParams as ViewGroup.MarginLayoutParams
            p.setMargins(left, top, right, bottom)
            view.requestLayout()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        SplashViewModel.filterfinaldata = HashMap<String, java.util.ArrayList<String>>()
        SplashViewModel.filterinputformat = HashMap<String, String>()
    }
}
