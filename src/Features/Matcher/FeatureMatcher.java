package Features.Matcher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class FeatureMatcher {

    protected String text;
    protected String regexCore;    //Pharase which is supposed to be matched either fully or partial
    protected String regex;
    protected Pattern pattern;
    protected Matcher matcher;

    public Pattern getPattern() {
        return pattern;
    }

    public void loadPattern(Pattern pattern) {
        this.pattern = pattern;
        this.regexCore=null;
        this.regex=null;
        matcher = null;
        text = null;
    }

    public void setRegexCorePhrase(String lookingPharase) {
        this.regexCore = lookingPharase;
        buildRegex();
        pattern = Pattern.compile(regex);
        matcher = null;
        text = null;
    }

    public void setText(String text) {
        this.text = text;
        matcher = pattern.matcher(text);
    }

    /**
     *
     * @return Matched String if found. else, Null is returned.
     */
    public String matchNext() {
        String resultString = null;
        if (matcher.find()) {
            resultString = matcher.group();
        }
        return resultString;
    }

    protected abstract void buildRegex();

}
