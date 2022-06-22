package com.example.mlkitzxing

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.mlkitzxing.databinding.ActivityMainBinding
import java.io.File
import java.io.FileWriter


class MainActivity : AppCompatActivity() {
    private val cameraPermissionRequestCode = 1
    private val REQUEST_CODE = 100
    private var clipManager: ClipboardManager? = null
    private var url: String? = ""
    private var title: String? = ""
    private var body: String? = ""
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        onCLickListeners()
        initialization()

    }


    private fun startScanning() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openCameraWithScanner()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                cameraPermissionRequestCode
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == cameraPermissionRequestCode && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCameraWithScanner()
            } else if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.CAMERA
                )
            ) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivityForResult(intent, cameraPermissionRequestCode)
            }
        }
    }

    private fun openCameraWithScanner() {
        binding.openBtn.visibility = View.GONE
        binding.shareBtn.visibility = View.GONE
        binding.copyBtn.visibility = View.GONE
        binding.saveBtn.visibility = View.GONE
        val intent = Intent(this, BarcodeScanningActivity::class.java)

        startActivityForResult(intent, REQUEST_CODE)
    }

 var scanBarcodeActivityResultLauncher=registerForActivityResult(
     StartActivityForResult(),
     ActivityResultCallback {  }
 )



    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == cameraPermissionRequestCode) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                openCameraWithScanner()
            }
        }

        if (requestCode == REQUEST_CODE) {
            if (data != null) {
                if (data.getParcelableExtra<WifiModel>("barcodeWifi") != null) {
                    val barcode = data.getParcelableExtra<WifiModel>("barcodeWifi")
                    Glide.with(this).load(barcode!!.img).into(binding.imgV)
                    binding.result.post {binding.result.text = barcode.ssid + "\n" + barcode.password + "\n" + barcode.type.toString() }
                    url = ""
                    title = "ssid: " + barcode.ssid
                    body = "Password: " + barcode.password + "\n" + "Type: " + barcode.type
                    binding.shareBtn.visibility = View.VISIBLE
                    binding.copyBtn.visibility = View.VISIBLE
                    binding.saveBtn.visibility = View.VISIBLE
                } else if (data.getParcelableExtra<UrlModel>("barcodeUrl") != null) {
                    val barcode = data.getParcelableExtra<UrlModel>("barcodeUrl")

                    Glide.with(this).load(barcode!!.img).into(binding.imgV)
                    binding.result.post {
                        binding.result.text = barcode.title + "\n" + barcode.url
                    }
                    url = barcode.url
                    title = barcode.title
                    body = ""
                    binding.openBtn.visibility = View.VISIBLE
                    binding.shareBtn.visibility = View.VISIBLE
                    binding.copyBtn.visibility = View.VISIBLE
                    binding.saveBtn.visibility = View.VISIBLE
                } else if (data.getParcelableExtra<textModel>("barcodeTxt") != null) {
                    val barcode = data.getParcelableExtra<textModel>("barcodeTxt")
                    Glide.with(this).load(barcode!!.bit).into(binding.imgV)
                    binding.result.post {
                        binding.result.text = barcode.txt
                    }
                    url = ""
                    title = ""
                    body = barcode.txt
                    binding.shareBtn.visibility = View.VISIBLE
                    binding.copyBtn.visibility = View.VISIBLE
                    binding.saveBtn.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun initialization() {
        clipManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    private fun onCLickListeners() {
        binding.scanbtn.setOnClickListener {
            startScanning()
        }

        binding.openBtn.setOnClickListener {
            if (url != null) {
                gotoUrl(url!!)
            }
        }
        binding.shareBtn.setOnClickListener {
            share(title!!, body!! + url)
        }
        binding.copyBtn.setOnClickListener {
            val str = title + "\n" + body + url
            clipManager!!.text = str
            Toast.makeText(this, "Copied To Clipboard", Toast.LENGTH_SHORT).show()
        }
        binding.saveBtn.setOnClickListener {
            val textToSaveString = title + "\n" + body + url
            writeToFile(title, textToSaveString)
        }

    }

    private fun writeToFile(title: String?, data: String) {
        val file = File(filesDir, "text")
        if (!file.exists()) {
            file.mkdir()
        }
        val gPXFile = File(file, "QR $title")
        val writer = FileWriter(gPXFile)
        writer.append(data)
        writer.flush()
        writer.close()
        Toast.makeText(this, "Saved your text", Toast.LENGTH_LONG).show()
    }

    private fun gotoUrl(s: String) {
        val uri = Uri.parse(s)
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }

    private fun share(headerText: String, bodyText: String) {
        ShareCompat.IntentBuilder.from(this)
            .setType("text/plain")
            .setChooserTitle(headerText)
            .setText(bodyText)
            .startChooser()
    }


}