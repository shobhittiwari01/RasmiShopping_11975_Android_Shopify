package com.rasmishopping.app.basesection.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.shopify.buy3.Storefront
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.basesection.activities.Splash
import com.rasmishopping.app.basesection.viewholders.ListItems
import com.rasmishopping.app.basesection.viewmodels.LeftMenuViewModel
import com.rasmishopping.app.databinding.CountrycodeListItemBinding
import com.rasmishopping.app.dbconnection.entities.AppLocalData
import com.rasmishopping.app.repositories.Repository
import com.rasmishopping.app.sharedprefsection.MagePrefs
import com.rasmishopping.app.utils.Constant
import javax.inject.Inject

class RecylerCountryCodeAdapter @Inject constructor() : RecyclerView.Adapter<ListItems>() {
    private var countrycode: List<Storefront.Country>? = null
    private var activity: Activity? = null
    private var repository: Repository? = null
    private var leftMenuViewModel: LeftMenuViewModel? = null
    var appLocalData: AppLocalData?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListItems {
        val binding = DataBindingUtil.inflate<CountrycodeListItemBinding>(
            LayoutInflater.from(parent.context),
            R.layout.countrycode_list_item,
            parent,
            false
        )
        return ListItems(binding)
    }

    @SuppressLint("SetTextI18n", "LogNotTimber")
    override fun onBindViewHolder(holder: ListItems, position: Int) {
        holder.binding.currencyTitle.text =
            countrycode!![position].name + " " + "(" + "" + countrycode!![position].isoCode + ")"
        holder.binding.currencyTitle.setOnClickListener {
            try{
                Log.i("COUNTRYCODES", countrycode!![position].isoCode.toString())
                leftMenuViewModel!!.deleteData()
                MagePrefs.saveCountryCode(countrycode!![position].isoCode)
                saveCurrency(countrycode!![position].isoCode.toString())
                (activity as NewBaseActivity).closePopUp()
                val intent = Intent(activity, Splash::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                activity?.startActivity(intent)
                Constant.activityTransition(activity!!)
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }

    override fun getItemCount(): Int {
        return countrycode!!.size
    }

    fun setData(
        countrycode: List<Storefront.Country>,
        activity: NewBaseActivity,
        repository: Repository,
        leftMenuViewModel: LeftMenuViewModel
    ) {
        this.countrycode = countrycode
        this.activity = activity
        this.repository = repository
        this.leftMenuViewModel = leftMenuViewModel
    }

    fun saveCurrency(countryCode: String) {
        appLocalData= AppLocalData()
        Log.i("COUNTRYCODES2", Constant.getCurrency(countryCode))
        val runnable = Runnable {
            appLocalData?.currencycode = Constant.getCurrency(countryCode)
            if (repository!!.localData.size == 0) {
                repository!!.insertData(appLocalData!!)
            } else {
                repository!!.currencyupdate(Constant.getCurrency(countryCode))
            }
        }
        Thread(runnable).start()
    }
}
