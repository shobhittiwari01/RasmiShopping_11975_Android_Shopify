package com.shopify.flitsappmodule.FlitsDashboard.HowtoEarnSpent

import android.graphics.Color
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.shopify.flitsappmodule.R
import org.json.JSONObject

class EarnSpentTabAdapter(fm: FragmentManager?, var tabLayout: TabLayout,var response:JSONObject,var currencycode:String) :
    FragmentStatePagerAdapter(fm!!) {
    override fun getItem(position: Int): Fragment {
        var fragment: Fragment? = null
        if (position == 0) {
            fragment = HowtoEarnFragment(response,currencycode,position)
        }
        else if (position == 1) {
            fragment = HowtoSpentFragment(response,currencycode,position)
        }
        return fragment!!
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence? {
        var title: String? = null
        if (position == 0) {
            title = "How to Earn"
        } else if (position == 1) {
            title = "How to Spent"
        }
        return title
    }
}
