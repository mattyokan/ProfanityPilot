package me.yokan.profanitypilot.classifier.regex

import me.yokan.profanitypilot.model.Classifier

class RegexClassifier : Classifier {

    private val filter = RegexFilterProvider()

    override fun computeScore(message: String) = if(filter.blocked(message)) 1.0 else 0.0
}