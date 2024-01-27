package io.bimmergestalt.reader

import org.junit.Assert.assertEquals
import org.junit.Test

class UtilsTest {
	@Test
	fun testReadoutFormat() {
		val input = "Line1 spoken by H. G. Wells and his Jr. son.  But, maybe not? Same paragraph:\nLine2\n\nExcited line!\nVersion number 3.4.2 is unterminated\n\nReady? Go!\n\n\t\t\n"
		val expected = listOf(
			"Line1 spoken by H. G. Wells and his Jr. son.",
			"But, maybe not?",
			"Same paragraph:",
			"Line2",
			"Excited line!",
			"Version number 3.4.2 is unterminated",
			"Ready?",
			"Go!"
		)
		assertEquals(expected, Utils.formatForReadout(input))
	}
}