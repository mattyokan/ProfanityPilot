package me.yokan.profanitypilot.train

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import me.yokan.profanitypilot.classifier.bayes.BayesianClassifier
import me.yokan.profanitypilot.classifier.bayes.BayesianDataGenerator
import me.yokan.profanitypilot.model.BayesianClassifierData
import opennlp.tools.tokenize.SimpleTokenizer

class GenerateDataFromProfanityCheckDataset {

    private data class Tokenized(
        val good: Boolean,
        val tokens: List<String>
    )

    fun generate() : BayesianClassifierData {
        val tokenizer = SimpleTokenizer.INSTANCE

        val dataFile = GenerateDataFromProfanityCheckDataset::class.java.getResourceAsStream("/training/clean_data.csv")
        val data = csvReader().readAll(dataFile)
        var allTokenized = data
            .drop(1)
            .map { info ->
                val isOffensive = info[0] == "1"
                val message = info[1]
                Tokenized(!isOffensive, tokenizer.tokenize(message).toList())
            }

        val info = allTokenized.groupBy { it.good }
            .map { (good, messages) ->
                good to messages.flatMap { it.tokens }
                    .groupingBy { it }
                    .eachCount()
            }
            .toMap()

        val good = info[true] ?: emptyMap()
        val bad = info[false] ?: emptyMap()

        return BayesianDataGenerator().generateDataFromCounts(good, bad)
    }
}
