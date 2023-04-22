package me.yokan.profanitypilot.data

import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import me.yokan.profanitypilot.model.BayesianClassifierData

class IncludedTrainingData {

    companion object {
        fun parse(): BayesianClassifierData {
            val file = object {}::class.java.getResourceAsStream("/trained_data.cbor")
            return Cbor.decodeFromByteArray(file.readBytes())
        }
    }
}