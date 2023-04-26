package me.yokan.profanitypilot.classifier.randomforest

import smile.classification.Classifier
import smile.classification.RandomForest
import smile.data.DataFrame
import smile.data.Tuple
import smile.data.formula.Formula
import smile.data.transform.Transform
import smile.data.type.DataTypes
import smile.data.type.StructField
import smile.data.type.StructType
import smile.feature.extraction.BagOfWords
import smile.nlp.dictionary.EnglishStopWords
import smile.nlp.stemmer.LancasterStemmer
import smile.nlp.tokenizer.SimpleTokenizer

val MessageSchema = StructType(
    listOf(
        StructField("text", DataTypes.StringType),
        StructField("label", DataTypes.IntegerType)
    )
)
fun String.toMessageTuple(): Tuple = Tuple.of(
    listOf(this, 0).toTypedArray(),
    MessageSchema
)

val Tuple.all get() = (0 until length()).map { get(it) }

/**
 * It is beyond me why this isn't included...
 */
fun Tuple.merge(other: Tuple): Tuple =
    Tuple.of(
        (all + other.all).toTypedArray(),
        StructType(
            (schema().fields() + other.schema().fields()).toList()
        )
    )

class RandomForestFactory {

    companion object {
        const val NumFeatures = 150
    }

    data class Message(
        val content: String,
        val profane: Boolean,
    )

    fun create(messages: List<Message>): Pair<Transform, Classifier<Tuple>> {
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
            NumFeatures,
            "text"
        )


        val transformed = transform.apply(dataFrame.select("text"))
            .merge(dataFrame.select("label"))

        val classifier = RandomForest.fit(Formula.lhs("label"), transformed)

        return transform to classifier
    }
}