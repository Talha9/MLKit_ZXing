package com.example.mlkitzxing

import android.graphics.Bitmap

interface ScanningResultListener {
    fun onScanned(result: String, bit: Bitmap)
}