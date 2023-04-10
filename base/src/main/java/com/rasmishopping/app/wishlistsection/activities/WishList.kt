package com.rasmishopping.app.wishlistsection.activities
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.shopify.buy3.Storefront
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.basesection.viewmodels.SplashViewModel
import com.rasmishopping.app.databinding.MWishlistBinding
import com.rasmishopping.app.homesection.activities.HomePage
import com.rasmishopping.app.homesection.viewmodels.HomePageViewModel
import com.rasmishopping.app.utils.Constant
import com.rasmishopping.app.utils.SpacesItemDecoration
import com.rasmishopping.app.utils.ViewModelFactory
import com.rasmishopping.app.wishlistsection.adapters.WishListAdapter
import com.rasmishopping.app.wishlistsection.viewmodels.WishListViewModel
import javax.inject.Inject
class WishList : NewBaseActivity() {
    private var binding: MWishlistBinding? = null
    @Inject
    lateinit var factory: ViewModelFactory
    private var model: WishListViewModel? = null
    @Inject
    lateinit var adapter: WishListAdapter
    private var list: RecyclerView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val group = findViewById<ViewGroup>(R.id.container)
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.m_wishlist, group, true)
        binding!!.continueShopping.setOnClickListener({
            var intent = Intent(this, HomePage::class.java)
            startActivity(intent)
            Constant.activityTransition(this)
        })
        shimmerStartGridProductList()
        showBackButton()
        list = setLayout(binding!!.root.findViewById(R.id.wishlist), "grid")
        list!!.addItemDecoration(SpacesItemDecoration(5,10))
        (application as MyApplication).mageNativeAppComponent!!.doWishListActivityInjection(this)
        model = ViewModelProvider(this, factory).get(WishListViewModel::class.java)
        model?.context = this
        showCartText(resources.getString(R.string.mywishlist) , " ( " + model!!.wishListCount + " ${resources.getString(R.string.items)} )")
        model!!.WishListResponse().observe(this, Observer<Storefront.Checkout> { this.consumeWishlistResponse(it) })
        model!!.getToastMessage().observe(this, Observer { consumeErrorResponse(it) })
    }
    private fun consumeErrorResponse(it: String?) {
        showToast(it!!)
    }
    private fun consumeWishlistResponse(reponse: Storefront.Checkout) {
        showCartText(resources.getString(R.string.mywishlist) , " ( " + reponse.lineItems?.edges?.size + " ${resources.getString(R.string.items)} )")
        if (reponse.lineItems?.edges?.size == 0) {
            ViewVisible(false)
        }

        else{
            ViewVisible(true)
            adapter.setData(reponse.lineItems.edges, this, model!!)
            adapter.notifyDataSetChanged()
            list!!.adapter = adapter

        }
        showShadow()
        shimmerStopGridProductList()
    }
    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
    override fun onResume() {
        super.onResume()
        if(model!!.wishListCount > 0){
            model?.prepareWishlist()
            invalidateOptionsMenu()
            ViewVisible(true)
        }else{
            shimmerStopGridProductList()
            ViewVisible(false)
        }
    }
    fun ViewVisible(flag:Boolean){
        when(flag){
            true->{
                binding!!.content.visibility= View.VISIBLE
                binding!!.nocartsection.visibility= View.GONE
            }
            false->{
                binding!!.content.visibility= View.GONE
                binding!!.nocartsection.visibility= View.VISIBLE
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.m_search, menu)
        try{
            ////////////////Search menu Item//////////////
            var item = menu.findItem(R.id.search_item)
            item.setActionView(R.layout.m_searchicon)
            val view = item.actionView
            val searchicon = view?.findViewById<ImageView>(R.id.cart_icon)
            searchicon?.setColorFilter(Color.parseColor(HomePageViewModel.icon_color))
            view!!.setOnClickListener {
                onOptionsItemSelected(item)
            }
            item.isVisible=false
            ////////////////Wishlist menu Item//////////////
            var wishitem = menu.findItem(R.id.wish_item)
            wishitem.setActionView(R.layout.m_wishcount)
            val wishview = wishitem.actionView
            val wishrelative = wishview?.findViewById<RelativeLayout>(R.id.back)
            val wishtext = wishview?.findViewById<TextView>(R.id.count)
            val wishicon = wishview?.findViewById<ImageView>(R.id.cart_icon)
            wishrelative?.backgroundTintList = ColorStateList.valueOf(
                Color.parseColor(
                HomePageViewModel.count_color
            ))
            wishtext?.setTextColor(Color.parseColor(HomePageViewModel.count_textcolor))
            wishicon?.setColorFilter(Color.parseColor(HomePageViewModel.icon_color))
            wishtext!!.text = "" + leftMenuViewModel!!.wishListcount
            wishitem.isVisible = SplashViewModel.featuresModel.in_app_wishlist
            wishitem.actionView?.setOnClickListener {
                onOptionsItemSelected(wishitem)
            }
            wishitem.isVisible=false
            ////////////////cart menu Item//////////////
            val cartitem = menu.findItem(R.id.cart_item)
            cartitem.setActionView(R.layout.m_count)
            val cartview=cartitem.actionView
            val cartrelative = cartview?.findViewById<RelativeLayout>(R.id.back)
            val carttext = cartview?.findViewById<TextView>(R.id.count)
            val carticon = cartview?.findViewById<ImageView>(R.id.cart_icon)
            cartrelative?.backgroundTintList = ColorStateList.valueOf(
                Color.parseColor(
                HomePageViewModel.count_color
            ))
            carttext?.setTextColor(Color.parseColor(HomePageViewModel.count_textcolor))
            carticon?.setColorFilter(Color.parseColor(HomePageViewModel.icon_color))
            if(leftMenuViewModel?.cartCount!!>0){
                cartrelative?.visibility=View.VISIBLE
                carttext!!.text = "" + leftMenuViewModel?.cartCount
            }
            cartitem.actionView?.setOnClickListener {
                onOptionsItemSelected(cartitem)
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
        return true
    }
}
