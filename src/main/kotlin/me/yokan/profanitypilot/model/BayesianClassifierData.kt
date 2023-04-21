package me.yokan.profanitypilot.model

import kotlinx.serialization.Serializable

@Serializable
data class BayesianClassifierData(
    val goodCounts: Int,
    val badCounts: Int,
    /**
     * Token to probability of the message being bad.
     */
    val tokenProbabilities: Map<String, Double>
)
