package com.example.customgridview


import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.AlertDialog
import android.content.ContentValues
import android.content.DialogInterface
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
import com.google.android.material.snackbar.Snackbar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import jp.co.cyberagent.android.gpuimage.GPUImageView
import jp.co.cyberagent.android.gpuimage.filter.GPUImageDarkenBlendFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageHueFilter
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    private lateinit var customGridView : SquareGridCustomViewConstraintLayout
    private lateinit var gpuImageView : GPUImageView
    private lateinit var publishSubject : PublishSubject<Bitmap>
    private lateinit var disposable: Disposable
    private val PERMISSION_REQUEST_CODE = 200


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
//        gpuImageView.filter = GPUImageDarkenBlendFilter()
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun getImageUri(image : Bitmap) : Uri{
        val relativeLocation = Environment.DIRECTORY_PICTURES + File.pathSeparator + "customgrid"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, System.currentTimeMillis().toString())
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativeLocation)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val resolver = applicationContext.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        try {

            uri?.let { uri ->
                val stream = resolver.openOutputStream(uri)

                stream?.let { stream ->
                    if (!image.compress(Bitmap.CompressFormat.JPEG, 100, stream)) {
                        throw IOException("Failed to save bitmap.")
                    }
                } ?: throw IOException("Failed to get output stream.")

            } ?: throw IOException("Failed to create new MediaStore record")

        } catch (e: IOException) {
            if (uri != null) {
                resolver.delete(uri, null, null)
            }
            throw IOException(e)
        } finally {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
        }
        resolver.update(uri!!, contentValues, null, null)
        return uri
    }

    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(applicationContext, WRITE_EXTERNAL_STORAGE)
        val result1 = ContextCompat.checkSelfPermission(applicationContext, READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> if (grantResults.isNotEmpty()) {
                val writeAccepted =
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                val readAccepted =
                    grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (writeAccepted && readAccepted){
                    Toast.makeText(this,"Permission Granted, Now you can read and write data",Toast.LENGTH_SHORT).show()

                }  else {
                    Toast.makeText(this,"Permission Denied, You can not read and write data",Toast.LENGTH_SHORT).show()

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {

                            val builder = AlertDialog.Builder(this@MainActivity)
                            builder.setTitle("Permission Required")
                            builder.setMessage("You need to allow access to both the permissions")
                            builder.setPositiveButton("YES"){dialog, which ->
                                requestPermissions(arrayOf( WRITE_EXTERNAL_STORAGE,READ_EXTERNAL_STORAGE),
                                    PERMISSION_REQUEST_CODE
                                )
                            }
                            builder.setNegativeButton("Cancel"){dialog,which ->
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
}
