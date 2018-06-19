package Modules;

import java.util.ArrayList;

public class CommentRecord extends Record {

    public ArrayList<SentenceRecord> sentences = new ArrayList<>();

    public CommentRecord() {
        super();
    }

    public CommentRecord(CommentRecord comment) {
        super(comment);
        sentences.addAll(comment.sentences);  // its not a deep clone
    }

    @Override
    public CommentRecord get() {
        return this;
    }
}
