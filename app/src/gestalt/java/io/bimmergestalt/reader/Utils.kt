package io.bimmergestalt.reader

import androidx.core.text.HtmlCompat

object Utils {
	fun parseHtml(article: String): String {
		val htmlContents = article.replace(Regex("<h1.*?<.*?>"), "")
		return HtmlCompat.fromHtml(htmlContents,
			HtmlCompat.FROM_HTML_MODE_COMPACT
		).toString()
	}
	fun formatForReadout(article: String): List<String> {
		/**
		 * split sentences into lines
		 * splitting by punctuation, as long as the punctuation has a blank space afterwards
		 * and some not-blankspaces before (to not split name abbreviations)
		 */
		return article.replace(Regex("(\\S{3}[.?!])\\p{Blank}+?\n?"), "$1\n")
			.lines()
			.map { it.trim() }
			.filter { it.isNotBlank() }
	}
}