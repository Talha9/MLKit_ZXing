package com.example.mlkitzxing

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.MotionEvent
import android.view.OrientationEventListener
import android.view.Surface
import android.view.View
import android.widget.SeekBar
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.mlkitzxing.databinding.ActivityBarcodeScanningBinding
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class BarcodeScanningActivity : AppCompatActivity() {
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var binding: ActivityBarcodeScanningBinding
    /** Blocking camera operations are performed using this executor */
    var  camera: Camera? = null
    private lateinit var cameraExecutor: ExecutorService
    private var flashEnabled = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityBarcodeScanningBinding.inflate(layoutInflater)
        setContentView(binding.root)


        initialization()
        onClickListeners()

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun onClickListeners() {
        binding.zoomSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {

                camera!!.cameraControl.setLinearZoom(progress / 100.toFloat())

            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        binding.cameraPreview.setOnTouchListener { _, event ->
            val actionMasked = event.actionMasked // Or action
            if (actionMasked != MotionEvent.ACTION_DOWN) {
                return@setOnTouchListener false
            }
            val x = event.x
            val y = event.y
            val factory = binding.cameraPreview.meteringPointFactory
            val point = factory.createPoint(x, y)
            val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
                .addPoint(point, FocusMeteringAction.FLAG_AE)
                .addPoint(point, FocusMeteringAction.FLAG_AWB)
                .build()
            val future = camera!!.cameraControl.startFocusAndMetering(action)
            future.addListener(
                {
                    try {
                        val result = future.get()
                        Log.d("tag", "Focus Success: ${result.isFocusSuccessful}")
                    } catch (e: ExecutionException) {
                        Log.e("tag", "Focus failed", e)
                    } catch (e: InterruptedException) {
                        Log.e("tag", "Focus interrupted", e)
                    }
                }, Executors.newSingleThreadExecutor()
            )
            return@setOnTouchListener true
        }

    }

    private fun initialization() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        // Initialize our background executor
        cameraExecutor = Executors.newSingleThreadExecutor()


        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(this))


        binding.overlay.post {
            binding.overlay.setViewFinder()
        }

    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun bindPreview(cameraProvider: ProcessCameraProvider?) {

        if (isDestroyed || isFinishing) {
            //This check is to avoid an exception when trying to re-bind use cases but user closes the activity.
            //java.lang.IllegalArgumentException: Trying to create use case mediator with destroyed lifecycle.
            return
        }

        cameraProvider?.unbindAll()

        val preview: Preview = Preview.Builder()
            .build()


        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(binding.cameraPreview.width, binding.cameraPreview.height))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        val orientationEventListener = object : OrientationEventListener(this as Context) {
            override fun onOrientationChanged(orientation : Int) {
                // Monitors orientation values to determine the target rotation value
                val rotation : Int = when (orientation) {
                    in 45..134 -> Surface.ROTATION_270
                    in 135..224 -> Surface.ROTATION_180
                    in 225..314 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }

                imageAnalysis.targetRotation = rotation
            }
        }
        orientationEventListener.enable()

        //switch the analyzers here, i.e. MLKitBarcodeAnalyzer, ZXingBarcodeAnalyzer
        class ScanningListener : ScanningResultListener {
            override fun onScanned(result: String, bit: Bitmap) {
                runOnUiThread {
                    imageAnalysis.clearAnalyzer()
                    cameraProvider?.unbindAll()
                    val intent = Intent()
                    intent.putExtra(
                        "barcodeTxt", textModel(result,bit))
                    setResult(RESULT_OK, intent)
                    Log.d("scanFromGalleryTAG", "scanImage: " + "Scanned Data")
                    finish()
                }
            }
        }

        val analyzer: ImageAnalysis.Analyzer = BarcodeAnalyzer(ScanningListener(),this)


        imageAnalysis.setAnalyzer(cameraExecutor, analyzer)

        preview.setSurfaceProvider(binding.cameraPreview.surfaceProvider)

         camera = cameraProvider?.bindToLifecycle(this, cameraSelector, imageAnalysis, preview)

        if (camera?.cameraInfo?.hasFlashUnit() == true) {
            binding.ivFlashControl.visibility = View.VISIBLE

            binding.ivFlashControl.setOnClickListener {
                camera!!.cameraControl.enableTorch(!flashEnabled)
            }

            camera!!.cameraInfo.torchState.observe(this) {
                it?.let { torchState ->
                    if (torchState == TorchState.ON) {
                        flashEnabled = true
                        binding.ivFlashControl.setImageResource(R.drawable.ic_round_flash_on)
                    } else {
                        flashEnabled = false
                        binding.ivFlashControl.setImageResource(R.drawable.ic_round_flash_off)
                    }
                }
            }

                camera!!.cameraControl.setLinearZoom(15/ 100.toFloat())
                val x: Int = binding.cameraPreview.width/2
                val y: Int = binding.cameraPreview.height/ 2
                val factory = binding.cameraPreview.meteringPointFactory
                val point = factory.createPoint(x.toFloat(), y.toFloat())
                val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
                    .addPoint(point, FocusMeteringAction.FLAG_AE)
                    .addPoint(point, FocusMeteringAction.FLAG_AWB)
                    .build()

                val future = camera!!.cameraControl.startFocusAndMetering(action)
                future.addListener(
                    {
                        try {
                            val result = future.get()
                            Log.d("tag", "Focus Success: ${result.isFocusSuccessful}")
                        } catch (e: ExecutionException) {
                            Log.e("tag", "Focus failed", e)
                        } catch (e: InterruptedException) {
                            Log.e("tag", "Focus interrupted", e)
                        }
                    }, Executors.newSingleThreadExecutor()
                )

        }


    }

    override fun onDestroy() {
        super.onDestroy()
        // Shut down our background executor
        cameraExecutor.shutdown()
    }
}