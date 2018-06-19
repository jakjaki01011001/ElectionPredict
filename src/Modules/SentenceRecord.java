package Modules;
public class SentenceRecord extends Record {

    public int reviewID = 0;

    public SentenceRecord(String text) {
        super();
        this.text = text;
    }

    public SentenceRecord(SentenceRecord sentence) {
        super(sentence);
        this.reviewID = sentence.reviewID;
    }

    public SentenceRecord() {
        super();
    }
}
