package Aggregation;

import java.util.ArrayList;

public interface IAggregator {

    public int run(ArrayList<Double> scores, int minScore, int maxScore);
}
