package io.bimmergestalt.reader.carapp

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class ReadoutControllerTest {
	@Test
	fun testTTSEventEmpty() {
		val input = """
			{"TTSState": {
			  "state": 0,
			  "currentblock": 0,
			  "blocks": 0,
			  "type": null,
			  "languageavailable": 1
			}}
		""".trimIndent()
		val parsed = Gson().fromJson(input, HMITTS::class.java)
		assertEquals(0, parsed.TTSState.state)
		assertEquals(0, parsed.TTSState.currentblock)
		assertEquals(0, parsed.TTSState.blocks)
		assertEquals(null, parsed.TTSState.type)
		assertEquals(1, parsed.TTSState.languageavailable)
		assertEquals(ReadoutState.UNDEFINED, parsed.TTSState.stateName)
	}
}