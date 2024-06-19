package io.bimmergestalt.reader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Base64
import androidx.core.graphics.drawable.toDrawable
import coil.imageLoader
import coil.request.ImageRequest
import java.io.ByteArrayOutputStream

class GraphicsUtils(val context: Context) {
	val imageLoader = context.imageLoader

	suspend fun loadImageUri(uri: String?, width: Int, height: Int): Drawable? {
		uri ?: return null
		return if (Regex("^image/[a-rt-z-]*;base64,.*").matches(uri)) {
			loadBase64Drawable(uri)
		} else if (uri.startsWith("http")) {
			val request = ImageRequest.Builder(context)
				.data(uri)
				.allowHardware(false)
				.size(width, height)
				.build()
			imageLoader.execute(request).drawable
		} else {
			null
		}
	}

	fun loadBase64Drawable(base64Uri: String): Drawable? {
		val bytes = base64ToBytes(base64Uri)
		return BitmapFactory.decodeByteArray(bytes, 0, bytes.size).toDrawable(context.resources)
	}

	private fun base64ToBytes(base64String: String): ByteArray {
		val base64Data = base64String.substringAfter("base64,")
		return Base64.decode(base64Data, Base64.DEFAULT)
	}

	fun resizeDrawable(drawable: Drawable, width: Int, height: Int): Bitmap {
		if (drawable is BitmapDrawable && drawable.bitmap.width == width && drawable.bitmap.height == height) {
			return drawable.bitmap
		}
		val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
		val canvas = Canvas(bitmap)
		drawable.setBounds(0, 0, width, height)
		drawable.draw(canvas)
		return bitmap
	}

	fun compressBitmapJpg(bitmap: Bitmap, quality: Int): ByteArray {
		val jpg = ByteArrayOutputStream()
		bitmap.compress(Bitmap.CompressFormat.JPEG, quality, jpg)
		return jpg.toByteArray()
	}
}