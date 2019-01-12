package amoghnagalla.popupquiz;


public class Answer
{
    private String text;
    private boolean isThisTheCorrectAnswer;

    public Answer(String text, boolean isThisTheCorrectAnswer){
        this.text = text;
        this.isThisTheCorrectAnswer = isThisTheCorrectAnswer;
    }

    public boolean isThisTheCorrectAnswer(){
        return isThisTheCorrectAnswer;
    }
    public String getAnswerText(){
        return text;
    }
}

