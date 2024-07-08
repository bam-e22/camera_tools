package com.example.common

import android.util.Log
import java.util.Timer
import java.util.TimerTask

class SimpleMeasurer(
    private val id: String,
) {
    private var fpsTimer: Timer? = null
    private var framesPerSecond: Int = 0
    private var frameProcessedInOneSecondInterval: Int = 0
    private val a: Int = 0

    fun startTimer() {
        fpsTimer = Timer()
        fpsTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                framesPerSecond = frameProcessedInOneSecondInterval
                frameProcessedInOneSecondInterval = 0
                printLog()
            }
        }, 0L, 1000L)
    }

    fun addCount() {
        frameProcessedInOneSecondInterval++
    }

    fun stopTimer() {
        fpsTimer?.cancel()
        fpsTimer = null
    }

    private fun printLog() {
        buildString {
            appendLine("[$id] FramesPerSecond           : ${framesPerSecond}")
        }.let {
            Log.i(TAG, it)
        }
    }

    companion object {
        private const val TAG = "Measurer"
    }
}
