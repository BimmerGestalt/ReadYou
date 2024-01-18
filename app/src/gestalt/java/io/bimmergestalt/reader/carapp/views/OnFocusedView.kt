package io.bimmergestalt.reader.carapp.views

import io.bimmergestalt.idriveconnectkit.rhmi.FocusCallback
import io.bimmergestalt.idriveconnectkit.rhmi.RHMIState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch

abstract class OnFocusedView(internal val state: RHMIState) {

	internal val coroutineScope = CoroutineScope(Job() + Dispatchers.IO)

	init {
		state.focusCallback = FocusCallback { focused ->
			if (focused) {
				coroutineScope.launch { onFocus() }
			} else {
				coroutineScope.coroutineContext.cancelChildren()
				onBlur()
			}
		}
	}

	open suspend fun onFocus() {
	}
	open fun onBlur() {
	}
}