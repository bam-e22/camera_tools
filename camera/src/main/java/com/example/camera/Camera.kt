package com.example.camera

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager

fun Context.getAvailableCameraInfo(): String {
    val sb = StringBuilder()
    val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
    val cameraIdList = cameraManager.cameraIdList
    cameraIdList.forEachIndexed { index, cameraId ->
        val characteristic = cameraManager.getCameraCharacteristics(cameraId)
        sb.appendLine("Camera #$index")
        sb.appendLine(" - cameraId: $cameraId")
        val lensFacing = characteristic.get(CameraCharacteristics.LENS_FACING)
        val readableLensFacing = when (lensFacing) {
            0 -> "Front"
            1 -> "Back"
            2 -> "External"
            else -> "Unknown"
        }
        sb.appendLine(" - lensFacing: $readableLensFacing")
        characteristic.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)?.let { size ->
            sb.appendLine(" - sensorSize: $size")
        }

        val flashAvailable = characteristic.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
        sb.appendLine(" - flashAvailable: $flashAvailable")

        characteristic.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)?.let { map ->
            sb.appendLine(" - outputFormats: ${map.outputFormats.joinToString()}")
        }

        val afModes = characteristic.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES)
        sb.appendLine(" - autoFocusModes: ${afModes?.joinToString() ?: "None"}")
        sb.appendLine()
    }

    return sb.toString()
}
