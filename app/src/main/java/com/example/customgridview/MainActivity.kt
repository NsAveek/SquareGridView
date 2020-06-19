package com.example.customgridview

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import jp.co.cyberagent.android.gpuimage.GPUImageView
import jp.co.cyberagent.android.gpuimage.filter.GPUImageHueFilter
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    private lateinit var customGridView : SquareGridCustomViewConstraintLayout
    private lateinit var gpuImageView : GPUImageView
    private lateinit var publishSubject : PublishSubject<Bitmap>
    private lateinit var disposable: Disposable

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        initPublishSubject()
        initCustomView()
    }

    private fun initPublishSubject() {
        publishSubject = PublishSubject.create()
        disposable = publishSubject.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {(
                    initGPUImageView(getImageUri(it))
                    )
            }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun initCustomView(){
        customGridView = findViewById(R.id.squareGridView)
        customGridView.setTouchListener(publishSubject)
    }
    private fun initGPUImageView(imageUri : Uri){
        gpuImageView = findViewById(R.id.gpuimageview)
        thread {
            gpuImageView.setImage(imageUri) // this loads image on the current thread, should be run in a thread
        }
        gpuImageView.filter = GPUImageHueFilter()
    }

    private fun getImageUri(inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()

        val extStorageDirectory = Environment.getExternalStorageDirectory().toString()
        var outStream: OutputStream? = null
        val file = File(extStorageDirectory, "er.PNG")
        try {
            outStream = FileOutputStream(file)
            inImage.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
//            bbicon.compress(Bitmap.CompressFormat.PNG, 100, outStream)
            outStream.flush()
            outStream.close()
        } catch (e: Exception) {
        }
//        val path = MediaStore.Images.Media.insertImage(
//            contentResolver,
//            inImage,
//            "Title",
//            null
//        )
        return Uri.parse(file.path)
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
