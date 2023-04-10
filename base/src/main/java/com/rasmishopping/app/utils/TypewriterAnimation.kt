package com.rasmishopping.app.utils
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.TextView
class TypewriterAnimation(private val text: String, private val textView: TextView) : Animation()  {
    private var index: Int = 0
    private val words: List<String> = text.split("\\s+".toRegex())
    override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
        if (index < words.size) {
            textView.text = textView.text.toString() + " " + words[index]
            index++
        }
        Handler(Looper.myLooper()!!).postDelayed({
            applyTransformation(interpolatedTime, t)
        }, getNextDelay())
    }
    private fun getNextDelay(): Long {
        // Generate a random delay between 50ms and 200ms
        return 200
    }
}