package Modules;

import Modules.Enums.Candidate;
import Modules.Enums.Polarity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Record {

    public int id;
    public String text;
    public float estimatedScore;
    public Polarity estimatedPolarity;

    public int viewCount;
    public long publishedDate;
    public byte publishedDateLevel;         //1 to 5
    public byte viewCountLevel;             //1 to 5
    public HashMap<Candidate, Float> candidatesWeights = new HashMap<>(); // weight is between 0 to 1

    public Record(Record record) {
        id = record.id;
        estimatedScore = record.estimatedScore;
        text = record.text;
        estimatedPolarity = record.estimatedPolarity;
        candidatesWeights.putAll(record.candidatesWeights);
    }

    public Record() {
        Arrays.asList(Main.Main_SA.CANDIDATES).stream().forEach(candid->{
            candidatesWeights.put(candid, 0f);
        });
    }

    public Record get() {
        return this;
    }

}
