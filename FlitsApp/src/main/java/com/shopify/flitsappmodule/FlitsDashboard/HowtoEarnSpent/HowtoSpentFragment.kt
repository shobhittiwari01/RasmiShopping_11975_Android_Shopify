package com.shopify.flitsappmodule.FlitsDashboard.HowtoEarnSpent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonArray
import com.shopify.flitsappmodule.R
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class HowtoSpentFragment(var response:JSONObject,var currency: String,var pos:Int):Fragment() {
    private var recyclerView: RecyclerView? = null
    private var packageViewAdapter: EarnSpentViewAdapter? = null
    var jsonObject:JSONObject?= JSONObject()
    var jsonArray: JSONArray?= JSONArray()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.howtoearnfragment, container, false)
    }

    override fun onViewCreated(@NonNull view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recyclerView)

        var data=response.getJSONObject("rules").getJSONArray("all_rules_data")
        for(i in 0 until data.length()) {
            var key=data.getJSONObject(i).get("tab_to_append")
            when(key) {
                "flits_spent_rules" -> {
                    jsonArray?.put(data.getJSONObject(i))
                }
            }
        }
        jsonObject?.put("data",jsonArray)
        packageViewAdapter = context?.let { EarnSpentViewAdapter(it, jsonObject!!,currency,pos) }
        recyclerView!!.adapter = packageViewAdapter
    }

}