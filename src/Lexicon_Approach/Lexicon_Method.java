package Lexicon_Approach;

import Features.Matcher.FeatureMatcher;
import Modules.Enums;
import Modules.LexiconRecord;
import Modules.MutableInteger;
import Modules.Record;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexicon_Method {

    public enum PutLabelOn {
        estimatedScore, labeledScore
    }

    ArrayList<? extends Record> dataSet;
    ArrayList<LexiconRecord> lexiconWords;
    ArrayList<String> negationWordsList;
    HashMap<Enums.Candidate, List<String>> candidatesRelatedExpressions;
    Map<String, Pattern> lexiconPatterns = Collections.synchronizedMap(new HashMap<String, Pattern>());
    Enums.AnalysisLevel analysisLevel;
    FeatureMatcher featureMatcher;
    PutLabelOn labelOn;
    boolean applyQuestionSentenceRule = false;

    public Lexicon_Method setDataSet(ArrayList<? extends Record> dataSet) {
        this.dataSet = dataSet;
        return this;
    }

    public Lexicon_Method setLexiconWords(ArrayList<LexiconRecord> lexiconWords) {
        this.lexiconWords = lexiconWords;
        return this;
    }

    public Lexicon_Method activeNegationRule(ArrayList<String> negationWordsList) {
        this.negationWordsList = negationWordsList;
        return this;
    }

    public Lexicon_Method activeQuestionSentencesRule(boolean activate) {
        applyQuestionSentenceRule = activate;
        return this;
    }

    public Lexicon_Method setAnalysisLevel(Enums.AnalysisLevel analysisLevel) {
        this.analysisLevel = analysisLevel;
        return this;
    }

    public Lexicon_Method setLabelOn(PutLabelOn labelOn) {
        this.labelOn = labelOn;
        return this;
    }

    public Lexicon_Method setFeatureMatcher(FeatureMatcher featureMatcher) {
        this.featureMatcher = featureMatcher;
        return this;
    }

    public Lexicon_Method setCandidatesRelatedExpressions(HashMap<Enums.Candidate, List<String>> candidatesRelatedExpressions) {
        this.candidatesRelatedExpressions = candidatesRelatedExpressions;
        return this;
    }

    /**
     * run method. estimate scores and apply new scores to the reviews object
     * you passed in.
     */
    public void run() {

        ScoreCalculator scoreCalculator = new ScoreCalculator().
                setDataSet(dataSet)
                .setLexiconWords(lexiconWords)
                .setAnalysisLevel(analysisLevel)
                .activeQuestionSentenceRule(applyQuestionSentenceRule)
                .setFeatureMatcher(featureMatcher)
                .setLexiconPatterns(lexiconPatterns)
                .activeNegationRule(negationWordsList);

        MutableInteger stepCounter = new MutableInteger(0);
        dataSet.parallelStream().forEach((record) -> {
            try {
                int score = scoreCalculator.run(record);
                calculateCandidatesWeights(record);
                switch (labelOn) {
                    case estimatedScore:
                        record.estimatedScore = score;
                        if (record.estimatedScore == 3) {
                            record.estimatedPolarity = Enums.Polarity.Neutral;
                        } else if (record.estimatedScore > 3) {
                            record.estimatedPolarity = Enums.Polarity.Positive;
                        } else if (record.estimatedScore < 3) {
                            record.estimatedPolarity = Enums.Polarity.Negative;
                        }
                        break;
                }

                double step;
                synchronized (stepCounter) {
                    step = stepCounter.get_incOne();
                }
                if (step % (dataSet.size() / 1000) == 0) {
                    System.out.println((step / dataSet.size() * 100) + " %");
                }

            } catch (Exception ex) {
                Logger.getLogger(Lexicon_Method.class.getName()).log(Level.SEVERE, null, ex);
                record.estimatedScore = 3;
                record.estimatedPolarity = Enums.Polarity.Neutral;
            }
        }
        );
    }

    // Specifying Targeted Candidate in each comment
    private void calculateCandidatesWeights(Record record) {

        candidatesRelatedExpressions.entrySet().stream().forEach(entry -> {
            Enums.Candidate candidate = entry.getKey();
            ArrayList<String> relatedExpressions = (ArrayList<String>) entry.getValue();
            int occurrenceCount = 0;
            StringBuilder regexBuilder = new StringBuilder();
            relatedExpressions.stream().forEach(expression -> {
                regexBuilder.append("(").append(expression).append(")|");
            });
            regexBuilder.deleteCharAt(regexBuilder.length() - 1);

            Pattern pattern = Pattern.compile(regexBuilder.toString(), Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(record.text);
            while (matcher.find()) {
                occurrenceCount++;
            }
            record.candidatesWeights.put(candidate, (float) occurrenceCount);
        });

        MutableInteger sumOccurrence = new MutableInteger(0);
        record.candidatesWeights.values().stream().forEach(w -> {
            sumOccurrence.setVal((int) (sumOccurrence.getVal() + w));
        });

        if (sumOccurrence.getVal() != 0) {
            record.candidatesWeights.entrySet().stream().forEach(entry -> {
                entry.setValue(entry.getValue() / sumOccurrence.getVal());
            });
        }
    }
}
