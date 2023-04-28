package evaluation

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import me.yokan.profanitypilot.classifier.bayes.BayesianClassifier
import me.yokan.profanitypilot.classifier.regex.RegexClassifier
import me.yokan.profanitypilot.model.BayesianClassifierData
import me.yokan.profanitypilot.train.GenerateDataFromProfanityCheckDataset
import me.yokan.profanitypilot.train.TrainRandomForest

private data class TestEntry(
    val message: String,
    val good: Boolean,
)

private data class EvaluationInfo(
    val precision: Double,
    val recall: Double,
    val accuracy: Double,
    val runtimeMillis: Long,
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
    val bayesData = Cbor.decodeFromByteArray<BayesianClassifierData>(file.readBytes())


    val testingDataFile = GenerateDataFromProfanityCheckDataset::class.java.getResourceAsStream("/training/test_data_hasoc.csv")
    val trainingData = csvReader().readAll(testingDataFile)
    var allMessages: List<TestEntry> = trainingData
        .drop(1)
        .map { info ->
            val content = info[1]
            val safe = info[2] == "NOT"
            TestEntry(content, safe)
        }

    fun computeMetrics(isGood: (String) -> Boolean): EvaluationInfo {
        val start = System.currentTimeMillis()
        val truePositives = allMessages
            .count { it.good && isGood(it.message) }

        val trueNegatives = allMessages
            .count { !it.good && !isGood(it.message) }

        val falsePositives = allMessages
            .count { it.good && !isGood(it.message) }

        val falseNegatives = allMessages
            .count { !it.good && isGood(it.message) }

        val elapsedMillis = System.currentTimeMillis() - start

        val precision = truePositives.toDouble() / (truePositives + falsePositives)
        val recall = truePositives.toDouble() / (truePositives + falseNegatives)
        val accuracy = (truePositives + trueNegatives).toDouble() / (truePositives + trueNegatives + falsePositives + falseNegatives)

        return EvaluationInfo(precision, recall, accuracy, elapsedMillis, truePositives, trueNegatives, falsePositives, falseNegatives)
    }

    val models = mapOf(
        "Regex Filter" to RegexClassifier(),
        "Naive Bayes" to BayesianClassifier(bayesData),
        "Random Forest" to TrainRandomForest().generate()
    )

    val columns = models.flatMap { (name, _) ->
        listOf(
            "Threshold",
            "$name Runtime (ms)",
            "$name Precision",
            "$name Recall",
            "$name Accuracy",
        )
    }
        .joinToString("\t")

    val thresholds = listOf(
        0.25,
        0.4,
        0.5,
        0.75,
    )

    println(columns)
    thresholds.forEach { threshold ->
        models.flatMap { (_, model) ->
            val classifier = { input: String -> model.computeScore(input) >= threshold }
            computeMetrics(classifier)
                .let {
                    listOf(
                        it.runtimeMillis,
                        it.precision,
                        it.recall,
                        it.accuracy
                    )
                }
        }
            .joinToString("\t")
            .let { info ->
                println("$threshold\t$info")
            }
    }

}