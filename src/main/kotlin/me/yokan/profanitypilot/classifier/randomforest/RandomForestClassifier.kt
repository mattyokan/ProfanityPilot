package me.yokan.profanitypilot.classifier.randomforest

import me.yokan.profanitypilot.model.Classifier


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
}