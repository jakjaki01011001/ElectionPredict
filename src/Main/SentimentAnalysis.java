package Main;

import Evaluation.VoteEvaluator;
import Utils.IO_Operations;
import Lexicon_Approach.Lexicon_Method;
import Modules.AnalysisConfiguration;
import Modules.Enums;
import Modules.LexiconRecord;
import Modules.Record;
import java.util.ArrayList;
import org.apache.commons.io.FilenameUtils;

public class SentimentAnalysis {

    private AnalysisConfiguration configuration;
    private String lexicon_URL;

    public SentimentAnalysis(AnalysisConfiguration configuration) {
        this.configuration = configuration;
        if (configuration.Lexicon != null) {
            lexicon_URL = Main_SA.LexiconsURLS.get(configuration.Lexicon.toString());
        }
    }

    public void run() throws Exception {

        System.out.println("Analysis : " + configuration.description + " started:");
        IO_Operations iO_Operations = new IO_Operations();
        ArrayList<String> negationWordsList = iO_Operations.loadStringList(Main_SA.NEGATIONWORDS_LIST_URL);
        // Load Datasets
        final ArrayList<? extends Record> dataSet = iO_Operations.loadDataSetJson(Main_SA.DATASET_DOCUMENT_FOLDER_URL, false, configuration.limitDataset);

        // Load Lexicon
        final ArrayList<LexiconRecord> lexiconWords = new ArrayList<>();
        if (lexicon_URL != null) {
            String fileType = FilenameUtils.getExtension(lexicon_URL);
            if (fileType.equalsIgnoreCase("xml")) {
                lexiconWords.addAll(iO_Operations.loadLexiconXML(lexicon_URL));
            } else if (fileType.equalsIgnoreCase("xlsx") || fileType.equalsIgnoreCase("xls")) {
                lexiconWords.addAll(iO_Operations.loadLexiconExcel(lexicon_URL));
            }
        }

        Lexicon_Method lexiconMethod = null;
        if (configuration.methodInUse == Enums.AnalysisMethodInUse.lexicon) {
            // Initiate LexiconMethod
            lexiconMethod = new Lexicon_Method().setDataSet(dataSet)
                    .setLexiconWords(lexiconWords)
                    .setLabelOn(Lexicon_Method.PutLabelOn.estimatedScore)
                    .setAnalysisLevel(configuration.analysisLevel)
                    .setFeatureMatcher(configuration.featureMatcher)    // Feature Match is either Full Match or Partial Match.
                    .setCandidatesRelatedExpressions(Main.Main_SA.candidatesRelatedExpressions);

            if (configuration.lexiconBasedRules.contains(Enums.LexiconBasedRule.NegationWords)) {
                lexiconMethod.activeNegationRule(negationWordsList);
            }
            
            // Question Rule is for neglecting Question Sentences which dont carry specific sentimental burden.
            if (configuration.lexiconBasedRules.contains(Enums.LexiconBasedRule.QuestionSentences)) {
                lexiconMethod.activeQuestionSentencesRule(true);
            }
            
            // Run Core Phase of Analysis
            lexiconMethod.run();
        }

        //Evaluate Votes.
        VoteEvaluator voteEvaluator = new VoteEvaluator(dataSet);
        voteEvaluator.run();
    }
}
