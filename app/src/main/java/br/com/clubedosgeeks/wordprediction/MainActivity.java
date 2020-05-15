package br.com.clubedosgeeks.wordprediction;

import androidx.appcompat.app.AppCompatActivity;
import java.nio.MappedByteBuffer;
import android.app.Activity;
import java.io.IOException;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import java.io.FileInputStream;
import java.io.InputStream;
import android.content.res.AssetFileDescriptor;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import org.tensorflow.lite.Interpreter;
import android.app.Activity;
import android.content.Context;
import  org.tensorflow.lite.TensorFlowLite;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.widget.ListView;

import org.json.*;


public class MainActivity extends AppCompatActivity  implements View.OnClickListener {
    protected Interpreter tflite;
    EditText editText;
    List<String> tokens = new ArrayList<String>();
    ArrayList<String> nextWords=new ArrayList<String>();
    private static final int SEQUENCE_LEN = 3;
    private static final int MAX_RESULTS = 10;
    private ArrayAdapter<String> arrayAdapter;
    List<String> sentence = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ListView lv = (ListView) findViewById(R.id.lv);
        arrayAdapter = new ArrayAdapter<String>(this,  android.R.layout.simple_list_item_1, nextWords);
        lv.setAdapter(arrayAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                addWord(position);
            }
        });

        this.editText = (EditText)findViewById(R.id.editText);

        this.editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                sentence = Arrays.asList(editText.getText().toString().split("\\W+"));
//                getNextWords();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
//        this.editText.setFocusable(false);
//        this.editText.setEnabled(false);
//        this.editText.setCursorVisible(false);
//        this.editText.setKeyListener(null);
//        this.editText.setBackgroundColor(Color.TRANSPARENT);
        try{
            this.tflite = new Interpreter(loadModelFile());

        }
        catch(IOException e) {
            e.printStackTrace();
            Log.d("JAYR", "DEU ERRADO");
        }
        try{
            JSONArray jsonTokens = new JSONArray(loadJSONFromAsset());
            for (int i = 0 ; i < jsonTokens.length(); i++) {
                this.tokens.add(jsonTokens.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        this.getNextWords();
    }

    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = this.getAssets().openFd(getModelPath());
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private String getModelPath(){
        return "converted_model.tflite";
    }

    private void getNextWords() {

        List<Integer> tokens_id = new ArrayList<Integer>();

        for (int i = 0 ; i < this.sentence.size(); i++) {
            tokens_id.add(this.tokens.indexOf(this.sentence.get(i)));
        }

        if (tokens_id.size() < SEQUENCE_LEN) {
            int initial_size = tokens_id.size();
            for (int j = 0 ; j < SEQUENCE_LEN-initial_size; j++) {
                tokens_id.add(0,0);
            }
        }
        float [][] result = new float[1][tokens.size()];
        float [][][] input = new float[1][SEQUENCE_LEN][tokens.size()];

        for (int i = 0; i < SEQUENCE_LEN ; i++ ) {
            Log.d("TOKENS", tokens_id.toString());
            for (int j = 0 ; j < tokens.size(); j ++) {
                if (j == tokens_id.get(tokens_id.size()-(i+1))) {
                    input[0][i][j] = 1;
                } else {
                    input[0][i][j] = 0;
                }
            }
//            input[0][i] = tokens_id.get(tokens_id.size()-(i+1));

        }

        this.tflite.run(input, result);
        this.showNextWords(result);

    }

    public void onClick(View v) {
        // do something when the button is clicke
//        String text = this.editText.getText().toString();

    }

    private  void showNextWords (float[][] wordProbsArray) {
        this.nextWords.clear();
        PriorityQueue<Recognition> pq =
                new PriorityQueue<>(
                        MAX_RESULTS,
                        new Comparator<Recognition>() {
                            @Override
                            public int compare(Recognition lhs, Recognition rhs) {
                                return Float.compare(rhs.getConfidence(), lhs.getConfidence());
                            }
                        });

        for (int i = 0 ; i < this.tokens.size(); i++) {
            float confidence = wordProbsArray[0][i];
            pq.add(new Recognition(""+i, this.tokens.get(i), confidence));
        }

//        final ArrayList<Recognition> recognitions = new ArrayList<>();
        int recognitionsSize = Math.min(pq.size(), MAX_RESULTS);
        for (int i = 0; i < recognitionsSize; ++i) {
//            recognitions.add(pq.poll());
            List<String> new_sentence = new ArrayList<String>(this.sentence);
            new_sentence.add(pq.poll().getWord());
            this.nextWords.add(TextUtils.join(" ", new_sentence));
            Log.d("OPCAO", TextUtils.join(" ", new_sentence));
            this.arrayAdapter.notifyDataSetChanged();


        }
    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = this.getAssets().open("vocab.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private void addWord(int position) {
//        this.sentence.add(this.nextWords.get(position));
        Log.d("asdas", this.nextWords.get(position)+" ");
        this.editText.setText(this.nextWords.get(position)+" ");
        this.editText.setSelection(this.editText.getText().length());
        this.sentence = Arrays.asList(this.nextWords.get(position).split("\\W+"));

        this.getNextWords();
    }


}
