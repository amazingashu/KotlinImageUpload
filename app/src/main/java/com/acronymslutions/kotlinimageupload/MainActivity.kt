package com.acronymslutions.kotlinimageupload

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri

import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.webkit.PermissionRequest
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity() , AsyncTaskCompleteListener {

    private var btn: Button? = null

    private var tv: TextView? = null

    private var imageview: ImageView? = null

    private val CAMERA = 1

    internal var uploadURL = "http://acronymsolutions.in/jsondata/imageUpload.php"

    var arraylist: ArrayList<HashMap<String, String>>? = null

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestMultiplePermissions()

        btn = findViewById<View>(R.id.btn) as Button
        tv = findViewById<View>(R.id.tv) as TextView
        imageview = findViewById<View>(R.id.iv) as ImageView

        btn!!.setOnClickListener {
            val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, CAMERA)
        }

    }

    public override fun onActivityResult(requestCode:Int, resultCode:Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA) {
            if (data != null)
            {
                val contentURI = data!!.data
                try
                {
                    val bitmap = data.extras!!.get("data") as Bitmap
                    val path = saveImage(bitmap)
                    Toast.makeText(this@MainActivity, "Image Saved!", Toast.LENGTH_SHORT).show()
                    imageview!!.setImageBitmap(bitmap)

                    uploadImage(path)

                }
                catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this@MainActivity, "Failed!", Toast.LENGTH_SHORT).show()
                }

            }

        }

    }

    private fun uploadImage(path: String) {

        val map = HashMap<String, String>()
        map.put("url", uploadURL)
        map.put("filename", path)
        MultiPartRequester(this, map, CAMERA, this)
    }

    override fun onTaskCompleted(response: String, serviceCode: Int) {

        Log.d("respon", response.toString())
        when (serviceCode) {
            CAMERA -> if (isSuccess(response))
            {
                val url = getURL(response)
                tv!!.text = url

                tv!!.setOnClickListener(View.OnClickListener {
                    val browserIntent = Intent(Intent.ACTION_VIEW)
                    browserIntent.data = Uri.parse(url)
                    startActivity(browserIntent)
                })

            }
        }

    }

    fun isSuccess(response: String): Boolean {

        try {
            val jsonObject = JSONObject(response)
            return jsonObject.optString("status") == "true"

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return false
    }


    fun getURL(response:String):String {
        var url = ""
        try
        {
            val jsonObject = JSONObject(response)
            jsonObject.toString().replace("\\\\", "")
            if (jsonObject.getString("status").equals("true"))
            {
                arraylist = ArrayList<HashMap<String, String>>()
                val dataArray = jsonObject.getJSONArray("data")
                for (i in 0 until dataArray.length())
                {
                    val dataobj = dataArray.getJSONObject(i)
                    url = dataobj.optString("FilePath")
                }
            }
        }
        catch (e: JSONException) {
            e.printStackTrace()
        }
        return url
    }

    fun saveImage(myBitmap: Bitmap):String {
        val bytes = ByteArrayOutputStream()
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
        val wallpaperDirectory = File(
            (Environment.getExternalStorageDirectory()).toString() + IMAGE_DIRECTORY)
        // have the object build the directory structure, if needed.
        Log.d("fee",wallpaperDirectory.toString())
        if (!wallpaperDirectory.exists())
        {

            wallpaperDirectory.mkdirs()
        }

        try
        {
            Log.d("heel",wallpaperDirectory.toString())
            val f = File(wallpaperDirectory, ((Calendar.getInstance()
                .getTimeInMillis()).toString() + ".jpg"))
            f.createNewFile()
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
            MediaScannerConnection.scanFile(this,
                arrayOf(f.getPath()),
                arrayOf("image/jpeg"), null)
            fo.close()
            Log.d("TAG", "File Saved::--->" + f.getAbsolutePath())

            return f.getAbsolutePath()
        }
        catch (e1: IOException) {
            e1.printStackTrace()
        }

        return ""
    }

    companion object {
        private val IMAGE_DIRECTORY = "/KotlinImage"
    }

    private fun requestMultiplePermissions() {
        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    // check if all permissions are granted
                    if (report.areAllPermissionsGranted()) {
                        Toast.makeText(applicationContext, "Permissions are granted", Toast.LENGTH_SHORT)
                            .show()
                    }

                    // check for permanent denial of any permission
                    if (report.isAnyPermissionPermanentlyDenied) {
                        // show alert dialog navigating to Settings
                        //openSettingsDialog()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    if (token != null) {
                        token.continuePermissionRequest()
                    }
                }
            }).withErrorListener { Toast.makeText(applicationContext, "Error Occured! ", Toast.LENGTH_SHORT).show() }
            .onSameThread()
            .check()
    }

}