package Lexicon_Approach;

import Aggregation.Aggregator_Average;
import Aggregation.IAggregator;
import Features.Matcher.FeatureMatcher;
import Modules.LexiconRecord;
import Modules.CommentRecord;
import Modules.Enums;
import Modules.Record;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ScoreCalculator {

    ArrayList<? extends Record> dataSet;
    ArrayList<LexiconRecord> lexiconWords;
    Enums.AnalysisLevel analysisLevel;
    ArrayList<String> negationWordsList;
    FeatureMatcher featureMatcher_Sample;
    boolean activeQuestionSentencesRule = false;
    Map<String, Pattern> lexiconPatterns;

    public ScoreCalculator setDataSet(ArrayList<? extends Record> dataSet) {
        this.dataSet = dataSet;
        return this;
    }

    public ScoreCalculator setLexiconWords(ArrayList<LexiconRecord> lexiconWords) {
        this.lexiconWords = lexiconWords;
        return this;
    }

    public ScoreCalculator setAnalysisLevel(Enums.AnalysisLevel analysisLevel) {
        this.analysisLevel = analysisLevel;
        return this;
    }

    public ScoreCalculator activeNegationRule(ArrayList<String> negationWordsList) {
        this.negationWordsList = negationWordsList;
        return this;
    }

    public ScoreCalculator setFeatureMatcher(FeatureMatcher featureMatcher) {
        this.featureMatcher_Sample = featureMatcher;
        return this;
    }

    public ScoreCalculator setLexiconPatterns(Map<String, Pattern> lexiconPatterns) {
        this.lexiconPatterns = lexiconPatterns;
        return this;
    }

    public ScoreCalculator activeQuestionSentenceRule(boolean active) {
        this.activeQuestionSentencesRule = active;
        return this;
    }

    public int run(Record record) throws Exception {
        int result = 3;
        if (record instanceof CommentRecord) {
            CommentRecord comment = (CommentRecord) record;
            switch (analysisLevel) {
                case review:
                    if (activeQuestionSentencesRule && isQuestionExpression(comment.text)) {
                        result = 3;
                    } else {
                        // claculate Sentiment Score of text
                        result = calcStringScore(comment);
                        if (negationWordsList != null) {
                            result = applyNegationWordsRule(comment.text, result);
                        }
                    }
                    break;
            }
        }else {
            throw new Exception("data set is build of neither Comment nor Sentence");
        }
        return result;
    }

    // claculate Sentiment Score of text
    public int calcStringScore(Record record) {
        FeatureMatcher featureMatcher = null;
        try {
            featureMatcher = featureMatcher_Sample.getClass().newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(ScoreCalculator.class.getName()).log(Level.SEVERE, null, ex);
        }

        String text = record.text;
        ArrayList<Integer> scores = new ArrayList<>();
        for (LexiconRecord lexicon : lexiconWords) {
            Pattern pattern = lexiconPatterns.get(lexicon.word);
            if (pattern == null) {
                featureMatcher.setRegexCorePhrase(lexicon.word);
                pattern = featureMatcher.getPattern();
                lexiconPatterns.put(lexicon.word, pattern);
            } else {
                featureMatcher.loadPattern(pattern);
            }

            featureMatcher.setText(text);
            while (featureMatcher.matchNext() != null) {
                scores.add(lexicon.score);
            }
        }

        if (scores.isEmpty()) {
            return 3;
        } else {
            IAggregator aggregator = new Aggregator_Average();
            int score = aggregator.run(scores.stream().map(x -> (double) x).collect(Collectors.toCollection(ArrayList::new)), 1, 5);
            return score;
        }
    }

    private int applyNegationWordsRule(String text, int calculatedScore) {
        int result = calculatedScore;
        for (int i = 0; i < negationWordsList.size(); i++) {
            // 8204 is for Half Spaces
            String regex = "((^)|(\\s)|(" + (char) (8204) + "))"    + negationWordsList.get(i)
                    + "(($)|(\\s)|(\\z)|[.!؟?]|(" + (char) (8204) + "))";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(text);

            if (matcher.find()) {
                int distance = 3 - calculatedScore;
                result = 3 + distance;
                if (result == 3) {
                    result = 2;
                }
                break;
            }
        }
        return result;
    }

    private boolean isQuestionExpression(String text) {
        String regex = "[?؟]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        boolean result = matcher.find();
        return result;
    }

}
