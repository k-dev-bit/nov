package com.example.ui.screens.tools

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.deviceactions.ActionResult
import com.example.deviceactions.ActionType
import com.example.deviceactions.CommandParser
import com.example.deviceactions.DeviceActionExecutor
import com.example.deviceactions.ParsedCommand
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PhoneControlViewModel(application: Application) : AndroidViewModel(application) {

    private val actionExecutor = DeviceActionExecutor(application)

    private val _testInput = MutableStateFlow("")
    val testInput: StateFlow<String> = _testInput.asStateFlow()

    private val _parsedCommand = MutableStateFlow<ParsedCommand?>(null)
    val parsedCommand: StateFlow<ParsedCommand?> = _parsedCommand.asStateFlow()

    private val _lastActionResult = MutableStateFlow<ActionResult?>(null)
    val lastActionResult: StateFlow<ActionResult?> = _lastActionResult.asStateFlow()

    fun updateInput(text: String) {
        _testInput.value = text
        if (text.isNotBlank()) {
            _parsedCommand.value = CommandParser.parse(text)
        } else {
            _parsedCommand.value = null
        }
    }

    fun executeAction(type: ActionType, target: String?) {
        val result = actionExecutor.execute(type, target)
        _lastActionResult.value = result
    }

    fun executeParsedCommand() {
        val cmd = _parsedCommand.value ?: return
        if (cmd.isDeviceAction && cmd.actionType != null) {
            val result = actionExecutor.execute(cmd.actionType, cmd.target)
            _lastActionResult.value = result
        }
    }
}
