package amoghnagalla.popupquiz;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;



public class QuizActivity extends AppCompatActivity
{
    private boolean answerChecked = false;
    private boolean isCorrect = false;
    private int currentI = 0;
    private ArrayList <Question> questionList;
    private int correctCount;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        Question dynamicQuestion = new Question(" ");
        questionList = new ArrayList<Question>();
        //Question one
        questionList.add(new Question("What did Java Swing say to the mouse?"));
        questionList.get(0).addAnswer("Listen!", true);
        questionList.get(0).addAnswer("Pull your pants up!", false);
        questionList.get(0).addAnswer("I don't know!",false);
        questionList.get(0).addAnswer("Who cares!",false);
        //Question two
        questionList.add(new Question("What color is my underwear?"));
        questionList.get(1).addAnswer("Red!",false);
        questionList.get(1).addAnswer("White!",true);
        questionList.get(1).addAnswer("Blue",false);
        questionList.get(1).addAnswer("Sparkled",false);
        //Question three
        questionList.add(new Question("Which is the correct answer"));
        questionList.get(2).addAnswer("This one!",true);
        questionList.get(2).addAnswer("Nah!",false);
        questionList.get(2).addAnswer("Maybe!",false);
        questionList.get(2).addAnswer("Not at all!",false);
        //Question four
        questionList.add(new Question("Do you need another one?"));
        questionList.get(3).addAnswer("What was the question!",false);
        questionList.get(3).addAnswer("Can you repeat the question!",false);
        questionList.get(3).addAnswer("I did not understand the question!",false);
        questionList.get(3).addAnswer("Yea actually.",true);
        //Populate our quiz with question one
        getRandomQuestion(questionList);
        //OnClick listner which retrieves a new Question that has not been used
        Button nextQuestion = (Button) findViewById(R.id.question_button);
        nextQuestion.setVisibility(View.VISIBLE);
        nextQuestion.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                if(answerChecked){
                    if(isCorrect)
                    {
                        Toast.makeText(QuizActivity.this, "Your answer is correct!", Toast.LENGTH_SHORT).show();
                        correctCount++;
                        currentI++;
                        reset();
                        getRandomQuestion(questionList);
                    }
                    else{
                        Toast.makeText(QuizActivity.this, "Your answer is incorrect! Get gUd s0n", Toast.LENGTH_SHORT).show();
                        currentI++;
                        getRandomQuestion(questionList);
                        reset();
                    }


                }
                else
                {
                    Toast.makeText(QuizActivity.this, "Please pick an answer", Toast.LENGTH_SHORT).show();

                }


            }
        });
    }

    private void reset(){
        RadioButton answerOne = (RadioButton) findViewById(R.id.answer_one);
        RadioButton answerTwo = (RadioButton) findViewById(R.id.answer_two);
        RadioButton answerThree = (RadioButton) findViewById(R.id.answer_three);
        RadioButton answerFour = (RadioButton) findViewById(R.id.answer_four);
        Button nextQuestion = (Button) findViewById(R.id.question_button);
        //nextQuestion.setVisibility(View.VISIBLE);
        answerOne.setChecked(false);
        answerTwo.setChecked(false);
        answerThree.setChecked(false);
        answerFour.setChecked(false);
        answerChecked = false;
        isCorrect = false;
    }
    public void getRandomQuestion(ArrayList<Question> questionList)
    {
        Question temp = null;

//        int tempI = (int) (Math.random() * 4);
//        temp = questionList.get(tempI);
//        while (!temp.usedQuestion)
//        {
//            tempI = (int) (Math.random() * 4);
//            if (temp.usedQuestion)
//            {
//                continue;
//            }
//            else
//            {
//                temp = questionList.get(tempI);
//                break;
//            }
//        }

        if (currentI > 4){
            return;
        }
        else {
            temp = questionList.get(currentI);

        }

        fillAnswer(temp);

        TextView questionText =(TextView) findViewById(R.id.question_text);
        questionText.setText(temp.getQuestionText());


    }

    private void fillAnswer(Question question)
    {
        RadioButton answerOne = (RadioButton) findViewById(R.id.answer_one);
        RadioButton answerTwo = (RadioButton) findViewById(R.id.answer_two);
        RadioButton answerThree = (RadioButton) findViewById(R.id.answer_three);
        RadioButton answerFour = (RadioButton) findViewById(R.id.answer_four);
        String temp = question.getOneAnswer().getAnswerText();
        answerOne.setText(temp);
        answerTwo.setText(question.getOneAnswer().getAnswerText());
        answerThree.setText(question.getOneAnswer().getAnswerText());
        answerFour.setText(question.getOneAnswer().getAnswerText());
    }

    public void onRadioButtonClicked(View view) {
        // Is the view now checked?
        boolean checked = ((RadioButton) view).isChecked();

        RadioButton answerOne = (RadioButton) findViewById(R.id.answer_one);
        RadioButton answerTwo = (RadioButton) findViewById(R.id.answer_two);
        RadioButton answerThree = (RadioButton) findViewById(R.id.answer_three);
        RadioButton answerFour = (RadioButton) findViewById(R.id.answer_four);
        TextView questionText =(TextView) findViewById(R.id.question_text);

        if (answerChecked){
            answerOne.setChecked(false);
            answerTwo.setChecked(false);
            answerThree.setChecked(false);
            answerFour.setChecked(false);
            answerChecked = false;
            isCorrect = false;
            return;
        }

        // Check which RadioButton was clicked
        switch(view.getId()) {
            case R.id.answer_one:

                if (checked)
                {
                    answerChecked = true;
                    String tempText = answerOne.getText().toString();

                    if(questionList.get(currentI).findAnswer(tempText))
                    {
                        isCorrect = true;
                    }
                    else{
                        isCorrect = false;
                    }

                }
                else
                {
                    answerChecked = false;

                }
                //Toast.makeText(QuizActivity.this,"1--Checked: " + checked + " Answer checked: " + answerChecked + " Correct?: " + isCorrect + " " + answerOne.getText().toString(),Toast.LENGTH_LONG).show();
                break;
            case R.id.answer_two:

                if (checked)
                {

                    answerChecked = true;
                    if(questionList.get(currentI).findAnswer(answerTwo.getText().toString()))
                    {
                        isCorrect = true;
                    }
                    else{
                        isCorrect = false;
                    }
                }
                else
                {
                    answerChecked = false;
                }
               //Toast.makeText(QuizActivity.this,"2--Checked: " + checked + " Answer checked: " + answerChecked + " Correct?: " + isCorrect,Toast.LENGTH_LONG).show();

                break;
            case R.id.answer_three:

                if(checked)
                {
                    answerChecked = true;
                    if(questionList.get(currentI).findAnswer(answerThree.getText().toString()))
                    {

                        isCorrect = true;

                    }
                    else{
                        isCorrect = false;
                    }
                }
                else
                {
                    answerChecked = false;

                }
               //Toast.makeText(QuizActivity.this,"3--Checked: " + checked + " Answer checked: " + answerChecked + " Correct?: " + isCorrect,Toast.LENGTH_LONG).show();

                break;
            case R.id.answer_four:

                if(checked)
                {
                    answerChecked = true;
                    if(questionList.get(currentI).findAnswer(answerFour.getText().toString()))
                    {
                        isCorrect = true;

                    }
                    else{
                        isCorrect = false;
                    }

                }
                else
                {
                    answerChecked = false;

                }
                //Toast.makeText(QuizActivity.this,"4--Checked: " + checked + " Answer checked: " + answerChecked + " Correct?: " + isCorrect, Toast.LENGTH_LONG).show();

                break;

        }
    }

}
