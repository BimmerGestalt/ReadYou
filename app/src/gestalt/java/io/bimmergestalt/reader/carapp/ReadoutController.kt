package io.bimmergestalt.reader.carapp

import android.util.Log
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIApplication
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIEvent
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlin.math.max
import kotlin.math.min


data class HMITTS(  // the json decoder object
	val TTSState: TTSState
)
data class TTSState(    // the actual state
	val state: Int?,
	val currentblock: Int?,
	val blocks: Int?,
	val type: String?,
	val languageavailable: Int?,
) {
	val stateName: ReadoutState
		get() = ReadoutState.fromValue(state)

	override fun toString(): String {
		return "$type ${stateName.name} - $currentblock/$blocks"
	}
}
enum class ReadoutState(val value: Int) {   // enum name for the state int
	UNDEFINED(0),
	IDLE(1),
	PAUSED(2),
	ACTIVE(3),
	BUSY(4);

	companion object {
		fun fromValue(value: Int?): ReadoutState {
			return values().firstOrNull { it.value == value } ?: UNDEFINED
		}
	}
}
enum class ReadoutCommand(val value: String) {
	PAUSE("STR_READOUT_PAUSE"),
	STOP("STR_READOUT_STOP"),
	PREV_BLOCK("STR_READOUT_PREV_BLOCK"),
	NEXT_BLOCK("STR_READOUT_NEXT_BLOCK"),
	RESTART("STR_READOUT_JUMP_TO_BEGIN"),
}

class ReadoutController(val name: String, val speechEvent: RHMIEvent.ActionEvent, val commandEvent: RHMIEvent.ActionEvent) {
	val speechList = speechEvent.getAction()?.asLinkAction()?.getLinkModel()?.asRaListModel()!!
	val commandList = commandEvent.getAction()?.asLinkAction()?.getLinkModel()?.asRaListModel()!!

	companion object {
		fun build(app: RHMIApplication, name: String): ReadoutController {
			val events = app.events.values.filterIsInstance<RHMIEvent.ActionEvent>().filter {
				it.getAction()?.asLinkAction()?.actionType == "readout"
			}
			if (events.size != 2) {
				throw IllegalArgumentException("UI Description is missing 2 readout events")
			}
			return ReadoutController(name, events[0], events[1])
		}
	}

	var desiredState = ReadoutState.IDLE
	val activeState = MutableStateFlow(ReadoutState.IDLE)   // whether we are currently talking
	private var currentState = TTSState(0, null, null, null, null)
	val debugText = MutableStateFlow(currentState.toString())
	val isActive: Boolean
		get() = currentState.type == name &&
				(currentState.stateName == ReadoutState.ACTIVE || currentState.stateName == ReadoutState.BUSY)

	private val lineIndex = MutableStateFlow(0)
	private var nextLineIndex = -1  // next line index to read at the next IDLE state
	private var lines = MutableStateFlow(emptyList<String>())
	val currentLine = lines.combine(lineIndex) { lines, i ->
		lines.getOrNull(i) ?: ""
	}

	fun onTTSEvent(ttsState: TTSState) {
		currentState = ttsState
		debugText.value = currentState.toString()
		Log.d(TAG, "TTSEvent: currentState:${ttsState.stateName} currentName:${ttsState.type} currentBlock:${ttsState.currentblock}/${ttsState.blocks}")

		if (desiredState == ReadoutState.ACTIVE && ttsState.stateName == ReadoutState.IDLE) {
			if (nextLineIndex >= 0) {
				lineIndex.value = nextLineIndex
				readLine()
				return // don't update activeState
			} else {
				desiredState = ReadoutState.IDLE    // automatically stop
			}
		}
		// we aren't automatically continuing on, update the public activeState
		activeState.value = if (ttsState.type == name) {
			when (ttsState.stateName) {
				ReadoutState.ACTIVE -> ReadoutState.ACTIVE
				ReadoutState.BUSY -> ReadoutState.ACTIVE
				ReadoutState.PAUSED -> ReadoutState.PAUSED
				else -> ReadoutState.IDLE
			}
		} else {        // some other TTS app is speaking
			ReadoutState.IDLE
		}
	}

	fun readLines(lines: List<String>) {
		loadLines(lines)
		play()
	}

	fun loadLines(lines: List<String>) {
		this.lines.value = lines
		this.lineIndex.value = 0
		nextLineIndex = 0
		desiredState = ReadoutState.IDLE
	}

	fun play() {
		desiredState = ReadoutState.ACTIVE
		readLine()
	}


	fun prevLine() {
		nextLineIndex = max(0, lineIndex.value - 1)
		lineIndex.value = nextLineIndex
		_stop()
	}

	fun nextLine() {
		nextLineIndex = min(lines.value.size - 1, lineIndex.value + 1)
		lineIndex.value = nextLineIndex
		_stop()
	}

	private fun readLine() {
		val line = lines.value.getOrNull(lineIndex.value) ?: ""
		val data = RHMIModel.RaListModel.RHMIListConcrete(2)
		data.addRow(arrayOf(line, name))
		Log.d(TAG, "Starting readout from $name: ${data[0][0]}")
		speechList.setValue(data, 0, 1, 1)
		speechEvent.triggerEvent()

		// cue the next line to play
		if (lineIndex.value < lines.value.size - 1) {
			nextLineIndex = lineIndex.value + 1
		} else {
			nextLineIndex = -1
		}
	}

	fun pause() {
		desiredState = ReadoutState.PAUSED

		Log.d(TAG, "Pausing $name readout")
		val data = RHMIModel.RaListModel.RHMIListConcrete(2).apply {
			addRow(arrayOf(ReadoutCommand.PAUSE.value, name))
		}
		commandList.setValue(data, 0, 1, 1)
		commandEvent.triggerEvent()
	}
	fun stop() {
		desiredState = ReadoutState.IDLE
		_stop()
	}
	private fun _stop() {
		Log.d(TAG, "Cancelling $name readout")
		val data = RHMIModel.RaListModel.RHMIListConcrete(2).apply {
			addRow(arrayOf(ReadoutCommand.STOP.value, name))
		}
		commandList.setValue(data, 0, 1, 1)
		commandEvent.triggerEvent()
	}
}