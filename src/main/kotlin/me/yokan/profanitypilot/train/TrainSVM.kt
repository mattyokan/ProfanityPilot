package me.yokan.profanitypilot.train

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import me.yokan.profanitypilot.classifier.svm.SVMClassifier
import me.yokan.profanitypilot.classifier.svm.SVMModelFactory

class TrainSVM {

    fun generate() : SVMClassifier {
        val dataFile = TrainSVM::class.java.getResourceAsStream("/training/clean_data.csv")
        val data = csvReader().readAll(dataFile)
        var allMessages = data
            .drop(1)
            .map { info ->
                val isOffensive = info[0] == "1"
                val message = info[1]
                SVMModelFactory.Message(message, isOffensive)
            }

        return SVMClassifier(allMessages)
    }
}
