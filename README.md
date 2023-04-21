# ProfanityPilot

A simple Naive Bayes classifier solution to profanity detection.

# License

This project is currently all rights reserved, Copyright 2023 Matt Yokanovich. I may decide to relicense this software
under a permissive open-source license at some point in the future.

# Motivation

Many online communities contain realtime online chat functionality. Oftentimes, human moderation is not sufficient to
address the volume of messages sent in these communities (sometimes >100 messages a second in some large multiplayer
game communities!), and as such automated filtering is necessary to address this shortfall and prevent
obvious/low-effort insincere content.

In realtime chat environments, filter performance is still an important criteria, as messages cannot be deleted once
they have been sent, and latency in messages being broadcasted to other users is a critical concern. Because of this,
more sophisticated filtering techniques are not always feasible.

Within many online gaming communities specifically, the most frequent filtering option is still Regex filtering based on
a wordlist. This project hopes to provide a slightly more sophisticated and accurate filtering strategy for these
communities, with the goals of being performant, accurate, and easy to integrate.

# Provided Filtering Probabilities

You can train your own filtering probability map if you wish. This project includes an example of doing such in
the ``src/main/.../train`` folder of the project, which creates a dataset in the format ProfanityPilot can efficiently
read using
the [profanity-check](https://github.com/vzhou842/profanity-check/blob/master/profanity_check/data/clean_data.csv)
cleaned dataset, which itself sources data
from [t-davidson/hate-speech-and-offensive-language](https://github.com/t-davidson/hate-speech-and-offensive-language/tree/master/data)
and [Wikipedia comments from a Kaggle competition](https://www.kaggle.com/c/jigsaw-toxic-comment-classification-challenge).

If you wish to use this dataset, it is bundled with the application in a form that is compressed. This compression is
not necessary, but is done to avoid all the mean words from showing up in search indexing.

## Notes on use of provided dataset & BayesianClassifier

If you use the standard setup of the provided dataset & BayesianClassifier, choosing a threshold upon which to filter
messages is an important challenge.

The default provided threshold (0.4) is based on some trial and error and analysis of a ROC curve upon a testing
dataset. The table used for these recommendations is below:

| Threshold | Bayes Precision | Bayes Recall |
|-----------|-----------------|--------------|
| 0         | 0.04685816      | 0.61516554   |
| 0.1       | 0.59925835      | 0.92604892   |
| 0.2       | 0.63765601      | 0.92443169   |
| 0.3       | 0.66296294      | 0.92308927   |
| **0.4**   | 0.6830973       | 0.92159876   |
| 0.5       | 0.70138771      | 0.91981686   |
| 0.6       | 0.7184104       | 0.91795951   |
| 0.7       | 0.73605678      | 0.91584284   |
| 0.8       | 0.75751988      | 0.91304348   |
| 0.9       | 0.78607407      | 0.90838086   |
| 1         | 1               | 0.80013995   |

Below are some additional recommendations:

### Child-friendly online community:

**Threshold recommendation:** ~0.3 - ~0.4

**Comments:**

For a child-friendly community, it is important to prioritize catching as much profane content as possible to create a
safe environment for children. Because of this, I suggest a lower threshold that results in higher recall (catching more
profanity) at the cost of lower precision (more false positives).

### All-ages-friendly online community:

**Threshold recommendation:** ~0.4 - ~0.5

**Comments:**

For an all-ages-friendly community, it's essential to strike a balance between ensuring a welcoming environment for all
users and not over-filtering content. Choosing a value in this threshold strikes a balance between precision and recall,
making sure that most profane content is caught while minimizing false positives.

### Adult-friendly online community:

**Threshold recommendation:** ~0.55 - ~0.6

**Comments:**

In an adult-friendly online community, a balance between precision and recall is desired. Adults can handle some level
of profanity, but it's still important to filter out explicit content that goes beyond the community's standards. The
recommended thresholds offer a decent balance between precision and recall, maintaining a moderate level of filtering
while keeping false positives in check.
