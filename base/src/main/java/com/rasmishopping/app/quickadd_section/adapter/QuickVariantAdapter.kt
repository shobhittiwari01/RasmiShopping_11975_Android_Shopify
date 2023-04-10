package com.rasmishopping.app.quickadd_section.adapter
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.databinding.SwactchesListItemQuickcartBinding
import com.rasmishopping.app.homesection.viewmodels.HomePageViewModel
import com.rasmishopping.app.quickadd_section.viewholders.VariantItemQuickAdd

class QuickVariantAdapter : RecyclerView.Adapter<VariantItemQuickAdd>() {
    private var variants: MutableList<String>? = null
    private var context: Context? = null
    private var selectedPosition = -1
    private var optionName: String? = null
    private var optionposition =0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VariantItemQuickAdd {
        val binding = DataBindingUtil.inflate<SwactchesListItemQuickcartBinding>(
            LayoutInflater.from(parent.context),
            R.layout.swactches_list_item_quickcart,
            parent,
            false
        )
        return VariantItemQuickAdd(binding)
    }

    companion object {
        var variantCallback: VariantCallback? = null
    }

    interface VariantCallback {
        fun clickVariant(variantName: String, optionName: String,optionposition:Int,position: Int)
    }


    override fun onBindViewHolder(holder: VariantItemQuickAdd, @SuppressLint("RecyclerView") position: Int) {
        holder.binding.variantName.text = variants?.get(position)
        if (selectedPosition == position) {
            var typeface = Typeface.createFromAsset(context!!.assets, "fonts/popnormal.ttf")
            holder.binding.variantName.setTypeface(typeface)
            holder.binding.variantName.background = ContextCompat.getDrawable(context!!, R.drawable.selectedlineborder)
            var drawable =holder.binding.variantName.background as GradientDrawable
            drawable.setStroke(2,Color.parseColor(NewBaseActivity.textColor))
            drawable.setColor(Color.parseColor(NewBaseActivity.themeColor))
            holder.binding.variantName.setTextColor(Color.parseColor(NewBaseActivity.textColor))
        }else {
            holder.binding.variantName.background = ContextCompat.getDrawable(context!!, R.drawable.lineborder)
            holder.binding.variantName.setTextColor(ContextCompat.getColor(HomePageViewModel.themedContext,R.color.normalgrey1text))
            var typeface = Typeface.createFromAsset(context!!.assets, "fonts/poplight.ttf")
            holder.binding.variantName.setTypeface(typeface)
        }
        holder.binding.variantName.setOnClickListener {
            selectedPosition = position
            variantCallback?.clickVariant(variants?.get(position) ?: "", optionName ?: "",optionposition,position)
            notifyDataSetChanged()
        }
        holder.setIsRecyclable(false)
    }
    override fun getItemCount(): Int {
        return variants?.size!!
    }
    fun setData(
        optionposition:Int,
        optionName: String,
        variants: MutableList<String>,
        context: Context,
        variantCallback_: VariantCallback
    ) {
        this.optionposition=optionposition
        this.optionName = optionName
        this.variants = variants
        variantCallback = variantCallback_
        this.context = context
    }
}
