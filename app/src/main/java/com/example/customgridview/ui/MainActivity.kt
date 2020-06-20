package com.example.customgridview.ui

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.AlertDialog
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.customgridview.BaseActivity
import com.example.customgridview.R
import com.example.customgridview.customview.SquareGridCustomView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import jp.co.cyberagent.android.gpuimage.GPUImageView
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGrayscaleFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageHueFilter
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import kotlin.concurrent.thread


class MainActivity : BaseActivity() {

    private lateinit var customGridView : SquareGridCustomView
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
        if (!checkPermission()){
            requestPermission()
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun initPublishSubject() {
        publishSubject = PublishSubject.create()
        disposable = publishSubject.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {(initGPUImageView(getImageUriFromBitmap(it)))}
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun initCustomView(){
        customGridView = findViewById(R.id.squareGridView)
        customGridView.setTouchListener(publishSubject)
    }
    private fun initGPUImageView(imageUri : Uri){
        gpuImageView = findViewById(R.id.gpuimageview)
        thread {
            gpuImageView.setImage(imageUri)
        }
        gpuImageView.filter = GPUImageHueFilter()
        gpuImageView.filter = GPUImageGrayscaleFilter()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> if (grantResults.isNotEmpty()) {
                val writeAccepted =
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                val readAccepted =
                    grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (writeAccepted && readAccepted){
                    Toast.makeText(this,getString(R.string.permission_granted),Toast.LENGTH_SHORT).show()

                }  else {
                    Toast.makeText(this,getString(R.string.permission_denied),Toast.LENGTH_SHORT).show()

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {

                            val builder = AlertDialog.Builder(this@MainActivity)
                            builder.setTitle(getString(R.string.permission_dialogue_title))
                            builder.setMessage(getString(R.string.permission_dialogue_message))
                            builder.setPositiveButton(getString(R.string.dialogue_positive_text)){dialog, which ->
                                requestPermissions(arrayOf( WRITE_EXTERNAL_STORAGE,READ_EXTERNAL_STORAGE),
                                    PERMISSION_REQUEST_CODE
                                )
                            }
                            builder.setNegativeButton(getString(R.string.dialogue_cancel_text)){dialog,which ->
                                requestPermissions(arrayOf( WRITE_EXTERNAL_STORAGE,READ_EXTERNAL_STORAGE),
                                    PERMISSION_REQUEST_CODE
                                )
                            }
                            val dialog: AlertDialog = builder.create()
                            dialog.show()
                            return
                        }
                    }
                }
            }else{
                requestPermission()
            }
        }
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

    override fun onPause() {
        super.onPause()
        disposable.dispose()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        if(disposable != null && !disposable.isDisposed) {
            disposable.dispose();
        }
    }

    override fun onStop() {
        super.onStop()
    }
}