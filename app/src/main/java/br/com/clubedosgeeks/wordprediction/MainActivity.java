package br.com.clubedosgeeks.wordprediction;

import androidx.appcompat.app.AppCompatActivity;
import java.nio.MappedByteBuffer;
import android.app.Activity;
import java.io.IOException;
import android.os.Bundle;
import java.io.FileInputStream;
import java.io.InputStream;
import android.content.res.AssetFileDescriptor;
import java.nio.channels.FileChannel;
import org.tensorflow.lite.Interpreter;
import android.app.Activity;
import android.content.Context;
import  org.tensorflow.lite.TensorFlowLite;

import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import org.json.*;


public class MainActivity extends AppCompatActivity  implements View.OnClickListener {
    protected Interpreter tflite;
    EditText editText;
    JSONArray tokens;
    int sequence_len = 5;
    String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Button button = (Button)findViewById(R.id.button2);
//        button.setOnClickListener(this);

        this.editText = (EditText)findViewById(R.id.editText);
        try{
            tflite = new Interpreter(loadModelFile());
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        try{
            this.tokens = new JSONArray(loadJSONFromAsset());
        } catch (JSONException e) {
            e.printStackTrace();
        }

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
        String[] tokens = this.text.split(" ");

        int[] tokens_id;

        for (int i = 0 ; i < tokens.length; i++) {
            Log.d("TOKEN", tokens[i]);
        }
    }

    public void onClick(View v) {
        // do something when the button is clicke
//        String text = this.editText.getText().toString();

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




}
