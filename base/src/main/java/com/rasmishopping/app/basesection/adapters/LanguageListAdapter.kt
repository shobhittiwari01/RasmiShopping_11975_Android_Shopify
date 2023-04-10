package com.rasmishopping.app.basesection.adapters
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.rasmishopping.app.R
import com.rasmishopping.app.databinding.LanguageItemBinding
import com.rasmishopping.app.sharedprefsection.MagePrefs
import kotlinx.android.synthetic.main.language_item.view.*
import org.json.JSONArray
import java.util.*
import javax.inject.Inject
class LanguageListAdapter @Inject constructor() : RecyclerView.Adapter<LanguageListAdapter.LangauageListViewHolder>() {
    class LangauageListViewHolder(itemView: LanguageItemBinding) : RecyclerView.ViewHolder(itemView.root)
    private var languageList: JSONArray? = JSONArray()
    private var languageCallback: LanguageCallback? = null
    private var selectedPosition = -1
    fun setData(languageList: JSONArray?, languageCallback: LanguageCallback) {
        this.languageList = languageList
        this.languageCallback = languageCallback
    }
    interface LanguageCallback {
        fun selectedLanguage(language: String)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LangauageListViewHolder {
        val view = DataBindingUtil.inflate<LanguageItemBinding>(
            LayoutInflater.from(parent.context),
            R.layout.language_item,
            parent,
            false
        )
        return LangauageListViewHolder(view)
    }

    override fun onBindViewHolder(holder: LangauageListViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.itemView.language_title.tag = languageList?.getString(position)
        holder.itemView.language_title.text = getDisplayName(languageList?.getString(position)!!)
        if(MagePrefs.getLanguage().equals(languageList?.getString(position))){
            selectedPosition=position
        }
        if (selectedPosition == position) {
            holder.itemView.select_language.setImageDrawable(
                holder.itemView.select_language.context.resources.getDrawable(
                    R.drawable.checked_icon,
                    null
                )
            )
        } else {
            holder.itemView.select_language.setImageDrawable(
                holder.itemView.select_language.context.resources.getDrawable(
                    R.drawable.round_circle_selector,
                    null
                )
            )
        }
        holder.itemView.language_container.setOnClickListener {
            languageCallback?.selectedLanguage(languageList?.getString(position) ?: "")
            selectedPosition = position
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return languageList?.length()!!
    }
    fun getDisplayName(code:String):String{
        var loc= Locale(code)
        return loc.getDisplayLanguage(loc)
    }
}