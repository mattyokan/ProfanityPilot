package me.yokan.profanitypilot

import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import me.yokan.profanitypilot.classifier.bayes.BayesianClassifier
import me.yokan.profanitypilot.model.BayesianClassifierData
import me.yokan.profanitypilot.train.GenerateDataFromProfanityCheckDataset
import java.io.File
import java.util.Scanner

fun main(args: Array<String>) {
    test()
}

fun train() {
    println("Starting training.")
    val data = GenerateDataFromProfanityCheckDataset().generate()
    val encoded = Cbor.encodeToByteArray(data)
    File("trained_data.cbor").writeBytes(encoded)
    println("Training complete.")
}

fun test() {
    val file = object {}::class.java.getResourceAsStream("/trained_data.cbor")
    val data = Cbor.decodeFromByteArray<BayesianClassifierData>(file.readBytes())
    val model = BayesianClassifier(data)

    val scanner = Scanner(System.`in`)

    println("Enter in sentence to compute profanity score for:")
    while(scanner.hasNextLine()) {
        val line = scanner.nextLine()
        val score = model.computeScore(line)
        println("Line: $line")
        println("Profanity Score: $score")
    }
}