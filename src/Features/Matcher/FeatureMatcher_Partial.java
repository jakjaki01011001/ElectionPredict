package Features.Matcher;

public class FeatureMatcher_Partial extends FeatureMatcher {

    @Override
    protected void buildRegex() {
        
        regex = "((^)|(\\s)|(" + (char) (8204) + "))" + regexCore   //8204 is for halfSpace
                + "("
                + "($)|(\\s)|(\\z)|[!.?]|(" + (char) (8204) + ")|"
                // Possible PostFixes
                + "(م)|(ی)|(یم)|(ید)|(ند)|(ت)|(ش)|(مان)|(شان)|(تان)|(ه)|(ا)|(یه)|(گی)|(انگی)|(نگی)|(اش)|(هاش)|(های)|(ها)|(هایه)" 
                + ")";
    }

    @Override
    public String toString() {
        return "PartialMatch";
    }

}
