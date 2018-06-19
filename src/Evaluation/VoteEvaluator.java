package Evaluation;

import Modules.Enums;
import Modules.Enums.Candidate;
import Modules.Record;
import Utils.Opponents;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class VoteEvaluator {

    public HashMap<Candidate, Double> candidatesVotes = new HashMap<>();
    public ArrayList<? extends Record> dataSet;

    public VoteEvaluator(ArrayList<? extends Record> dataSet) {
        this.dataSet = dataSet;
        Arrays.asList(Main.Main_SA.CANDIDATES).stream().forEach(candidate -> {
            candidatesVotes.put(candidate, 0d);
        });
    }

    public void run() {

        dataSet.stream().forEach(record -> {
            HashMap<Candidate, Float> votes = new HashMap<>();
            if (record.estimatedPolarity != Enums.Polarity.Neutral) {
                float maxWeight = record.candidatesWeights.values().stream().max(Float::compare).get();
                int countMax = (int) record.candidatesWeights.values().stream().filter(x -> x == maxWeight).count();
                if (countMax == 1) {
                    Candidate dominantCandidate = record.candidatesWeights.entrySet().stream()
                            .filter(entry -> entry.getValue() == maxWeight).findFirst().get().getKey();
                    if (record.estimatedPolarity == Enums.Polarity.Positive) {
                        votes.put(dominantCandidate, 1f);
                    } else if (record.estimatedPolarity == Enums.Polarity.Negative) {   // Cast Vote For Opponent Candidate
                        Candidate opponentCandidate = new Opponents().opponentsHashMap.get(dominantCandidate);
                        votes.put(opponentCandidate, 1f);
                    }
                    votes.entrySet().stream().forEach(entry -> {
                        double vote = entry.getValue() * record.publishedDateLevel * record.viewCountLevel; //Applying Weights
                        candidatesVotes.put(entry.getKey(), candidatesVotes.get(entry.getKey()) + vote);
                    });
                }
            }
        });
        print();
    }

    private void print() {
        System.out.println("\n\nVotes Count : ");
        candidatesVotes.entrySet().stream().forEach(entry -> {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        });
        System.out.println("");
        double sumVotes = candidatesVotes.entrySet().stream().mapToDouble(x -> x.getValue()).sum();
        candidatesVotes.entrySet().stream().forEach(entry -> {
            System.out.println(entry.getKey() + " : " + (entry.getValue() / sumVotes * 100) + "%");
        });
        System.out.println("");
    }
}
