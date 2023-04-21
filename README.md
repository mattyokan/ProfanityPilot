# ProfanityPilot

A simple Naive Bayes classifier solution to profanity detection.

# License

This project is currently all rights reserved, Copyright 2023 Matt Yokanovich. I may decide to relicense this software under a permissive open-source license at some point in the future.

# Motivation

Many online communities contain realtime online chat functionality. Oftentimes, human moderation is not sufficient to address the volume of messages sent in these communities (sometimes >100 messages a second in some large multiplayer game communities!), and as such automated filtering is necessary to address this shortfall and prevent obvious/low-effort insincere content.

In realtime chat environments, filter performance is still an important criteria, as messages cannot be deleted once they have been sent, and latency in messages being broadcasted to other users is a critical concern. Because of this, more sophisticated filtering techniques are not always feasible.

Within many online gaming communities specifically, the most frequent filtering option is still Regex filtering based on a wordlist. This project hopes to provide a slightly more sophisticated and accurate filtering strategy for these communities, with the goals of being performant, accurate, and easy to integrate.


# Provided Filtering Probabilities

You can train your own filtering probability map if you wish. This project includes an example of doing such in the ``src/main/.../train`` folder of the project, which creates a dataset in the format ProfanityPilot can efficiently read using the [profanity-check](https://github.com/vzhou842/profanity-check/blob/master/profanity_check/data/clean_data.csv) cleaned dataset, which itself sources data from [t-davidson/hate-speech-and-offensive-language](https://github.com/t-davidson/hate-speech-and-offensive-language/tree/master/data) and [Wikipedia comments from a Kaggle competition](https://www.kaggle.com/c/jigsaw-toxic-comment-classification-challenge). 

If you wish to use this dataset, it is bundled with the application in a form that is compressed. This compression is not necessary, but is done to avoid all the mean words from showing up in search indexing.