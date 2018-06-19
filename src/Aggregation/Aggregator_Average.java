package Aggregation;

import java.util.ArrayList;

public class Aggregator_Average implements IAggregator {

    @Override
    public int run(ArrayList<Double> scores, int minScore, int maxScore) {
        double result = scores.stream().mapToDouble(Double::valueOf).average().getAsDouble();
        if (result > 3) {
            return (int) Math.ceil(result);
        } else if (result < 3) {
            return (int) Math.floor(result);
        } else {
            return 3;
        }
    }
}