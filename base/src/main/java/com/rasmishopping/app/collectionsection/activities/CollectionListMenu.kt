package com.rasmishopping.app.collectionsection.activities
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonElement
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.collectionsection.adapters.CategoryAdapter
import com.rasmishopping.app.collectionsection.adapters.SubCategoriesRecycler
import com.rasmishopping.app.collectionsection.viewmodels.CollectionMenuViewModel
import com.rasmishopping.app.databinding.ActivityCollectionListMenuBinding
import com.rasmishopping.app.productsection.activities.ProductList
import com.rasmishopping.app.utils.ApiResponse
import com.rasmishopping.app.utils.Constant
import com.rasmishopping.app.utils.Status
import com.rasmishopping.app.utils.ViewModelFactory
import kotlinx.android.synthetic.main.shimmer_layout_collection_list.*
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
class CollectionListMenu : NewBaseActivity() {
    var binding: ActivityCollectionListMenuBinding? = null
    @Inject
    lateinit var categoryAdapter: CategoryAdapter
    @Inject
    lateinit var subcatAdapter: SubCategoriesRecycler
    @Inject
    lateinit var factory: ViewModelFactory
    private var model: CollectionMenuViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val group = findViewById<ViewGroup>(R.id.container)
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.activity_collection_list_menu, group, true)
        shimmerStart()
        showBackButton()
        showTittle(resources.getString(R.string.collection))
        hidethemeselector()
        (application as MyApplication).mageNativeAppComponent!!.doCollectionInjection(this)
        model = ViewModelProvider(this, factory).get(CollectionMenuViewModel::class.java)
        model?.context = this
        model?.Response()?.observe(this, Observer { consumeMenuResponse(it) })
        binding?.clickHandler = this
    }
    fun clickSearch(view: View) {
        moveToSearch(this)
    }
    private fun consumeMenuResponse(reponse: ApiResponse?) {
        when (reponse?.status) {
            Status.SUCCESS -> renderSuccessResponse(reponse.data!!)
            Status.ERROR -> {
                reponse.error!!.printStackTrace()
                showToast(resources.getString(R.string.errorString))
            }
            else -> {
            }
        }
    }
    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
    @OptIn(DelicateCoroutinesApi::class)
    fun renderSuccessResponse(data: JsonElement) {
        Log.i("MageNative:", "MenuData$data")
        try {
            val `object` = JSONObject(data.toString())
            if (`object`.getBoolean("success")) {
                if (`object`.has("data")) {
                    val array = `object`.getJSONArray("data")
                    if (array.length() > 0) {
                        categoryAdapter.setData(array, tagSelectionCallBack = object : CategoryAdapter.TagSelectionCallBack {
                            override fun tagCallback(list: JSONArray,id:String) {
                                    binding?.shopbycatTitle?.visibility = View.VISIBLE
                                    binding?.subcatRecycler?.visibility = View.VISIBLE
                                    binding?.nocategorysectionsection?.visibility=View.GONE
                                    subcatAdapter.setSubCatRecylerData(list, this@CollectionListMenu)
                                    val gridLayoutManager = GridLayoutManager(applicationContext, 3)
                                    gridLayoutManager.orientation = LinearLayoutManager.VERTICAL
                                    binding?.subcatRecycler?.layoutManager = gridLayoutManager
                                    binding?.subcatRecycler?.adapter = subcatAdapter
                                    subcatAdapter.notifyDataSetChanged()
                            }
                        },this, callback = object:CategoryAdapter.CallBackForNoSubmenu{
                            override fun Callback(id: String) {
                                binding?.shopbycatTitle?.visibility=View.GONE
                                binding?.subcatRecycler?.visibility=View.GONE
                                binding?.nocategorysectionsection?.visibility=View.VISIBLE
                                binding!!.continueShopping.setOnClickListener{
                                    val intent= Intent(this@CollectionListMenu, ProductList::class.java)
                                    intent.putExtra("ID",id)
                                    startActivity(intent)
                                    Constant.activityTransition(this@CollectionListMenu)
                                }
                            }
                        })
                        binding?.catRecycler?.adapter = categoryAdapter
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        shimmerStop()
    }
    fun shimmerStart() {
        shimmer_view_container_collection_list.startShimmer()
    }
    override fun shimmerStop() {
        shimmer_view_container_collection_list.stopShimmer()
        shimmer_view_container_collection_list.visibility = View.GONE
        binding!!.content.visibility=View.VISIBLE
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return false
    }
}