package com.rasmishopping.app.productsection.activities

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.VideoView
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.databinding.VideoplayerBinding

//class VideoPlayerActivity : NewBaseActivity() {
//    private val TAG = "VideoPlayerActivity"
//  private lateinit var simpleExoPlayer: SimpleExoPlayer
//    private lateinit var mediaDataSourceFactory: DataSource.Factory
//    private var binding: VideoplayerBinding? = null
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        val group = findViewById<ViewGroup>(R.id.container)
//        binding =
//            DataBindingUtil.inflate(layoutInflater, R.layout.videoplayer, group, true)
//        showBackButton()
//        intent.getStringExtra("videoLink")?.let { initializePlayer(it) }
//    }
//
//
//    private fun initializePlayer(url: String) {
//        mediaDataSourceFactory =
//            DefaultDataSourceFactory(this, Util.getUserAgent(this, "mediaPlayerSample"))
//        val mediaSource = ProgressiveMediaSource.Factory(mediaDataSourceFactory)
//            .createMediaSource(MediaItem.fromUri(url))
//        val mediaSourceFactory: MediaSourceFactory =
//            DefaultMediaSourceFactory(mediaDataSourceFactory)
//        simpleExoPlayer = SimpleExoPlayer.Builder(this)
//            .setMediaSourceFactory(mediaSourceFactory)
//            .build()
//        simpleExoPlayer.setMediaSource(mediaSource)
//        simpleExoPlayer.prepare()
//        simpleExoPlayer.playWhenReady = true
//
//        binding?.videoplayer?.setShutterBackgroundColor(Color.BLACK)
//        binding?.videoplayer?.player = simpleExoPlayer
//        binding?.videoplayer?.requestFocus()
//    }
//
//    private fun releasePlayer() {
//       simpleExoPlayer.release()
//    }
//
//    public override fun onPause() {
//        super.onPause()
//
//        if (Util.SDK_INT <= 23) releasePlayer()
//    }
//
//    public override fun onStop() {
//        super.onStop()
//        if (Util.SDK_INT > 23) releasePlayer()
//    }
//}
class VideoPlayerActivity : NewBaseActivity() , MediaPlayer.OnCompletionListener {
    var vw: VideoView? = null
    var videolist: ArrayList<String> = ArrayList()
    var currvideo = 0
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.videoplayer)
        vw = findViewById(R.id.videoplayer) as VideoView?
        vw?.setMediaController(MediaController(this))
        vw?.setOnCompletionListener(this)
        showBackButton()

//         video name should be in lower case alphabet.
//        videolist.add(R.raw.middle)
//        videolist.add(R.raw.faded)
//        videolist.add(R.raw.aeroplane)
        setVideo(intent.getStringExtra("videoLink")!!)
    }

    fun setVideo(id: String) {
        val uriPath = id
        val uri: Uri = Uri.parse(uriPath)
        vw?.setVideoURI(uri)
        vw?.start()
    }

    override fun onCompletion(mediapalyer: MediaPlayer?) {
        val obj: AlertDialog.Builder = AlertDialog.Builder(this)
        obj.setTitle("Playback Finished!")
        obj.setIcon(R.mipmap.ic_launcher)
        val m: MyListener = MyListener()
        obj.setPositiveButton("Replay", m)
        obj.setNegativeButton("Next", m)
        obj.setMessage("Want to replay or play next video?")
        obj.show()
    }

    internal inner class MyListener : DialogInterface.OnClickListener {
        override fun onClick(dialog: DialogInterface?, which: Int) {
            if (which == -1) {
                vw?.seekTo(0)
                vw?.start()
            } else {

                setVideo(videolist[currvideo])
            }
        }
    }
}