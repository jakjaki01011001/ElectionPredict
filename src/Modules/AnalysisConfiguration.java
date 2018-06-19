package Modules;

import Features.Matcher.FeatureMatcher;
import Modules.Enums.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnalysisConfiguration {

    public String description;
    public AnalysisLevel analysisLevel;
    public AnalysisMethodInUse methodInUse;
    public LexiconInUse Lexicon;
    public List<LexiconBasedRule> lexiconBasedRules = new ArrayList<>();

    /**
     * Both for Lexicon-Based and ML-Based
     */
    public FeatureMatcher featureMatcher;
    public boolean limitDataset;

    public AnalysisConfiguration setFeatureMatcher(FeatureMatcher featureMatcher) {
        this.featureMatcher = featureMatcher;
        return this;
    }

    public AnalysisConfiguration setAnalysisLevel(AnalysisLevel analysisLevel) {
        this.analysisLevel = analysisLevel;
        return this;
    }

    public AnalysisConfiguration setMethodInUse(AnalysisMethodInUse methodInUse) {
        this.methodInUse = methodInUse;
        return this;
    }

    public AnalysisConfiguration setLexicon(LexiconInUse Lexicon) {
        this.Lexicon = Lexicon;
        return this;
    }

    public AnalysisConfiguration setLimitDataset(boolean limitDataset) {
        this.limitDataset = limitDataset;
        return this;
    }

    public AnalysisConfiguration setLexiconBasedRules(LexiconBasedRule... lexiconBasedRules) {
        this.lexiconBasedRules = Arrays.asList(lexiconBasedRules);
        return this;
    }

    public AnalysisConfiguration create() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(methodInUse).append("_").append(analysisLevel);
        if(methodInUse==AnalysisMethodInUse.lexicon) {
            strBuilder.append("_").append(Lexicon.toString());
            lexiconBasedRules.stream().forEach(rule -> {
                strBuilder.append("_").append(rule);
            });
        }
        strBuilder.append("_").append(featureMatcher.toString());
        if (limitDataset) {
            strBuilder.append("_Limited");
        } else {
            strBuilder.append("_unlimited");
        }

        description = strBuilder.toString();
        return this;
    }
}
