package me.yokan.profanitypilot.classifier.svm

import me.yokan.profanitypilot.model.Classifier


class SVMClassifier(
    messages: List<SVMModelFactory.Message>
) : Classifier {

    private val model = SVMModelFactory().create(messages)
    private val encoder = model.first
    private val classifier = model.second

    override fun computeScore(message: String): Double {
        return classifier.score(encoder.apply(message))
    }
}