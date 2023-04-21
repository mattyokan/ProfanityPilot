package me.yokan.profanitypilot.classifier.bayes

import me.yokan.profanitypilot.model.BayesianClassifierData
import kotlin.math.min

class BayesianDataGenerator {

    fun generateDataFromCounts(goodCounts: Map<String, Int>, badCounts: Map<String, Int>) =
        (goodCounts.values.sum() to badCounts.values.sum())
            .let { (totalGood, totalBad) ->
                println("Have $totalGood good words and $totalBad words")
                (goodCounts.keys + badCounts.keys)
                    .associate { token ->
                        // Algorithm adapted from http://www.paulgraham.com/spam.html

                        // Doubling numbers in good is used to bias probabilities slightly
                        // to avoid false positives: distinguishes between tokens that occasionally occur
                        // in sincere messages compared to words that almost never do.
                        val g = (goodCounts[token] ?: 0) * 2
                        val b = badCounts[token] ?: 0

                        if (g + b < 5) {
                            token to 0.0
                        } else {

                            val goodProb = min(1.0, g.toDouble() / totalGood)
                            val badProb = min(1.0, b.toDouble() / totalBad)

                            val badLikelihood = badProb / (goodProb + badProb)

                            token to badLikelihood
                                .coerceAtLeast(BayesianClassifier.ExclusivelyBadProbability)
                                .coerceAtMost(BayesianClassifier.ExclusivelyGoodProbability)
                        }
                    }
                    .let { BayesianClassifierData(totalGood, totalBad, it) }
            }
}