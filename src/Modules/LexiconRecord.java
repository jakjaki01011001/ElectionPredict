package Modules;

public class LexiconRecord {

    public int id;
    public String word;
    public int score;

    public LexiconRecord(LexiconRecord lexRec) {
        this.id = lexRec.id;
        this.word = lexRec.word;
        this.score = lexRec.score;
    }

    public LexiconRecord() {
    }
    
    public LexiconRecord get() {
        return this;
    }
}
