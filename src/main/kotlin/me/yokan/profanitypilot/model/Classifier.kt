package me.yokan.profanitypilot.model

import opennlp.tools.tokenize.Tokenizer

interface Classifier {

    val tokenizer: Tokenizer

    /**
     * Compute the likelihood of the message provided
     * to be profane/not profane on a scale of [0.0, 1.0].
     */
    fun computeScore(message: String): Double
}