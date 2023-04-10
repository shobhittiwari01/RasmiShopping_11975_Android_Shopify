package com.rasmishopping.app.customviews
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.viewpager.widget.ViewPager
import com.rasmishopping.app.R

class MageNativeViewPager(context: Context,attrs: AttributeSet) : ViewPager(context,attrs) {
    init {
        val ta = getContext().obtainStyledAttributes(attrs, R.styleable.MageNativeViewPager, 0, 0)
        ta.recycle()
    }
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightMeasureSpec = heightMeasureSpec
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var height = 0
        val childWidthSpec = MeasureSpec.makeMeasureSpec(
            Math.max(0, MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight),
            MeasureSpec.getMode(widthMeasureSpec)
        )
        val child: View = getChildAt(0)
        child.measure(childWidthSpec, MeasureSpec.UNSPECIFIED)
        val h: Int = child.getMeasuredHeight()
        if (h > height) height = h
        if (height != 0) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

}
