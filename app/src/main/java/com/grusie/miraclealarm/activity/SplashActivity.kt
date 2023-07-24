package com.grusie.miraclealarm.activity

import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.grusie.miraclealarm.R
import kotlinx.coroutines.*
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.grusie.miraclealarm.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageLoadListener = object : RequestListener<GifDrawable>{
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<GifDrawable>?,
                isFirstResource: Boolean
            ): Boolean {
                Log.d("confirm failure", "fail, ${e?.message}")
                navigateToMainActivity()
                return false
            }

            override fun onResourceReady(
                resource: GifDrawable?,
                model: Any?,
                target: Target<GifDrawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                resource?.setLoopCount(1)
                resource?.registerAnimationCallback(object : Animatable2Compat.AnimationCallback(){
                    override fun onAnimationEnd(drawable: Drawable?) {
                        navigateToMainActivity()
                        super.onAnimationEnd(drawable)
                    }
                })
                return false
            }
        }

        Glide.with(this).asGif().listener(imageLoadListener).load(R.drawable.splash_alarm).into(binding.ivSplash)
    }

    override fun onDestroy() {
        super.onDestroy()
        Glide.with(applicationContext).clear(binding.ivSplash)
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this@SplashActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
