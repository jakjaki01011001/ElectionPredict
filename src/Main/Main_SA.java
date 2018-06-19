package Main;

import Features.Matcher.FeatureMatcher_Partial;
import Utils.IO_Operations;
import Modules.AnalysisConfiguration;
import Modules.Enums.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main_SA {

    public static final String ROOTURI = new File(System.getProperty("user.dir")).getParent() + "/Data/";

    // Dataset Json Folder ->the folder which contains all json file (Level 1,2,3)
    //********* Lotfan File haye Json ra dar in Pushe gharar dahid.*********//
    //********* Lotfan File haye Json ra dar in Pushe gharar dahid.*********//
    public static final String DATASET_DOCUMENT_FOLDER_URL = ROOTURI + "DataSet/DataSet-IransElection96/jsonData/usedData/";
    //********* Lotfan File haye Json ra dar in Pushe gharar dahid.*********//
    //********* Lotfan File haye Json ra dar in Pushe gharar dahid.*********//

    // Expressions folder indicating debate around each candidate.
    public static final String CANDIDATES_RELATED_EXPRESSIONS_FOLDER_URL = ROOTURI + "DataSet/DataSet-IransElection96/CandidatesRelatedExpressions/";
    public static final String NEGATIONWORDS_LIST_URL = ROOTURI + "persianNegationWordsList.txt";
    public static final String LEXICON_URL_LEXIPERS = ROOTURI + "Lexicon/LexiPersV1.0/Data/adj-final.xml";
    public static final String LEXICON_URL_ADJECTIVES = ROOTURI + "Lexicon/AdjectiveLexicon/AdjectiveLexicon_Reduced.xlsx";
    public static final String LEXICON_URL_CNRC = ROOTURI + "Lexicon/NRC Lexicon/CNRC.xlsx";
    public static final String LEXICON_URL_LOOKUP = ROOTURI + "Lexicon/Lookup Table Lexicon/PersianLookupTable-5Star.xlsx";
// Expressions indicating debate around each candidate.
    public static HashMap<Candidate, List<String>> candidatesRelatedExpressions = new HashMap<>();
    public static HashMap<String, String> LexiconsURLS = new HashMap<>();
// Specifying all Present Candidates
    public static final Candidate[] CANDIDATES = {Candidate.HashemiTaba, Candidate.Raisi, Candidate.Rouhani, Candidate.Mirsalim};

    private static void initialization() {
        LexiconsURLS.put(LexiconInUse.LexiPers.toString(), LEXICON_URL_LEXIPERS);
        LexiconsURLS.put(LexiconInUse.adj.toString(), LEXICON_URL_ADJECTIVES);
        LexiconsURLS.put(LexiconInUse.lookup.toString(), LEXICON_URL_LOOKUP);
        LexiconsURLS.put(LexiconInUse.CNRC.toString(), LEXICON_URL_CNRC);
        loadCandidatesRelatedExpressions();
    }

    private static void initiateConfigurations(ArrayList<AnalysisConfiguration> configurations) {

        // Different Possible Configurations for Election Analysis
        configurations.add(new AnalysisConfiguration()
                .setMethodInUse(AnalysisMethodInUse.lexicon)
                .setAnalysisLevel(AnalysisLevel.review) // Investigate Analysis in Review Level.
                .setLexicon(LexiconInUse.LexiPers) //use LexiPers Lexicon
                .setFeatureMatcher(new FeatureMatcher_Partial()) // Use Partial Matcher for lexicon match
                .setLexiconBasedRules(LexiconBasedRule.NegationWords)
                .setLimitDataset(false)
                .create());
    }

    public static void main(String[] args) {

        initialization();
        ArrayList<AnalysisConfiguration> configurations = new ArrayList<>();
        initiateConfigurations(configurations);

        ArrayList<SentimentAnalysis> activeAnalysis = new ArrayList<>();
        configurations.stream().forEach(config -> {
            activeAnalysis.add(new SentimentAnalysis(config));
        });

        //Runing SentimentAnalysis********************************************************************
        activeAnalysis.stream().forEach(analysis -> {
            try {
                analysis.run();
                Thread.sleep(240000);
            } catch (Exception ex) {
                Logger.getLogger(Main_SA.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
    
    private static void loadCandidatesRelatedExpressions() {
        IO_Operations iO_Operations = new IO_Operations();
        Arrays.asList(CANDIDATES).stream().forEach(candidate -> {
            ArrayList<String> relatedExpressions = iO_Operations
                    .loadStringList(CANDIDATES_RELATED_EXPRESSIONS_FOLDER_URL + candidate + ".txt");
            candidatesRelatedExpressions.put(candidate, relatedExpressions);
        });
    }
}
