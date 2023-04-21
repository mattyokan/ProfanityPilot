package me.yokan.profanitypilot.classifier.bayes

import me.yokan.profanitypilot.model.BayesianClassifierData
import me.yokan.profanitypilot.model.Classifier
import opennlp.tools.tokenize.SimpleTokenizer
import opennlp.tools.tokenize.Tokenizer
import kotlin.math.abs

/**
 * Represents probability maps corresponding to a given token being "good" or "bad"
 * and the associated helper functions for calculating the probability of a given token list being spam or not.
 *
 * This classifier is based on Paul Graham's essay on spam filtering, titled "A Plan for Spam": http://www.paulgraham.com/spam.html
 */
class BayesianClassifier(
    private val data: BayesianClassifierData
) : Classifier {

    override val tokenizer: Tokenizer = SimpleTokenizer.INSTANCE

    companion object {
        // Words that only appear in good/bad (not both) are considered to be almost certainly good or bad.
        const val ExclusivelyGoodProbability = 0.99
        const val ExclusivelyBadProbability = 0.01

        // If we do not have a word in our probability maps, treat it as 50/50 good/bad as it is unknown.
        const val NeutralProbability = 0.50

        // The maximum number of most interesting tokens to consider when classifying a message.
        const val InterestingTokenAmount = 15

        // In his article, Paul Graham says he uses 0.4 as the cutoff. I will be doing some further
        // testing and optimization later on, but for now, that's good enough.
        const val ProfanityThreshold = 0.4


    }

    private val String.spamProbability get() = data.tokenProbabilities[this] ?: NeutralProbability


    /**
     * Select the most interesting N tokens as defined by how far they differ
     * from the neutral probability.
     */
    private fun mostInterestingTokens(tokens: Iterable<String>, count: Int) = tokens.map { it to it.spamProbability }
        .sortedByDescending { (_, spamProb) -> spamProb - NeutralProbability }
        .take(count)

    private fun combinedProbability(interestingTokens: List<Pair<String, Double>>): Double =
        interestingTokens
            .takeIf { it.isNotEmpty() }
            ?.map { (_, prob) -> prob }
            ?.let { probs ->
                val (product, complementProduct) = probs.fold(1.0 to 1.0) { (prodAcc, compProdAcc), prob ->
                    prodAcc * prob to compProdAcc * (1 - prob)
                }
                product / (product + complementProduct)
            } ?: NeutralProbability // Empty token list should be neutral.

    override fun computeScore(message: String): Double =
        tokenizer.tokenize(message.replace("\"", "").lowercase())
            .toList()
            .let { tokens -> combinedProbability(mostInterestingTokens(tokens, InterestingTokenAmount)) }


}