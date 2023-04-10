package com.rasmishopping.app.basesection.models
import android.app.Activity
import android.graphics.*
import android.widget.ImageView
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.BindingAdapter
import androidx.databinding.library.baseAdapters.BR
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.rasmishopping.app.MyApplication.Companion.context
import com.rasmishopping.app.R
import com.rasmishopping.app.homesection.viewmodels.HomePageViewModel
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
fun <String> ImageView.loadCircularImage(
    model: kotlin.String,
    borderSize: Float = 0F,
    borderColor: Int = Color.TRANSPARENT
) {
    if (!(context as Activity).isDestroyed) {
        Glide.with(context)
            .asBitmap()
            .load(model)
                .placeholder(HomePageViewModel.getPlaceHolder())
                .error(HomePageViewModel.getPlaceHolder())
            .apply(RequestOptions.circleCropTransform())
            .into(object : BitmapImageViewTarget(this) {
                override fun setResource(resource: Bitmap?) {
                    setImageDrawable(
                        resource?.run {
                            RoundedBitmapDrawableFactory.create(
                                resources,
                                if (borderSize > 0) {
                                    createBitmapWithBorder(borderSize, borderColor)
                                } else {
                                    this
                                }
                            ).apply {
                                isCircular = true
                            }
                        }
                    )
                }
            })
    }
}
fun Bitmap.createBitmapWithBorder(borderSize: Float, borderColor: Int = Color.WHITE): Bitmap {
    val borderOffset = (borderSize * 2).toInt()
    val halfWidth = width / 2
    val halfHeight = height / 2
    val circleRadius = Math.min(halfWidth, halfHeight).toFloat()
    val newBitmap = Bitmap.createBitmap(
        width + borderOffset,
        height + borderOffset,
        Bitmap.Config.ARGB_8888
    )
    // Center coordinates of the image
    val centerX = halfWidth + borderSize
    val centerY = halfHeight + borderSize
    val paint = Paint()
    val canvas = Canvas(newBitmap).apply {
        // Set transparent initial area
        drawARGB(0, 0, 0, 0)
    }
    // Draw the transparent initial area
    paint.isAntiAlias = true
    paint.style = Paint.Style.FILL
    canvas.drawCircle(centerX, centerY, circleRadius, paint)
    // Draw the image
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(this, borderSize, borderSize, paint)
    // Draw the createBitmapWithBorder
    paint.xfermode = null
    paint.style = Paint.Style.STROKE
    paint.color = borderColor
    paint.strokeWidth = borderSize
    canvas.drawCircle(centerX, centerY, circleRadius, paint)
    return newBitmap
}

class CommanModel : BaseObservable() {
    @get:Bindable
    var imageurl: String? = null
        set(imageurl) {
            field = imageurl
            notifyPropertyChanged(BR.imageurl)
        }
    companion object {
        @BindingAdapter("imageUrl")
        @JvmStatic
        fun loadImage(view: ImageView, imageUrl: String?) {
            if (view.context != null) {
                Glide.with(context)
                    .asBitmap()
                    .load(imageUrl)
                    .placeholder(HomePageViewModel.getPlaceHolder())
                    .error(HomePageViewModel.getPlaceHolder())
                    .priority(Priority.HIGH)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(view)
            }
        }
        @BindingAdapter("RoundedimageUrl")
        @JvmStatic
        fun loadRoundedImage(view: ImageView, imageUrl: String?) {
            if (view.context!=null){
                val radius = context.resources.getDimensionPixelSize(R.dimen.height_20)
                Glide.with(context)
                    .asBitmap()
                    .transform(RoundedCorners(radius))
                    .load(imageUrl)
                    .placeholder(HomePageViewModel.getPlaceHolder())
                    .error(HomePageViewModel.getPlaceHolder())
                    .priority(Priority.HIGH)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(view)
            }
        }
        @BindingAdapter("circleImageUrl")
        @JvmStatic
        fun circleLoadImage(view: ImageView, imageUrl: String?) {
            val observable = Observable.fromCallable { imageUrl }
            observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(object : Observer<String?> {
                    override fun onSubscribe(d: Disposable) {
                    }
                    override fun onNext(s: String) {
                        if (view.context != null) {
                            view.loadCircularImage<String>(s, 2f, Color.parseColor(view.tag.toString()))
                        }
                    }
                    override fun onError(e: Throwable) {
                    }

                    override fun onComplete() {
                    }
                })
        }
    }
}
