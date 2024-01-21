package io.bimmergestalt.reader.carapp

import android.content.Context
import io.bimmergestalt.idriveconnectkit.android.CarAppAssetResources
import java.io.InputStream
import java.util.Locale

class CarAppSharedAssetResources(context: Context, name: String): CarAppAssetResources(context, name) {
	companion object {
		const val IMG_BUILTIN_PREV_PAGE = 55002
		const val IMG_BUILTIN_NEXT_PAGE = 55001
		const val IMG_BUILTIN_STATUS_ACTIVE = 55009
		const val IMG_BUILTIN_PREV_MESSAGE = 55003
		const val IMG_BUILTIN_NEXT_MESSAGE = 55004

		const val IMG_PLAY = 58001
		const val IMG_PAUSE = 58002
		const val IMG_PREV_PARAGRAPH = 58003
		const val IMG_NEXT_PARAGRAPH = 58004
		const val IMG_PREV_MESSAGE = 58005
		const val IMG_NEXT_MESSAGE = 58006
		const val IMG_STATUS_ACTIVE = 58007
		const val IMG_STATUS_PAUSED = 58008
		const val IMG_STATUS_STOPPED = 58009

		const val TXT_PLAY = 59001
		const val TXT_PAUSE = 59002
		const val TXT_PREV_PARAGRAPH = 59003
		const val TXT_NEXT_PARAGRAPH = 59004
		const val TXT_PREV_MESSAGE = 59005
		const val TXT_NEXT_MESSAGE = 59006
		const val TXT_STATUS_ACTIVE = 59007
		const val TXT_STATUS_PAUSED = 59008
		const val TXT_STATUS_STOPPED = 59009
		const val TXT_TEXT_TO_SPEECH = 59010
		const val TXT_BEGINNING = 59011
		const val TXT_LINK_HTTP = 59012
		const val TXT_LINK_FTP = 59013
		const val TXT_LINK = 59014
	}
	fun getSharedImagedDB(brand: String): InputStream? {
		return loadFile("carapplications/$name/rhmi/${brand.lowercase(Locale.ROOT)}/images_shared.zip") ?:
		loadFile("carapplications/$name/rhmi/common/images_shared.zip")
	}
	fun getSharedTextsDB(brand: String): InputStream? {
		return loadFile("carapplications/$name/rhmi/${brand.lowercase(Locale.ROOT)}/texts_shared.zip") ?:
		loadFile("carapplications/$name/rhmi/common/texts_shared.zip")
	}
}