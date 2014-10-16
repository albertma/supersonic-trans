package com.test.demo;


import com.test.audio.communication.AndroidAudioSender;
import com.test.audio.communication.AudioReceiver;
import com.test.audio.communication.AudioReceiver.IReceiverListener;
import com.test.audio.communication.AudioSpeaker;
import com.test.audio.communication.AudioSpeaker.IAudioSpeakCallBack;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class FreqencyGenerateActivity extends Activity implements IReceiverListener, IAudioSpeakCallBack {
    AudioReceiver audioReceiver = new AudioReceiver();
    AudioSpeaker audioSpeaker = new AudioSpeaker();
    EditText mEditText;
    Button mSendButton;
//    FFT fft = new FFT(1024, 44100);
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mEditText = (EditText)findViewById(R.id.editText1);
        mSendButton = (Button)findViewById(R.id.send_button);
        mEditText.addTextChangedListener(new TextWatcher()
        {
            private CharSequence temp;
            private int editStart;
            private int editEnd;

            @Override
            public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3)
            {
                temp = s;
            }

            @Override
            public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                editStart = mEditText.getSelectionStart();
                editEnd = mEditText.getSelectionEnd();
                if (temp.length() > 3)
                {
                    Toast.makeText(FreqencyGenerateActivity.this, "你输入的字数已经超过了限制！", Toast.LENGTH_SHORT).show();
                    s.delete(editStart - 1, editEnd);
                    int tempSelection = editStart;
                    mEditText.setText(s);
                    mEditText.setSelection(tempSelection);
                }
                if (s.length() > 0)
                {
                    mSendButton.setEnabled(true);
                }
            }
        });
    }
    
    public void onRecordData(View view)
    {
        audioReceiver.startReceive(this, 20000);
        
        changeButtonsState(true);
        TextView textView = (TextView)findViewById(R.id.curr_audio_freq);
        textView.setText("Recording text...");
    }
    
    public void onStopAction(View view)
    {
        if(audioReceiver != null)
        {
            audioReceiver.stopReceive();
        }
        changeButtonsState(false);
    }
    
    public void onSendData(View view)
    {
        final String inputCode = mEditText.getEditableText().toString().toString();
        if (inputCode != null && inputCode.trim().length() > 0)
        {
            new Thread(new Runnable()
            {

                @Override
                public void run()
                {

                    AndroidAudioSender audioSender = new AndroidAudioSender();
                    audioSpeaker.speak(inputCode.getBytes(), audioSender, FreqencyGenerateActivity.this);
                    audioSender.close();
                }

            }).start();
            changeButtonsState(true);
            TextView textView = (TextView)findViewById(R.id.curr_audio_freq);
            textView.setText("Sending text...");
        }
        else
        {
            Toast.makeText(this, "Invalid input!", 3000).show();
        }
    }


    @Override
    public void onReceived(final String recvContent,  final int status)
    {
        this.runOnUiThread(new Runnable()
        {
            
            @Override
            public void run()
            {
                TextView textView = (TextView)findViewById(R.id.curr_audio_freq);
                String content = "";
                switch(status)
                {
                    case 0:
                        content = "Current Received text:" + recvContent;
                        break;
                    case -1:
                        content = "Incomplieted Received text:" + recvContent;
                        break;
                    case -2:
                        content = "Time out";
                        break;
                    default:
                        content = "Unknown err";
                }
                textView.setText(content);
                changeButtonsState(false);
                if(audioReceiver != null)
                {
                    audioReceiver.stopReceive();
                }
            }
        });
        
    }
    
    private void changeButtonsState(final boolean isBusy)
    {
        this.runOnUiThread(new Runnable()
        {
            
            @Override
            public void run()
            {
               
                findViewById(R.id.listen_button).setEnabled(!isBusy);
                findViewById(R.id.send_button).setEnabled(!isBusy);
                findViewById(R.id.stop_button).setEnabled(isBusy);
                
            }
        });
        

    }
    
    protected void onPause()
    {
        super.onPause();
        onStopAction(findViewById(R.id.stop_button));
    }

    @Override
    public void onAudioSpeakFinish(int state)
    {
        changeButtonsState(false);
    }
}