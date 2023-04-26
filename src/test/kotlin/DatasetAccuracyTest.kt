import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import me.yokan.profanitypilot.classifier.bayes.BayesianClassifier
import me.yokan.profanitypilot.model.BayesianClassifierData
import me.yokan.profanitypilot.train.GenerateDataFromProfanityCheckDataset
import kotlin.time.Duration.Companion.milliseconds


private data class TestEntry(
    val message: String,
    val good: Boolean,
    val computedScore: Double
) {
    fun computedGood(threshold: Double = BayesianClassifier.ProfanityThreshold) = computedScore <= threshold
}

private data class EvaluationInfo(
    val precision: Double,
    val recall: Double,
    val accuracy: Double,
    val truePositives: Int,
    val trueNegatives: Int,
    val falsePositives: Int,
    val falseNegatives: Int,
)

/**
 * Self-tests the generated filter for accuracy against the training dataset.
 * This is obviously flawed methodology for assessing whether the classifier is "good",
 * but it is helpful for the purposes of spotting obvious mistakes in the algorithm.
 */
fun main() {
    val file = object {}::class.java.getResourceAsStream("/trained_data.cbor")
    val data = Cbor.decodeFromByteArray<BayesianClassifierData>(file.readBytes())
    val model = BayesianClassifier(data)


    val trainingDataFile = GenerateDataFromProfanityCheckDataset::class.java.getResourceAsStream("/training/clean_data.csv")
    val trainingData = csvReader().readAll(trainingDataFile)
    var allMessages = trainingData
        .drop(1)
        .map { info ->
            val isOffensive = info[0] == "1"
            val message = info[1]

            TestEntry(message, !isOffensive, model.computeScore(message))
        }

//    val regex = RegexFilterProvider()
//
//    val time = System.currentTimeMillis()
//    val totalEvil = allMessages.count { regex.blocked(it.message) }
//    val elapsed = ((System.currentTimeMillis()) - time).milliseconds
//
//    val timeBayes = System.currentTimeMillis()
//    val totalEvilBayes = allMessages.count { model.computeScore(it.message) > BayesianClassifier.ProfanityThreshold }
//    val elapsedBayes = (System.currentTimeMillis() - timeBayes).milliseconds
//
//    println("Regex took ${elapsed.inWholeMilliseconds} milliseconds to scan ${allMessages.size} messages to find $totalEvil bad messages")
//    println("Bayes took ${elapsedBayes.inWholeMilliseconds} milliseconds to scan ${allMessages.size} messages to find $totalEvilBayes bad messages")

    fun computeBayesianPrecisionRecall(threshold: Double): EvaluationInfo {
        val truePositives = allMessages
            .count { it.good && it.computedGood(threshold) }

        val trueNegatives = allMessages
            .count { !it.good && !it.computedGood(threshold) }

        val falsePositives = allMessages
            .count { it.good && !it.computedGood(threshold) }

        val falseNegatives = allMessages
            .count { !it.good && it.computedGood(threshold) }

        val precision = truePositives.toDouble() / (truePositives + falsePositives)
        val recall = truePositives.toDouble() / (truePositives + falseNegatives)
         val accuracy = (truePositives + trueNegatives).toDouble() / (truePositives + trueNegatives + falsePositives + falseNegatives)

        return EvaluationInfo(precision, recall, accuracy, truePositives, trueNegatives, falsePositives, falseNegatives)
    }

    val regexFilter = RegexFilterProvider()

    fun computeRegexPrecisionRecall(): Pair<Double, Double> {
        fun good(msg: String) = !regexFilter.blocked(msg)

        val truePositives = allMessages
            .count { it.good && good(it.message) }

        val trueNegatives = allMessages
            .count { !it.good && !good(it.message) }

        val falsePositives = allMessages
            .count { it.good && !good(it.message) }

        val falseNegatives = allMessages
            .count { !it.good && good(it.message) }

        val precision = truePositives.toDouble() / (truePositives + falsePositives)
        val recall = truePositives.toDouble() / (truePositives + falseNegatives)

        return precision to recall
    }

//    println("Starting regex precision recall")
//    val time = System.currentTimeMillis()
//    val (regexPrecision, regexRecall) = computeRegexPrecisionRecall()
//    val regexElapsed = (System.currentTimeMillis() - time).milliseconds
//    println("Regex precision recall finished")

    println("Threshold\tPrecision\tRecall\tAccuracy")
    for (i in (0..100 step 1)) {
        val threshold = i / 100.0
        val time = System.currentTimeMillis()
        val metrics = computeBayesianPrecisionRecall(threshold)
        val elapsed = (System.currentTimeMillis() - time).milliseconds

        println("$threshold\t${metrics.precision}\t${metrics.recall}\t${metrics.accuracy}")
    }


}