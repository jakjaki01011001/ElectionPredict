package Modules;

public class MutableInteger {

    private int val;

    public MutableInteger(int val) {
        this.val = val;
    }

    public synchronized void increment(int inc) {
        this.val += inc;
    }

    public synchronized int getVal() {
        return this.val;
    }

    public synchronized void setVal(int val) {
        this.val = val;
    }
    
    public synchronized int get_incOne() {
        return this.val++;
    }
}
