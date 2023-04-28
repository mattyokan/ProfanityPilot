package me.yokan.profanitypilot.train

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import me.yokan.profanitypilot.classifier.randomforest.RandomForestClassifier
import me.yokan.profanitypilot.classifier.randomforest.RandomForestFactory
import me.yokan.profanitypilot.classifier.svm.SVMClassifier
import me.yokan.profanitypilot.classifier.svm.SVMModelFactory

class TrainRandomForest {

    fun generate() : RandomForestClassifier {
        val dataFile = TrainRandomForest::class.java.getResourceAsStream("/training/clean_data.csv")
        val data = csvReader().readAll(dataFile)
        var allMessages = data
            .drop(1)
            .map { info ->
                val isOffensive = !(info[0] == "1")
                val message = info[1]
                RandomForestFactory.Message(message, isOffensive)
            }

        return RandomForestClassifier(allMessages)
    }
}
