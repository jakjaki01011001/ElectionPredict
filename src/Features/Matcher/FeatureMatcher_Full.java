
package Features.Matcher;


public class FeatureMatcher_Full extends FeatureMatcher{

    @Override
    protected void buildRegex() {
        regex = "((^)|(\\s)|(" + (char) (8204) + "))" + regexCore + "(($)|(\\s)|(\\z)|[.!؟?]|(" + (char) (8204) + "))";
    }
    
    @Override
    public String toString() {
        return "FullMatch";
    }

}
