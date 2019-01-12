package amoghnagalla.popupquiz;

import android.widget.Toast;


public class Question
{
    private boolean used = false;

    Answer[] answerList = new Answer[4];
    boolean[] usedAnswer = new boolean[4];
    int count = 0;
    String questiontext;
    boolean usedQuestion;
    int current = 0;

    public Question(String questiontext)
    {
        this.questiontext = questiontext;
    }

    public String getQuestionText(){
        return questiontext;
    }

    public Answer getOneAnswer(){


        int index = (int) (Math.random()*4);
        while(usedAnswer[index]){
            index = (int) (Math.random()*4);
            if (usedAnswer[index]){
                continue;
            }
            else{
                usedAnswer[index] = true;
                return answerList[index];
            }

        }
        usedAnswer[index] = true;
        return answerList[index];


    }

    public boolean findAnswer(String text){
        boolean answerBoo = false;
        for (int i=0; i<4;i++){
            if(answerList[i].getAnswerText().equals(text)){
                answerBoo = answerList[i].isThisTheCorrectAnswer();
                break;
            }
        }

        return answerBoo;
    }

    public String findAnswerText(String text){
        Answer answerBoo = null;

        for (int i=0; i<4;i++){

            if(answerList[i].getAnswerText().equals(text)){

                answerBoo = answerList[i];
                break;
            }
        }

        return answerBoo.getAnswerText();
    }

    public void setUsed()
    {
        used = true;
    }

    public void addAnswer(String text, boolean correctanswer)
    {
        if (count != 4)
        {
            Answer answer = new Answer(text, correctanswer);
            answerList[count] = answer;
            count++;
        }
        else{
            return;
        }
    }

    public void resetAnswers()
    {
        answerList = new Answer[4];
        usedAnswer = new boolean[4];
    }

}
