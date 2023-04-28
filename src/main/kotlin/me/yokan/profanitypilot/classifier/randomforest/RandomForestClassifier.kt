package me.yokan.profanitypilot.classifier.randomforest

import me.yokan.profanitypilot.model.Classifier
import smile.classification.RandomForest
import smile.data.DataFrame
import smile.data.Tuple
import smile.data.formula.Formula
import smile.data.transform.Transform
import smile.feature.extraction.BagOfWords
import smile.nlp.dictionary.EnglishStopWords
import smile.nlp.stemmer.LancasterStemmer
import smile.nlp.tokenizer.SimpleTokenizer


class RandomForestClassifier(
    messages: List<RandomForestFactory.Message>
) : Classifier {

    private val model = RandomForestFactory().create(messages)
    private val encoder = model.first
    private val classifier = model.second

    override fun computeScore(message: String): Double {
        val tuple = message.toMessageTuple()
        val msg = encoder.apply(tuple)
        val merged = tuple.merge(msg)
        return classifier.predict(merged).toDouble()
    }

    fun create(messages: List<RandomForestFactory.Message>): Pair<Transform, smile.classification.Classifier<Tuple>> {
        val tokenizer = SimpleTokenizer()
        val stemmer = LancasterStemmer()
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
            ).toTypedArray(), MessageSchema) },
            MessageSchema
        )

        val transform = BagOfWords.fit(
            dataFrame,
            tokenizerFunction,
            RandomForestFactory.NumFeatures,
            "text"
        )


        val transformed = transform.apply(dataFrame.select("text"))
            .merge(dataFrame.select("label"))

        val classifier = RandomForest.fit(Formula.lhs("label"), transformed)

        return transform to classifier
    }
}