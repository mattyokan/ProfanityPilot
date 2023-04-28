from datetime import datetime

from profanity_check import predict_prob
from pandas import read_csv

def true_positive(row):
    return row["good"] and row["predicted"]
def true_negative(row):
    return (not row["good"]) and (not row["predicted"])
def false_positive(row):
    return row["good"] and (not row["predicted"])
def false_negative(row):
    return (not row["good"]) and row["predicted"]


contents = read_csv('test_data_hasoc.csv')

thresholds = [0.25, 0.4, 0.5, 0.75]

print(f"Threshold\tRuntime\tPrecision\tRecall\tAccuracy")
for threshold in thresholds:
    results = []

    start = datetime.now()
    for idx, row in contents.iterrows():
        content = row["text"]
        good = row["task_2"] == "PRFN"
        results.append({
            "content": content,
            "good": good,
            "predicted": predict_prob([content])[0] < threshold
        })
    elapsed = datetime.now() - start

    true_positives = sum(map(true_positive, results))
    true_negatives = sum(map(true_negative, results))
    false_positives = sum(map(false_positive, results))
    false_negatives = sum(map(false_negative, results))

    precision = true_positives / (true_positives + false_positives)
    recall = true_positives / (true_positives + false_negatives)
    accuracy = (true_positives + true_negatives) / (true_positives + true_negatives + false_positives + false_negatives)
    print(f"{threshold}\t{elapsed.total_seconds() * 1000}\t{precision}\t{recall}\t{accuracy}")
