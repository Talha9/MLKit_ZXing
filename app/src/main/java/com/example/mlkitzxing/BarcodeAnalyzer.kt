package com.example.mlkitzxing

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.sqrt


class BarcodeAnalyzer(private val listener: ScanningResultListener, var mContext: Context) :
    ImageAnalysis.Analyzer {
    private var croppedBmp: Bitmap? = null
    private var multiFormatReader: MultiFormatReader = MultiFormatReader()
    private var isScanning = AtomicBoolean(false)
    var chk = true

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(proxy: ImageProxy) {

        if (isScanning.get()) {
            proxy.close()
            return
        }
        Log.d("CheckTAG", "analyze: " + "ZXing")

        isScanning.set(true)

        val mediaImage = proxy.image
        if (mediaImage != null) {
            val ori: Bitmap = proxy.toBitmap()!!
            val image = InputImage.fromMediaImage(mediaImage, 0)
            val scanner = BarcodeScanning.getClient()

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    /* barcodes.firstOrNull().let { barcode ->
                         Log.d("TAGScanner", "analyze: "+"MLKit")
                         val rawValue = barcode?.rawValue
                         rawValue?.let {

                            *//* Log.d("---->BarcodeMLKit", it)
                            listener.onScanned(it)
                            Toast.makeText(mContext,"MLKit Scanning",Toast.LENGTH_SHORT).show()
                            isScanning.set(false)*//*
                        }
                    }*/

                    /* Log.d("TAGScanner", "analyze: " + "MLKit")
                     CoroutineScope(Dispatchers.IO).launch {
                         for (barcode in barcodes) {
                             when (barcode.valueType) {
                                 Barcode.TYPE_WIFI -> {
                                     Log.d("---->BarcodeMLKit:", barcode.displayValue!!)
                                     val bbox = barcode.boundingBox
                                     if (bbox!!.left >= 10 && bbox.top >= 10 && bbox.left + bbox.width() + 40 <= ori.width && bbox.top + bbox.height() + 40 <= ori.height) {
                                         croppedBmp = Bitmap.createBitmap(
                                             ori,
                                             bbox.left - 10,
                                             bbox.top - 10,
                                             bbox.width() + 20,
                                             bbox.height() + 20
                                         )

                                         val ssid = barcode.wifi!!.ssid
                                         val password = barcode.wifi!!.password
                                         val type = barcode.wifi!!.encryptionType
                                         withContext(Dispatchers.Main) {
                                             listener.onScanned(
                                                 ssid!! + "\n" + password + "\n" + type,
                                                 Bitmap.createScaledBitmap(
                                                     croppedBmp!!,
                                                     100,
                                                     100,
                                                     true
                                                 )
                                             )
                                             isScanning.set(false)
                                             if(chk){
                                                 Toast.makeText(mContext, "MLKit Scanning", Toast.LENGTH_SHORT).show()
                                                 chk=false
                                             }
                                         }

                                     }

                                 }
                                 Barcode.TYPE_URL -> {
                                     Log.d("---->BarcodeMLKit:", barcode.displayValue!!)
                                     val bbox = barcode.boundingBox
                                     if (bbox!!.left >= 10 && bbox.top >= 10 && bbox.left + bbox.width() + 40 <= ori.width && bbox.top + bbox.height() + 40 <= ori.height) {
                                         croppedBmp = Bitmap.createBitmap(
                                             ori,
                                             bbox.left - 10,
                                             bbox.top - 10,
                                             bbox.width() + 20,
                                             bbox.height() + 20
                                         )
                                         val title = barcode.url!!.title
                                         val url = barcode.url!!.url
                                         withContext(Dispatchers.Main) {
                                             listener.onScanned(
                                                 title!! + "\n" + url,
                                                 Bitmap.createScaledBitmap(
                                                     croppedBmp!!,
                                                     100,
                                                     100,
                                                     true
                                                 )
                                             )
                                             isScanning.set(false)
                                             if(chk){
                                                 Toast.makeText(mContext, "MLKit Scanning", Toast.LENGTH_SHORT).show()
                                                 chk=false
                                             }
                                         }

                                     }

                                 }
                                 else -> {
                                     Log.d("---->BarcodeMLKit:", barcode.displayValue!!)
                                     val bbox = barcode.boundingBox
                                     if (bbox!!.left >= 20 && bbox.top >= 20 && bbox.left + bbox.width() + 40 <= ori.width && bbox.top + bbox.height() + 40 <= ori.height) {
                                         croppedBmp = Bitmap.createBitmap(
                                             ori,
                                             bbox.left - 20,
                                             bbox.top - 20,
                                             bbox.width() + 40,
                                             bbox.height() + 40
                                         )

                                         val txt = barcode.displayValue
                                         withContext(Dispatchers.Main) {
                                             listener.onScanned(
                                                 txt!!,
                                                 Bitmap.createScaledBitmap(
                                                     croppedBmp!!,
                                                     200,
                                                     200,
                                                     true
                                                 )
                                             )
                                             isScanning.set(false)
                                             if(chk){
                                                 Toast.makeText(mContext, "MLKit Scanning", Toast.LENGTH_SHORT).show()
                                                 chk=false
                                             }
                                         }
                                     }
                                 }
                             }
                         }
                     }*/
                }
                .addOnFailureListener {
                    Log.d("ScanningTAG", "analyze: " + "Fail")
                    isScanning.set(false)

                }
                .addOnCompleteListener {
                    if (isScanning.get()) {
                        zXingInitiator(image, proxy)
                    }


                }

        }
    }

    private fun zXingInitiator(image: InputImage, imageProxy: ImageProxy) {
        Log.d("TAGScanner", "analyze: " + "ZXing")
        if ((image.format == ImageFormat.YUV_420_888 || image.format == ImageFormat.YUV_422_888 || image.format == ImageFormat.YUV_444_888) && image.planes!!.size == 3) {
            val rotatedImage = RotatedImage(
                getLuminancePlaneData(imageProxy),
                image.width,
                image.height
            )

            rotateImageArray(rotatedImage, imageProxy.imageInfo.rotationDegrees)
            val planarYUVLuminanceSource = PlanarYUVLuminanceSource(
                rotatedImage.byteArray,
                rotatedImage.width,
                rotatedImage.height,
                0, 0,
                rotatedImage.width,
                rotatedImage.height,
                false
            )
            val hybridBinarizer = HybridBinarizer(planarYUVLuminanceSource)
            val binaryBitmap = BinaryBitmap(hybridBinarizer)
            try {
                val rawResult = multiFormatReader.decodeWithState(binaryBitmap)
                /*
                 val writer = QRCodeWriter()
                 try {
                     val width: Int = image.getWidth()
                     val height: Int = image.getHeight()
                     val bitMatrix = writer.encode(rawResult.text.toString(), BarcodeFormat.QR_CODE, width, height)
                     val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                     for (i in 0 until width) {
                         for (j in 0 until height) {
                             bitmap.setPixel(i, j, if (bitMatrix[i, j]) Color.BLACK else Color.WHITE)
                         }
                     }
                     croppedBmp=bitmap
                 } catch (e: WriterException) {
                     e.printStackTrace()
                 }*/

                val ori: Bitmap = imageProxy.toBitmap()!!
                croppedBmp = drawResultPoints(ori, rawResult)
                Log.d("---->BarcodeZXing:", rawResult.text)
                Toast.makeText(mContext, "ZXing Scanning", Toast.LENGTH_SHORT).show()
                listener.onScanned(
                    rawResult.text,
                    Bitmap.createScaledBitmap(croppedBmp!!, 100, 100, true)
                )


            } catch (e: NotFoundException) {
                e.printStackTrace()
            } finally {
                multiFormatReader.reset()
                imageProxy.close()
            }
            isScanning.set(false)
        }
    }


    // 90, 180. 270 rotation
    private fun rotateImageArray(imageToRotate: RotatedImage, rotationDegrees: Int) {
        if (rotationDegrees == 0) return // no rotation
        if (rotationDegrees % 90 != 0) return // only 90 degree times rotations

        val width = imageToRotate.width
        val height = imageToRotate.height

        val rotatedData = ByteArray(imageToRotate.byteArray.size)
        for (y in 0 until height) { // we scan the array by rows
            for (x in 0 until width) {
                when (rotationDegrees) {
                    90 -> rotatedData[x * height + height - y - 1] =
                        imageToRotate.byteArray[x + y * width] // Fill from top-right toward left (CW)
                    180 -> rotatedData[width * (height - y - 1) + width - x - 1] =
                        imageToRotate.byteArray[x + y * width] // Fill from bottom-right toward up (CW)
                    270 -> rotatedData[y + x * height] =
                        imageToRotate.byteArray[y * width + width - x - 1] // The opposite (CCW) of 90 degrees
                }
            }
        }

        imageToRotate.byteArray = rotatedData

        if (rotationDegrees != 180) {
            imageToRotate.height = width
            imageToRotate.width = height
        }
    }

    private fun getLuminancePlaneData(image: ImageProxy): ByteArray {
        val plane = image.planes[0]
        val buf: ByteBuffer = plane.buffer
        val data = ByteArray(buf.remaining())
        buf.get(data)
        buf.rewind()
        val width = image.width
        val height = image.height
        val rowStride = plane.rowStride
        val pixelStride = plane.pixelStride

        // remove padding from the Y plane data
        val cleanData = ByteArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                cleanData[y * width + x] = data[y * rowStride + x * pixelStride]
            }
        }
        return cleanData
    }


    private class RotatedImage(var byteArray: ByteArray, var width: Int, var height: Int)


    private fun ImageProxy.toBitmap(): Bitmap? {
        val nv21 = yuv420888ToNv21(this)
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        return yuvImage.toBitmap()
    }

    private fun YuvImage.toBitmap(): Bitmap? {
        val out = ByteArrayOutputStream()
        if (!compressToJpeg(Rect(0, 0, width, height), 20, out))
            return null
        val imageBytes: ByteArray = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun yuv420888ToNv21(image: ImageProxy): ByteArray {
        val pixelCount = image.cropRect.width() * image.cropRect.height()
        val pixelSizeBits = ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888)
        val outputBuffer = ByteArray(pixelCount * pixelSizeBits / 8)
        imageToByteBuffer(image, outputBuffer, pixelCount)
        return outputBuffer
    }

    private fun imageToByteBuffer(image: ImageProxy, outputBuffer: ByteArray, pixelCount: Int) {
        assert(image.format == ImageFormat.YUV_420_888)

        val imageCrop = image.cropRect
        val imagePlanes = image.planes

        imagePlanes.forEachIndexed { planeIndex, plane ->
            val outputStride: Int
            var outputOffset: Int

            when (planeIndex) {
                0 -> {
                    outputStride = 1
                    outputOffset = 0
                }
                1 -> {
                    outputStride = 2
                    outputOffset = pixelCount + 1
                }
                2 -> {
                    outputStride = 2
                    outputOffset = pixelCount
                }
                else -> {
                    return@forEachIndexed
                }
            }

            val planeBuffer = plane.buffer
            val rowStride = plane.rowStride
            val pixelStride = plane.pixelStride
            val planeCrop = if (planeIndex == 0) {
                imageCrop
            } else {
                Rect(
                    imageCrop.left / 2,
                    imageCrop.top / 2,
                    imageCrop.right / 2,
                    imageCrop.bottom / 2
                )
            }

            val planeWidth = planeCrop.width()
            val planeHeight = planeCrop.height()
            val rowBuffer = ByteArray(plane.rowStride)

            val rowLength = if (pixelStride == 1 && outputStride == 1) {
                planeWidth
            } else {

                (planeWidth - 1) * pixelStride + 1
            }

            for (row in 0 until planeHeight) {
                planeBuffer.position(
                    (row + planeCrop.top) * rowStride + planeCrop.left * pixelStride
                )
                if (pixelStride == 1 && outputStride == 1) {

                    planeBuffer.get(outputBuffer, outputOffset, rowLength)
                    outputOffset += rowLength
                } else {
                    planeBuffer.get(rowBuffer, 0, rowLength)
                    for (col in 0 until planeWidth) {
                        outputBuffer[outputOffset] = rowBuffer[col * pixelStride]
                        outputOffset += outputStride
                    }
                }
            }
        }
    }

    private fun drawResultPoints(bar: Bitmap, rawResult: Result): Bitmap {
        var barcode = bar
        val points = rawResult.resultPoints
        if (points != null && points.isNotEmpty()) {
            barcode = Bitmap.createBitmap(bar.width, bar.height, Bitmap.Config.ARGB_8888)
            Log.d(
                "TAGAfterBefore",
                "Before Bitmap :Height" + barcode.height + ":: Width:" + barcode.width
            )
            val canvas = Canvas(barcode)
            canvas.drawBitmap(bar, 200f, 200f, null)
            Log.d(
                "TAGAfterBefore",
                "after Draw on Canvas:Height" + barcode.height + ":: Width:" + barcode.width
            )
            val paint = Paint()
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 5f
            paint.color = Color.RED
            if (points.size == 2) {
                val ydw = (points[1].y - points[0].y) * (points[1].y - points[0].y)
                val xdw = (points[1].x - points[0].x) * (points[1].x - points[0].x)
                val d = sqrt((xdw.toDouble() + ydw.toDouble()))
                Log.d(
                    "resultPointParameters",
                    "drawResultPoints: img Height:" + barcode.height + "\n img Width:" + barcode.width + "\n detected barcode img 1st point: x=" + points[0].x + "\n y=" + points[0].y + "\n detected barcode img 2nd point: x=" + points[1].x + "\n y=" + points[1].y + "\n distance between two points:" + d
                )
                Log.d("distanceSquareTAG", "Width: $d,Height:$d")
                if (d <= 450) {
                    val scaleFactor = 0.30 // Set this to the zoom factor
                    val widthOffset = ((1 - scaleFactor) / 2 * bar.width).toInt()
                    Log.d("ParametersTAG", "widthOffset: $widthOffset")
                    val heightOffset = ((1 - scaleFactor) / 2 * bar.height).toInt()
                    Log.d("ParametersTAG", "heightOffset: $heightOffset")
                    val numWidthPixels: Int = ((bar.width) - 2 * widthOffset)
                    Log.d("ParametersTAG", "numWidthPixels: $numWidthPixels")
                    val numHeightPixels: Int = ((bar.height) - 2 * heightOffset)
                    Log.d("ParametersTAG", "numHeightPixels: $numHeightPixels")
                    val rescaledBitmap = Bitmap.createBitmap(
                        bar,
                        (widthOffset + (d / 8)).toInt(),
                        (heightOffset + (d / 6)).toInt(),
                        (numWidthPixels + (d / 2)).toInt(),
                        (numHeightPixels + (d / 4)).toInt(),
                        null,
                        true
                    )
                    return rescaledBitmap
                }

            } else if (points.size == 4) {
                val xd = (points[1].x - points[0].x) * (points[1].x - points[0].x)
                val yd = (points[1].y - points[0].y) * (points[1].y - points[0].y)
                val d = sqrt((xd.toDouble() + yd.toDouble()))

                 /*  val p0 = Point(0, 0)
                   val p1 =
                       sqrt(((points[0].x - p0.x) * (points[0].x - p0.x) + (points[0].y - p0.y) * (points[0].y - p0.y)))
                   val p2 =
                       sqrt(((points[1].x - p0.x) * (points[1].x - p0.x) + (points[1].y - p0.y) * (points[1].y - p0.y)))

                   val p3 =
                       sqrt(((points[2].x - p0.x) * (points[2].x - p0.x) + (points[2].y - p0.y) * (points[2].y - p0.y)))
                   val p4 =
                       sqrt(((points[3].x - p0.x) * (points[3].x - p0.x) + (points[3].y - p0.y) * (points[3].y - p0.y)))

                   Log.d("TAGParameterX", "drawResultPoints: 1 " + points[0].x + "," + points[0].y+","+p1)
                   Log.d("TAGParameterX", "drawResultPoints: 2 " + points[1].x + "," + points[1].y+","+p2)
                   Log.d("TAGParameterX", "drawResultPoints: 3 " + points[2].x + "," + points[2].y+","+p3)
                   Log.d("TAGParameterX", "drawResultPoints: 4 " + points[3].x + "," + points[3].y+","+p4)

             val recBmp=Rect(p1.toInt(),p2.toInt(),p3.toInt(),p4.toInt())

                   if (recBmp.width()>0) {
                       val bmp = Bitmap.createBitmap(
                           barcode,
                           recBmp.left ,
                           recBmp.top ,
                           recBmp.width() ,
                           recBmp.height()
                           )
                       return bmp
                   }*/
                if (d <= 300) {
                    val scaleFactor = 0.30 // Set this to the zoom factor
                    val widthOffset = ((1 - scaleFactor) / 2 * barcode.width).toInt()
                    Log.d("ParametersTAG", "widthOffset: $widthOffset")
                    val heightOffset = ((1 - scaleFactor) / 2 * barcode.height).toInt()
                    Log.d("ParametersTAG", "heightOffset: $heightOffset")
                    val numWidthPixels: Int = ((barcode.width) - 2 * widthOffset)
                    Log.d("ParametersTAG", "numWidthPixels: $numWidthPixels")
                    val numHeightPixels: Int = ((barcode.height) - 2 * heightOffset)
                    Log.d("ParametersTAG", "numHeightPixels: $numHeightPixels")

                    val rescaledBitmap = Bitmap.createBitmap(
                        barcode,
                        widthOffset,
                        heightOffset,
                        numWidthPixels + 100,
                        numHeightPixels + 150,
                        null,
                        true
                    )
                    Log.d(
                        "TAGParameterX",
                        "drawResultPoints: " + barcode.height + "," + barcode.width
                    )
                    return rescaledBitmap
                }

            }
        }
        Log.d("TAG", "Image: Height" + barcode.height + ":: Width:" + barcode.width)
        Log.d(
            "TAG",
            "Detected Barcode: P1x:" + rawResult.resultPoints[0].x + ", p1y:" + rawResult.resultPoints[0].y + ", p2x:" + rawResult.resultPoints[1].x + ", p2y:" + rawResult.resultPoints[1].y
        )
        return barcode
    }


}