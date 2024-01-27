package io.bimmergestalt.reader

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import me.ash.reader.BuildConfig
import java.text.DateFormat
import java.util.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

object L {
	// access to the Android string resources
	var loadedResources: Resources? = null
		private set

	// datetime formatting
	lateinit var mediumDateFormat: DateFormat
	lateinit var timeFormat: DateFormat

	// all of the strings used in the car app
	// these default string values are used in tests, Android resources are used for real
	val FEEDS by StringResourceDelegate("Feeds")
	val UNREAD by StringResourceDelegate("Unread")
	val STARRED by StringResourceDelegate("Starred")

	fun loadResources(context: Context, locale: Locale? = null) {
		val thisContext = if (locale == null) { context } else {
			val origConf = context.resources.configuration
			val localeConf = Configuration(origConf)
			localeConf.setLocale(locale)
			context.createConfigurationContext(localeConf)
		}

		loadedResources = thisContext.resources
		mediumDateFormat = android.text.format.DateFormat.getMediumDateFormat(context)
		timeFormat = android.text.format.DateFormat.getTimeFormat(context)
	}
}

class StringResourceDelegate(val default: String): ReadOnlyProperty<L, String> {
	companion object {
		val pluralMatcher = Regex("([A-Z_]+)_([0-9]+)\$")
	}
	override operator fun getValue(thisRef: L, property: KProperty<*>): String {
		val resources = L.loadedResources ?: return default
		return if (property.name.matches(pluralMatcher)) {
			val nameMatch = pluralMatcher.matchEntire(property.name)
				?: throw AssertionError("Could not parse L name ${property.name}")
			val id = resources.getIdentifier(nameMatch.groupValues[1].lowercase(), "plurals", BuildConfig.APPLICATION_ID)
			if (id == 0) {
				throw AssertionError("Could not find Resource value for string ${property.name}")
			}
			val quantity = nameMatch.groupValues[2].toInt()
			resources.getQuantityString(id, quantity, quantity)
		} else {
			val id = resources.getIdentifier(property.name.lowercase(), "string", BuildConfig.APPLICATION_ID)
			if (id == 0) {
				throw AssertionError("Could not find Resource value for string ${property.name}")
			}
			resources.getString(id)
		}
	}
}