import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class MonteCarlo {
    static Random r = new Random();
    static int nActions = 5;
    static double epsilon = 1e-6;

    MonteCarlo[] children;
    double nVisits, totValue;

    public void selectAction() {
        List<MonteCarlo> visited = new LinkedList<MonteCarlo>();
        MonteCarlo cur = this;
        visited.add(this);
        while (!cur.isLeaf()) {
            cur = cur.select();
            visited.add(cur);
        }
        cur.expand();
        MonteCarlo newNode = cur.select();
        visited.add(newNode);
        double value =  rollOut(newNode);
        for (MonteCarlo node : visited) {
            // would need extra logic for n-player game
            node.updateStats(value);
        }
    }

    public void expand() {
        children = new MonteCarlo[nActions];
        for (int i=0; i<nActions; i++) {
            children[i] = new MonteCarlo();
        }
    }

    private MonteCarlo select() {
        MonteCarlo selected = null;
        double bestValue = Double.MIN_VALUE;
        for (MonteCarlo c : children) {
            double uctValue = c.totValue / (c.nVisits + epsilon) +
                    Math.sqrt(Math.log(nVisits+1) / (c.nVisits + epsilon)) +
                    r.nextDouble() * epsilon;
            // small random number to break ties randomly in unexpanded nodes
            if (uctValue > bestValue) {
                selected = c;
                bestValue = uctValue;
            }
        }
        return selected;
    }

    public boolean isLeaf() {
        return children == null;
    }

    public double rollOut(MonteCarlo tn) {
        // ultimately a roll out will end in some value
        // assume for now that it ends in a win or a loss
        // and just return this at random
        return r.nextInt(2);
    }

    public void updateStats(double value) {
        nVisits++;
        totValue += value;
    }

    public int arity() {
        return children == null ? 0 : children.length;
    }
}