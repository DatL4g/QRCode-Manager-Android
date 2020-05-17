package de.datlag.qrcodemanager.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import java.io.InputStream
import java.lang.Exception
import java.util.*

class FileScanner(private val context: Context) {

    fun scanFromUri(uri: Uri, callback: Callback) {
        val bitmap = bitmapFromUri(uri, callback)
        val source = createSource(bitmap)
        bitmap.recycle()
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        val reader = MultiFormatReader()
        decode(binaryBitmap, reader, callback)
    }

    private fun bitmapFromUri(uri: Uri, callback: Callback): Bitmap {
        var imageStream: InputStream? = null
        try {
            imageStream = context.contentResolver.openInputStream(uri)
        } catch (e: Exception) {
            callback.onError(e)
        }
        val bitmap = BitmapFactory.decodeStream(imageStream)
        imageStream?.close()
        return bitmap
    }

    private fun createSource(bitmap: Bitmap): LuminanceSource {
        val intArray = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(intArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        return RGBLuminanceSource(bitmap.width, bitmap.height, intArray)
    }

    private fun decode(bitmap: BinaryBitmap, reader: Reader, callback: Callback) {
        try {
            val decodeHints = Hashtable<DecodeHintType, Any>()
            decodeHints[DecodeHintType.TRY_HARDER] = true
            decodeHints[DecodeHintType.PURE_BARCODE] = true
            val result = reader.decode(bitmap, decodeHints)
            if (result != null && result.text != null) callback.onResult(result) else callback.onError(null)
        } catch (e: Exception) {
            callback.onError(e)
        }
    }

    interface Callback {
        fun onResult(result: Result)
        fun onError(exception: Exception?)
    }
}