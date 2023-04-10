package com.rasmishopping.app.productsection.fragments
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.fragments.BaseFragment
import com.rasmishopping.app.basesection.models.CommanModel
import com.rasmishopping.app.databinding.MImagefragmentBinding
import com.rasmishopping.app.productsection.activities.VideoPlayerActivity
import com.rasmishopping.app.productsection.activities.ZoomActivity
import com.rasmishopping.app.productsection.models.MediaModel
import com.rasmishopping.app.utils.Constant
import java.util.*
class ImageFragment : BaseFragment(), View.OnClickListener {
    private var linkType: String? = null
    private var videoLink: String? = null
    private var mediaList: String? = null
    private var mediaImage: String? = null
    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<MImagefragmentBinding>(
            layoutInflater,
            R.layout.m_imagefragment,
            null,
            false
        )
        val mediaModel = Objects.requireNonNull<Bundle>(arguments).getSerializable("mediaModel") as MediaModel
        mediaList = Objects.requireNonNull<Bundle>(arguments).getString("mediaList")
        val url = mediaModel.previewUrl
        mediaImage=url
        linkType = mediaModel.typeName
        videoLink = mediaModel.mediaUrl
        if (linkType.equals("Video") || linkType.equals("ExternalVideo")) {
            binding.playButton.visibility = View.VISIBLE
        } else {
            binding.playButton.visibility = View.GONE
        }
        val model = CommanModel()
        model.imageurl = url!!
        binding.commondata = model
        binding.image.setOnClickListener(this)
        return binding.root
    }
    override fun onClick(v: View?) {
        if (linkType.equals("Video") || linkType.equals("ExternalVideo")) {
            if (videoLink?.contains("youtu")!!) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(videoLink)))
                Constant.activityTransition(v?.context!!)
            } else {
                var intent = Intent(context, VideoPlayerActivity::class.java)
                intent.putExtra("videoLink", videoLink)
                context?.startActivity(intent)
                Constant.activityTransition(v?.context!!)
            }
        } else {
            var intent = Intent(context, ZoomActivity::class.java)
            intent.putExtra("imageslist", mediaList)
            intent.putExtra("images", mediaImage)
            context?.startActivity(intent)
            Constant.activityTransition(v?.context!!)
        }
    }
}