package io.bimmergestalt.reader

import androidx.core.text.HtmlCompat
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

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

	// https://stackoverflow.com/a/74207113/169035
	val <A> Iterable<A>.par get() = ParallelizedIterable(this)
	@JvmInline
	value class ParallelizedIterable<A>(val iter: Iterable<A>) {
		suspend fun <B> map(f: suspend (A) -> B): List<B> = coroutineScope {
			iter.map { async { f(it) } }.awaitAll()
		}
	}
}
