package edu.asu.heal.promisapiv3.apiv31.model;

/**
 * A Question represents a survey question of some type. These could be questions
 * that appear on a PROMIS form, related to medication adherence, in-game question,
 * computer-adaptive testing etc. The Question could also be of just about any type,
 * such as MCSA, MCMA, Likert, multipart. The Java object is just a passthrough from
 * the database via JSON, so we aren't creating subtypes right now. It is immutable.
 * @author kevinagary
 *
 */
public class Question implements java.io.Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 939394312806437709L;

	// We could make this extensible via a properties file but we'll wait on that
	public enum Type {
		LIKERT5, LIKERT7, LIKERT10, MCSA, MCMA, RATING, bodypain, CUSTOM, MCMADW ,generalizedpain
	}

	private final String questionId;
	private final Question.Type questionType;
	private final String questionStem; // JSON of the text of the question
	private final String questionMetadata;  // JSON with a metadata structure the app may understand
	private final int questionOptionType;
	private final String shortForm;

	public Question(String id, Question.Type type, String stem, String md,String shortform) {
		this(id, type, stem, md, 1,shortform);
	}

	public Question(String id, Question.Type type, String stem, String md, int questionOption, String shortform) {
		// populated from DB
		questionId = id;
		questionType = type;
		questionStem = stem;
		questionMetadata = md;
		questionOptionType = questionOption;
		shortForm = shortform;
	}

	public String getShortForm() {
		return shortForm;
	}

	public String getQuestionId() {
		return questionId;
	}

	public Question.Type getQuestionType() {
		return questionType;
	}

	public String getQuestionStem() {
		return questionStem;
	}

	public String getQuestionMetadata() {
		return questionMetadata;
	}

	public int getQuestionOptionType() {
		return questionOptionType;
	}
	/*@Override
	public int hashCode(){
        //System.out.println("In hashcode");
        int hashcode = 0;
        hashcode = Integer.parseInt(questionId)*20;
        hashcode += questionStem.hashCode();
        System.out.println("The generated hashcode is::"+hashcode);
        return hashcode;
    }
    @Override
    public boolean equals(Object obj){
        System.out.println("In equals");
        if (obj instanceof Question) {
            Question question = (Question) obj;
            return (question.questionId.equals(this.questionId));
        } else {
            return false;
        }
    }*/
}
