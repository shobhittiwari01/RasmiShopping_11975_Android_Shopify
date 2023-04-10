package com.rasmishopping.app.customviews
import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import com.rasmishopping.app.R
class MageDotsIndicator(context: Context?, attrs: AttributeSet) : LinearLayout(context, attrs) {
    var selection: Int = 0
        private set
    private var dotsCount: Int = 2
    private var dotHeight: Int = 7
    private var dotWidth: Int = 7
    var marginsBetweenDots: Int =17
    var selectedDotScaleFactor: Float = 1.4f
    var selectedDotResource: Int = R.drawable.activedot
    var unselectedDotResource: Int = R.drawable.inactivedot
    var activedotcolor: String = "#FFFFFF"
    var inactivedotcolor: String = "#F2F2F2"
    var onSelectListener: ((position: Int) -> Unit)? = null

    init {
        orientation = HORIZONTAL
        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        gravity = Gravity.CENTER
        val ta = getContext().obtainStyledAttributes(attrs, R.styleable.MageDotsIndicator, 0, 0)
        dotsCount = ta.getInt(R.styleable.MageDotsIndicator_dots_count, 3)
        selectedDotScaleFactor = ta.getFloat(R.styleable.MageDotsIndicator_selected_dot_scale_factor, 1.4f)
        selectedDotResource = ta.getResourceId(R.styleable.MageDotsIndicator_selected_dot_resource, selectedDotResource)
        unselectedDotResource = ta.getResourceId(R.styleable.MageDotsIndicator_unselected_dot_resource, unselectedDotResource)
        dotHeight = ta.getDimensionPixelSize(R.styleable.MageDotsIndicator_dot_height, dotHeight)
        dotWidth = ta.getDimensionPixelSize(R.styleable.MageDotsIndicator_dot_width, dotWidth)
        marginsBetweenDots = ta.getDimensionPixelSize(R.styleable.MageDotsIndicator_margins_between_dots, marginsBetweenDots)
        initDots(dotsCount,activedotcolor,inactivedotcolor)
        ta.recycle()
    }
    fun initDots(dotsCount: Int,activedotcolor:String,inactivedotcolor:String) {
        removeAllViews()
        this.activedotcolor=activedotcolor
        this.inactivedotcolor=inactivedotcolor
        for (i: Int in 0 until dotsCount) {
            val dot = ImageView(context)
            dot.id = i
            dot.tag = i
            dot.layoutParams = LayoutParams(dotHeight, dotWidth).apply {
                this.marginEnd = marginsBetweenDots
                this.gravity = Gravity.CENTER_VERTICAL
            }
            dot.scaleType = ImageView.ScaleType.FIT_XY
            dot.setImageResource(R.drawable.activedot)
            if (selection == i) {
                dot.setColorFilter(Color.parseColor(this.activedotcolor),PorterDuff.Mode.SRC)
            } else {
                dot.setColorFilter(Color.parseColor(this.inactivedotcolor),PorterDuff.Mode.SRC)
            }
            if (selection == i) {
                dot.scaleX = selectedDotScaleFactor
                dot.scaleY = selectedDotScaleFactor
            }
            dot.setOnClickListener {
                onSelectListener?.invoke(it.tag as Int)
                setDotSelection(it.tag as Int)
            }
            addView(dot)
        }
        setDotSelection(selection)
    }
    fun setDotSelection(position: Int) {
        if (position == selection)
            return
        val newSelection: ImageView = findViewById(position)
        val selectedDot: ImageView = findViewWithTag(selection)
        val increaseAnimator = ValueAnimator.ofFloat(1f, selectedDotScaleFactor)
        val decreaseAnimator = ValueAnimator.ofFloat(selectedDotScaleFactor, 1f)
        increaseAnimator.addUpdateListener { animator ->
            val value: Float = animator.animatedValue as Float
            newSelection.scaleX = value
            newSelection.scaleY = value
        }
        decreaseAnimator.addUpdateListener {
            val value: Float = it.animatedValue as Float
            selectedDot.scaleX = value
            selectedDot.scaleY = value
        }
        increaseAnimator.start()
        decreaseAnimator.start()
        val animationListener = object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                newSelection.scaleX = selectedDotScaleFactor
                newSelection.scaleY = selectedDotScaleFactor

                selectedDot.scaleX = 1f
                selectedDot.scaleY = 1f
            }
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationStart(animation: Animator) {}
        }
        newSelection.setImageResource(selectedDotResource)
        selectedDot.setImageResource(unselectedDotResource)
        newSelection.setColorFilter(Color.parseColor(this.activedotcolor),PorterDuff.Mode.SRC)
        selectedDot.setColorFilter(Color.parseColor(this.inactivedotcolor),PorterDuff.Mode.SRC)
        selection = newSelection.tag as Int
    }
}