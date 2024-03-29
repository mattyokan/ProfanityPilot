package me.yokan.profanitypilot.train

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import me.yokan.profanitypilot.classifier.bayes.BayesianClassifier
import me.yokan.profanitypilot.classifier.bayes.BayesianDataGenerator
import me.yokan.profanitypilot.model.BayesianClassifierData
import opennlp.tools.tokenize.SimpleTokenizer
import smile.nlp.dictionary.EnglishStopWords
import smile.nlp.stemmer.LancasterStemmer

class GenerateDataFromProfanityCheckDataset {

    private data class Tokenized(
        val good: Boolean,
        val tokens: List<String>
    )

    fun generate() : BayesianClassifierData {
        val tokenizer = smile.nlp.tokenizer.SimpleTokenizer()
        val stemmer = LancasterStemmer()
        val stopWords = EnglishStopWords.COMPREHENSIVE

        val tokenizerFunction = { msg: String ->
            tokenizer.split(msg)
                .asSequence()
                .map { it.lowercase() }
                .filterNot { it in stopWords }
                .map { stemmer.stem(it) }
                .toList()
        }

        val dataFile = GenerateDataFromProfanityCheckDataset::class.java.getResourceAsStream("/training/clean_data.csv")
        val data = csvReader().readAll(dataFile)
        var allTokenized = data
            .drop(1)
            .map { info ->
                val isOffensive = info[0] == "1"
                val message = info[1]
                Tokenized(!isOffensive, tokenizerFunction(message))
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
