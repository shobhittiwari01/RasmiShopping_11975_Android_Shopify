package com.shopify.instafeeds

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shopify.instafeeds.databinding.ActivityInstafeedBinding
import org.json.JSONObject

class InstaFeed : AppCompatActivity() {
    //private var instafeedmodel: InstafeedViewModel? = null
    //var instafeed_adapter: InstaFeedAdapter = InstaFeedAdapter()
    var data:RecyclerView? = null

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //binding = DataBindingUtil.setContentView(this, R.layout.activity_instafeed)
        //var view:View = Inflater().inflate(R.layout.activity_instafeed)
        val binding: ActivityInstafeedBinding = DataBindingUtil.inflate(
            this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater,
            R.layout.activity_instafeed,
            null,
            false
        )

        binding.feedsrecycler.layoutManager = GridLayoutManager(this, 2)
        val instafeedmodel = ViewModelProvider(this).get(InstafeedViewModel::class.java)
        val instafeed_adapter = InstaFeedAdapter()
        instafeedmodel.context = this
        instafeedmodel.InstafeedResponse().observe(this) {
            Log.i("INSTAFEEDRESPONSE", "" + it?.data)
            val jsondata = JSONObject(it?.data.toString())
            if (jsondata.has("data")) {
                val dataarray = jsondata.getJSONArray("data")
                if (dataarray.length() > 0) {
                    binding.feedusername.text =
                        "Instagram Feeds" + " " + "@" + dataarray.getJSONObject(0).getString("username")
                    instafeed_adapter.setData(this, dataarray)
                    //binding!!.feedsrecycler.adapter = instafeed_adapter
                    binding.feedsrecycler.adapter = instafeed_adapter
                    //binding?.feedsrecycler?.layoutManager = GridLayoutManager(this, 2)
                    instafeed_adapter.notifyDataSetChanged()
                    data = binding.feedsrecycler
                }
            } }
      //  instafeedmodel.getInstafeeds(resources.getString(R.string.instafields), resources.getString(R.string.instaaccesstoken),12)
    }

    init{
        //onCreate(Bundle())
        //binding = DataBindingUtil.setContentView(this, R.layout.activity_instafeed)
        //var view:View = Inflater().inflate(R.layout.activity_instafeed)
        /*var binding: ActivityInstafeedBinding = DataBindingUtil.inflate(
            this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater,
            R.layout.activity_instafeed,
            null,
            false
        )
        binding.feedsrecycler!!.layoutManager = GridLayoutManager(this, 2)
        var instafeedmodel = ViewModelProvider(this).get(InstafeedViewModel::class.java)
        var  instafeed_adapter: InstaFeedAdapter = InstaFeedAdapter()
        instafeedmodel!!.context = this
        instafeedmodel.InstafeedResponse(
            resources.getString(R.string.instafields),
            resources.getString(R.string.instaaccesstoken)
        ).observe(this) { Log.i("INSTAFEEDRESPONSE", "" + it?.data)
            val jsondata = JSONObject(it?.data.toString())
            if (jsondata.has("data")) {
                val dataarray = jsondata.getJSONArray("data")
                if (dataarray.length() > 0) {
                    binding!!.feedusername.text =
                        "Instagram Feeds" + " " + "@" + dataarray.getJSONObject(0).getString("username")
                    instafeed_adapter.setData(this, dataarray)
                    //binding!!.feedsrecycler.adapter = instafeed_adapter
                    binding.feedsrecycler.adapter = instafeed_adapter
                    //binding?.feedsrecycler?.layoutManager = GridLayoutManager(this, 2)
                    instafeed_adapter.notifyDataSetChanged()
                    data = binding.feedsrecycler
                }
            } }*/
    }

    /*@SuppressLint("SetTextI18n")
    fun showfeedsData(apiResponse: ApiResponse?) {
        Log.i("INSTAFEEDRESPONSE", "" + apiResponse?.data)
        val jsondata = JSONObject(apiResponse?.data.toString())
        if (jsondata.has("data")) {
            val dataarray = jsondata.getJSONArray("data")
            if (dataarray.length() > 0) {
                binding!!.feedusername.text =
                    "Instagram Feeds" + " " + "@" + dataarray.getJSONObject(0).getString("username")
                instafeed_adapter.setData(this, dataarray)
                //binding!!.feedsrecycler.adapter = instafeed_adapter
                data!!.adapter = instafeed_adapter
                //binding?.feedsrecycler?.layoutManager = GridLayoutManager(this, 2)
                instafeed_adapter.notifyDataSetChanged()
            }
        }
    }*/
    /*fun getfeedsdata():RecyclerView{
        return data!!
    }*/
}