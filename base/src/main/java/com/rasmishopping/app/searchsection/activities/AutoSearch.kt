package com.rasmishopping.app.searchsection.activities
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.zxing.integration.android.IntentIntegrator
import com.mindorks.paracamera.Camera
import com.shopify.buy3.Storefront
import com.rasmishopping.app.FlitsDashboard.WishlistSection.FlitsWishlistViewModel
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.basesection.viewmodels.SplashViewModel.Companion.featuresModel
import com.rasmishopping.app.cartsection.activities.CartList
import com.rasmishopping.app.cartsection.activities.SubscribeCartList
import com.rasmishopping.app.databinding.MAutosearchBinding
import com.rasmishopping.app.databinding.SortDialogLayoutBinding
import com.rasmishopping.app.homesection.viewmodels.HomePageViewModel
import com.rasmishopping.app.productsection.viewmodels.ProductListModel
import com.rasmishopping.app.searchsection.adapters.RecentSearchAdapter
import com.rasmishopping.app.searchsection.adapters.SearchGridAdapter
import com.rasmishopping.app.searchsection.interfaces.Click
import com.rasmishopping.app.searchsection.viewmodels.SearchListModel
import com.rasmishopping.app.sharedprefsection.MagePrefs
import com.rasmishopping.app.utils.Constant
import com.rasmishopping.app.utils.SpacesItemDecoration
import com.rasmishopping.app.utils.ViewModelFactory
import com.rasmishopping.app.wishlistsection.activities.WishList
import kotlinx.android.synthetic.main.m_newbaseactivity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
class AutoSearch : NewBaseActivity() {
    private var binding: MAutosearchBinding? = null
    @Inject
    lateinit var factory: ViewModelFactory
    var model: SearchListModel? = null
    var flistwishmodel: FlitsWishlistViewModel? = null
    var productListModel: ProductListModel? = null
    private lateinit var camera: Camera
    private val PERMISSION_REQUEST_CODE = 1
    var image: InputImage? = null
    private val TAG = "AutoSearch"
    lateinit var labeler:ImageLabeler
    var currentmenutype="advance"
    private var products=ArrayList<Storefront.ProductEdge>()
    @Inject
    lateinit var searchadapter: SearchGridAdapter
    @Inject
    lateinit var recentSearchAdapter: RecentSearchAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val group = findViewById<ViewGroup>(R.id.container)
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.m_autosearch, group, true)
        binding!!.continueShopping.setOnClickListener { finish() }
        applycolors()
        binding!!.features= featuresModel
        showBackButton()
        hidethemeselector()
        if(intent.getStringExtra("menutype")!=null){
            currentmenutype=intent.getStringExtra("menutype")!!
        }
        if(currentmenutype.equals("advance")){
            showAutoSearch()
        }else{
            hideutoSearch()
        }

        nav_view.visibility = View.VISIBLE
        val options = ImageLabelerOptions.Builder().setConfidenceThreshold(0.7f).build()
        labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
        camera = Camera.Builder()
            .resetToCorrectOrientation(true)//1
            .setTakePhotoRequestCode(Camera.REQUEST_TAKE_PHOTO)//2
            .setDirectory("pics")//3
            .setName("delicious_${System.currentTimeMillis()}")//4
            .setImageFormat(Camera.IMAGE_JPEG)//5
            .setCompression(75)//6
            .build(this)
        setLayout(binding!!.searchlist, "autogrid")
        binding!!.searchlist!!.addItemDecoration(SpacesItemDecoration(15,25))
        (application as MyApplication).mageNativeAppComponent!!.doAutoSearchActivityInjection(this)
        model = ViewModelProvider(this, factory).get(SearchListModel::class.java)
        flistwishmodel = ViewModelProvider(this, factory).get(FlitsWishlistViewModel::class.java)
        flistwishmodel!!.context = this
        productListModel = ViewModelProvider(this, factory).get(ProductListModel::class.java)
        productListModel!!.context = this
        model!!.message.observe(this, Observer<String> { this.showToast(it) })
        model!!.noproduct!!.observe(this, Observer<Boolean> {
            binding!!.content.visibility=View.GONE
            binding!!.nodata.visibility=View.VISIBLE
        })
        model!!.getProducts().observe(this,{ updateProductList(it) })
        auto_search.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if(auto_search.text!!.isNotEmpty()){
                    if(products.size==0){
                        binding!!.loader.visibility=View.VISIBLE
                        binding!!.searchlist.visibility=View.GONE
                        model!!.firstdataloaded=false
                        model!!.setKeyword(auto_search.text.toString())
                        Constant.FirebaseEvent_SearchTerm(auto_search.text.toString())
                        binding!!.content.visibility=View.VISIBLE
                        binding!!.nodata.visibility=View.GONE
                        hideKeyboard(this@AutoSearch)
                        model!!.fetchProducts("nocursor",20,this@AutoSearch)
                    }else{
                        reload(auto_search.text.toString())
                    }
                }
                return@OnEditorActionListener true
            }
            false
        })
        binding?.searchtext?.requestFocus()
        binding?.cancelAction?.setOnClickListener {
            binding?.searchtext?.setText("")
        }
        imagesearch.setOnClickListener({
            takePicture()
        })
        brcodesearch.setOnClickListener({
            val integrator = IntentIntegrator(this)
            integrator.setPrompt("Scan a barcode")
            integrator.setCameraId(0) // Use a specific camera of the device
            integrator.setOrientationLocked(true)
            integrator.setBeepEnabled(true)
            integrator.captureActivity = SearchByScanner::class.java
            integrator.initiateScan()
        })
        auto_voicesearch.setOnClickListener({
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale(MagePrefs.getLanguage()))
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, resources.getString(R.string.search))
            try {
                startActivityForResult(intent,100234)
               // resultLauncher.launch(intent);
            } catch (e: Exception) {
                Toast.makeText(this@AutoSearch, " " + e.message, Toast.LENGTH_SHORT).show()
            }
        })
        binding!!.sort.setOnClickListener({
            openSortDialog()
        })
        if(intent.getStringExtra("search_keyword")!=null){
            if (intent.getStringExtra("sort")!=null){
                when(intent.getStringExtra("sort")) {
                    "atoz"->{
                        model!!.setSort(Storefront.ProductSortKeys.TITLE)
                        model!!.setReverse(false)
                    }
                    "ztoa"->{
                        model!!.setSort(Storefront.ProductSortKeys.TITLE)
                        model!!.setReverse(true)
                    }
                    "htol"->{
                        model!!.setSort(Storefront.ProductSortKeys.PRICE)
                        model!!.setReverse(true)
                    }
                    "ltoh"->{
                        model!!.setSort(Storefront.ProductSortKeys.PRICE)
                        model!!.setReverse(false)
                    }
                    "oton"->{
                        model!!.setSort(Storefront.ProductSortKeys.UPDATED_AT)
                        model!!.setReverse(false)
                    }
                    "ntoo"->{
                        model!!.setSort(Storefront.ProductSortKeys.UPDATED_AT)
                        model!!.setReverse(true)
                    }
                    "featured"->{
                        model!!.setSort(Storefront.ProductSortKeys.RELEVANCE)
                        model!!.setReverse(true)
                    }
                    "bestseller"->{
                        model!!.setSort(Storefront.ProductSortKeys.BEST_SELLING)
                        model!!.setReverse(true)
                    }
                }
            }
            if (!intent.getStringExtra("search_keyword").equals("noloading")){
                binding!!.loader.visibility=View.VISIBLE
                binding!!.searchlist.visibility=View.GONE
                model!!.firstdataloaded=false
                model!!.setKeyword(intent.getStringExtra("search_keyword")!!)
                binding!!.content.visibility=View.VISIBLE
                binding!!.nodata.visibility=View.GONE
                model!!.fetchProducts("nocursor",20,this@AutoSearch)
                auto_search.setText(intent.getStringExtra("search_keyword")!!)
            }
        }
        if(currentmenutype.equals("advance")){
            if (MagePrefs.getRecent().length()>0){
                setLayout(binding!!.recentsearch, "autogrid4")
                binding!!.recentsearch!!.addItemDecoration(SpacesItemDecoration(15,25))
                recentSearchAdapter.setData(MagePrefs.getRecent().names()!!,this@AutoSearch, object : Click{
                    override fun click(keyword: String) {
                        binding!!.loader.visibility=View.VISIBLE
                        binding!!.searchlist.visibility=View.GONE
                        model!!.firstdataloaded=false
                        model!!.setKeyword(keyword)
                        binding!!.content.visibility=View.VISIBLE
                        binding!!.nodata.visibility=View.GONE
                        model!!.fetchProducts("nocursor",20,this@AutoSearch)
                        auto_search.setText(keyword)
                    }
                })
                binding!!.recentsearch.adapter=recentSearchAdapter
                binding!!.recentsearchsection.visibility=View.VISIBLE
            }
        }
        binding!!.clearrecentsearch.setOnClickListener({
            MagePrefs.clearRecent()
            binding!!.recentsearchsection.visibility=View.GONE
        })
    }
    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if(products.size==0){
            currentmenutype="advance"
            return false
        }else{
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
                ////////////////Wishlist menu Item//////////////
                var wishitem = menu.findItem(R.id.wish_item)
                wishitem.setActionView(R.layout.m_wishcount)
                val wishview = wishitem.actionView
                val wishrelative = wishview?.findViewById<RelativeLayout>(R.id.back)
                val wishtext = wishview?.findViewById<TextView>(R.id.count)
                val wishicon = wishview?.findViewById<ImageView>(R.id.cart_icon)
                wishrelative?.backgroundTintList = ColorStateList.valueOf(Color.parseColor(
                    HomePageViewModel.count_color
                ))
                wishtext?.setTextColor(Color.parseColor(HomePageViewModel.count_textcolor))
                wishicon?.setColorFilter(Color.parseColor(HomePageViewModel.icon_color))
                wishtext!!.text = "" + leftMenuViewModel!!.wishListcount
                wishitem.isVisible = featuresModel.in_app_wishlist
                wishitem.actionView?.setOnClickListener {
                    onOptionsItemSelected(wishitem)
                }
                ////////////////cart menu Item//////////////
                val cartitem = menu.findItem(R.id.cart_item)
                cartitem.setActionView(R.layout.m_count)
                val cartview=cartitem.actionView
                val cartrelative = cartview?.findViewById<RelativeLayout>(R.id.back)
                val carttext = cartview?.findViewById<TextView>(R.id.count)
                val carticon = cartview?.findViewById<ImageView>(R.id.cart_icon)
                cartrelative?.backgroundTintList = ColorStateList.valueOf(Color.parseColor(
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
            showTittle(model!!.getKeyword().toUpperCase())
            hideutoSearch()
            currentmenutype="normal"
            return true
        }
    }
    override fun onResume() {
        invalidateOptionsMenu()
        super.onResume()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.scanner -> {
                val integrator = IntentIntegrator(this)
                integrator.setPrompt("Scan a barcode")
                integrator.setCameraId(0) // Use a specific camera of the device
                integrator.setOrientationLocked(true)
                integrator.setBeepEnabled(true)
                integrator.captureActivity = SearchByScanner::class.java
                integrator.initiateScan()
                true
            }
            R.id.camera -> {
                takePicture()
                true
            }
            R.id.search_item -> {
                moveToSearch(this)
                finish()
                true
            }
            R.id.wish_item -> {
                startActivity(Intent(this, WishList::class.java))
                Constant.activityTransition(this)
                true
            }
            R.id.cart_item -> {
                CoroutineScope(Dispatchers.IO).launch {
                    if (leftMenuViewModel?.repository?.getSellingPlanData()?.selling_plan_id!=null) {
                        startActivity(Intent(this@AutoSearch, SubscribeCartList::class.java))
                        Constant.activityTransition(this@AutoSearch)
                    }
                    else{
                        startActivity(Intent(this@AutoSearch, CartList::class.java))
                        Constant.activityTransition(this@AutoSearch)
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Camera.REQUEST_TAKE_PHOTO) {
                val bitmap = camera.cameraBitmap
                if (bitmap != null) {
                    checkImage(bitmap)
                } else {
                    Toast.makeText(
                        this.applicationContext,
                        getString(R.string.picture_not_taken),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else if (requestCode==100234){
                if (resultCode === RESULT_OK && data != null) {
                    val finaldata: ArrayList<String> = data!!.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS
                    ) as ArrayList<String>
                    Log.i("SearchSaif",""+finaldata[finaldata.size-1]);
                    auto_search.setText(finaldata[finaldata.size-1])
                    if(products.size==0){
                        binding!!.loader.visibility=View.VISIBLE
                        binding!!.searchlist.visibility=View.GONE
                        model!!.firstdataloaded=false
                        model!!.setKeyword(finaldata[finaldata.size-1])
                        binding!!.content.visibility=View.VISIBLE
                        binding!!.nodata.visibility=View.GONE
                        model!!.fetchProducts("nocursor",20,this@AutoSearch)
                    }else{
                        reload(finaldata[finaldata.size-1])
                    }
                }
            }else{
                val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
                if (result != null) {
                    if (result.contents == null) {
                        Toast.makeText(
                            applicationContext,
                            "" + resources.getString(R.string.noresultfound),
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    } else {
                        try {
                            Log.i("MageNative", "Barcode" + result.contents)
                            auto_search.setText(result.contents)
                            if(products.size==0){
                                binding!!.loader.visibility=View.VISIBLE
                                binding!!.searchlist.visibility=View.GONE
                                model!!.firstdataloaded=false
                                model!!.setKeyword(result.contents)
                                binding!!.content.visibility=View.VISIBLE
                                binding!!.nodata.visibility=View.GONE
                                model!!.fetchProducts("nocursor",1,this@AutoSearch)
                            }else{
                                reload(result.contents)
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
    fun takePicture() {
        if (!hasPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
            !hasPermission(android.Manifest.permission.CAMERA)
        ) {
            // If do not have permissions then request it
            requestPermissions()
        } else {
            // else all permissions granted, go ahead and take a picture using camera
            try {
                camera.takePicture()
                setResult(Activity.RESULT_OK)
            } catch (e: Exception) {
                // Show a toast for exception
                Toast.makeText(
                    this.applicationContext, getString(R.string.error_taking_picture),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    private fun requestPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.CAMERA
                ),
                PERMISSION_REQUEST_CODE
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.CAMERA
                ), PERMISSION_REQUEST_CODE
            )
            return
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                ) {
                    try {
                        camera.takePicture()
                        setResult(Activity.RESULT_OK)
                    } catch (e: Exception) {
                        Toast.makeText(
                            this.applicationContext, getString(R.string.error_taking_picture),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                return
            }
        }
    }

    private fun checkImage(bitmap: Bitmap) {
        image = InputImage.fromBitmap(bitmap, 0)
        labeler.process(image!!)
            .addOnSuccessListener { labels ->
                for (label in labels) {
                    val text = label.text
                    val confidence = label.confidence
                    Log.d(TAG, "checkImage: " + text)
                    Log.i("MageNative", "Label : " + text)
                    Log.i("MageNative", "confidence : $confidence")
                    if (confidence < 0.6) {
                        Toast.makeText(
                            this@AutoSearch,
                            resources.getString(R.string.norelevantsearch),
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        auto_search.setText("")
                        if(products.size==0){
                            binding!!.loader.visibility=View.VISIBLE
                            binding!!.searchlist.visibility=View.GONE
                            model!!.firstdataloaded=false
                            model!!.setKeyword(text)
                            model!!.fetchProducts("nocursor",20,this@AutoSearch)
                        }else{
                            reload(text)
                        }
                        break
                    }
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }
    private fun hasPermission(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
    override fun onDestroy() {
        super.onDestroy()
        nav_view.visibility = View.VISIBLE
    }
    private fun updateProductList(it: ArrayList<Storefront.ProductEdge>) {
        Log.i("SearchSaif",""+it!!.size)
        if(it.size>0){
            if(products.size==0){
                MagePrefs.saveRecent(model!!.getKeyword())
                Constant.FirebaseEvent_SearchTerm(model!!.getKeyword())
                products.addAll(it)
                searchadapter.setData(flistwishmodel!!,products, model = productListModel,this@AutoSearch,model!!.repository)
                binding!!.searchlist.adapter=searchadapter
                searchadapter.notifyDataSetChanged()
                showSortButton(true)
                binding!!.recentsearchsection.visibility=View.GONE
                invalidateOptionsMenu()
            }else{
                products.addAll(it)
                searchadapter.products=products
                searchadapter.notifyDataSetChanged()
            }
        }else{
            Log.i("SearchSaif","No More Products")
        }
        binding!!.loader.visibility=View.GONE
        binding!!.searchlist.visibility=View.VISIBLE
        hideKeyboard(this@AutoSearch)

    }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Log.i("SearchSaif",""+result)
        if (result.resultCode === RESULT_OK && result.data != null) {
            val finaldata: ArrayList<String> = result.data!!.getStringArrayListExtra(
                RecognizerIntent.EXTRA_RESULTS
            ) as ArrayList<String>
            Log.i("SearchSaif",""+finaldata[finaldata.size-1]);
            auto_search.setText("")
            if(products.size==0){
                binding!!.loader.visibility=View.VISIBLE
                binding!!.searchlist.visibility=View.GONE
                if(products.size>0){
                    products=ArrayList<Storefront.ProductEdge>()
                }
                model!!.firstdataloaded=false
                model!!.setKeyword(finaldata[finaldata.size-1])
                model!!.fetchProducts("nocursor",20,this@AutoSearch)
            }else{
                reload(finaldata[finaldata.size-1])
            }
        }
    }
    fun reload(keyword:String){
        var intent=Intent(this@AutoSearch, AutoSearch::class.java)
        intent.putExtra("search_keyword",keyword)
        intent.putExtra("menutype",currentmenutype)
        startActivity(intent)
        Constant.activityTransition(this@AutoSearch)
       // overridePendingTransition(R.anim.entry, R.anim.exit)
        finish()
    }
    private fun openSortDialog() {
        var dialog = BottomSheetDialog(this, R.style.WideDialog)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        var sortDialogLayoutBinding = DataBindingUtil.inflate<SortDialogLayoutBinding>(
            layoutInflater,
            R.layout.sort_dialog_layout,
            null,
            false
        )
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
    private fun sortLoading(sorttype:String){
        var intent=Intent(this@AutoSearch, AutoSearch::class.java)
        intent.putExtra("search_keyword",model!!.getKeyword())
        intent.putExtra("sort",sorttype)
        intent.putExtra("menutype",currentmenutype)
        startActivity(intent)
        Constant.activityTransition(this@AutoSearch)
        finish()
    }
    fun applycolors(){
        binding!!.sort.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(themeColor)))
        binding!!.sort.setColorFilter(Color.parseColor(textColor), PorterDuff.Mode.SRC_IN);
    }
    fun showSortButton(visible:Boolean){
        binding!!.sort.isVisible=visible
    }

    override fun onBackPressed() {
        if(currentmenutype.equals("normal")){
            currentmenutype="advance"
            reload("noloading")
        }else{
            super.onBackPressed()
        }
    }

}
