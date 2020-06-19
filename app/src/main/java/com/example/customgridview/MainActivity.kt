package com.example.customgridview

import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import jp.co.cyberagent.android.gpuimage.GPUImageView
import jp.co.cyberagent.android.gpuimage.filter.GPUImageHueFilter
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var customGridView : SquareGridCustomViewConstraintLayout
    private lateinit var gpuImageView : GPUImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        initCustomView()
        initGPUImageView()
    }

    private fun initCustomView(){
        customGridView = findViewById(R.id.squareGridView)
    }
    private fun initGPUImageView(){
        val imageUri: Uri = (Uri.Builder())
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(resources.getResourcePackageName(R.drawable.nature))
            .appendPath(resources.getResourceTypeName(R.drawable.nature))
            .appendPath(resources.getResourceEntryName(R.drawable.nature))
            .build()
        gpuImageView = findViewById(R.id.gpuimageview)
        thread {
            gpuImageView.setImage(imageUri) // this loads image on the current thread, should be run in a thread
        }
        gpuImageView.filter = GPUImageHueFilter()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
