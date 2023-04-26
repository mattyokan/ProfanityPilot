package me.yokan.profanitypilot.classifier.svm

import me.yokan.profanitypilot.classifier.randomforest.MessageSchema
import me.yokan.profanitypilot.classifier.randomforest.RandomForestFactory
import smile.classification.Classifier
import smile.classification.SVM
import smile.data.DataFrame
import smile.data.Tuple
import smile.feature.extraction.BagOfWords
import smile.nlp.dictionary.EnglishStopWords
import smile.nlp.stemmer.PorterStemmer
import smile.nlp.tokenizer.SimpleTokenizer

class SVMModelFactory {

    companion object {
        const val NumFeatures = 4
        const val RegularizationParameter = 100.0
        const val ConvergenceTolerance = 1E-2
    }

    data class Message(
        val content: String,
        val profane: Boolean,
    )

    fun create(messages: List<Message>): Pair<BagOfWords, Classifier<IntArray>> {
        val tokenizer = SimpleTokenizer()
        val stemmer = PorterStemmer()
        val stopWords = EnglishStopWords.COMPREHENSIVE

        val tokenizerFunction = { msg: String ->
            tokenizer.split(msg)
                .asSequence()
                .map { it.lowercase() }
                .filterNot { it in stopWords }
                .map { stemmer.stem(it) }
                .toList()
                .toTypedArray()
        }

        val dataFrame = DataFrame.of(
            messages.map { Tuple.of(listOf(
                it.content,
                if(it.profane) +1 else -1
            ).toTypedArray(), MessageSchema
            ) },
            MessageSchema
        )

        val transform = BagOfWords.fit(
            dataFrame,
            tokenizerFunction,
            RandomForestFactory.NumFeatures,
            "text"
        )

        val classifier = SVM.fit(
            messages.map { transform.apply(it.content) }.toTypedArray(),
            messages.map { if(it.profane) 1 else -1 }.toIntArray(),
            NumFeatures,
            RegularizationParameter,
            ConvergenceTolerance
        )

        return transform to classifier
    }
}