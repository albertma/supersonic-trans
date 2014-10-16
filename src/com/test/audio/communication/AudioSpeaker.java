/**
 * AudioXEncode.java
 * Copyright2011 FreqencyGenerate
 */
package com.test.audio.communication;

import java.io.WriteAbortedException;
import java.util.Vector;

import android.util.Log;


/**
 * @author albertma 
 * Sep 19, 2011
 */
public class AudioSpeaker implements IAudioConstants
{
    
    public interface IAudioSpeakCallBack
    {
        public void onAudioSpeakFinish(int state);
    }
    
    private static final int MAX_ACCEPT_SIZE = 70;
    private static float[] staticSeperatorFreqCode;
    private static float[] contentData = new float[1024]; 
    private static float[] nullData = new float[1024];
    private static Vector<Integer> currentFreqList = new Vector<Integer>();
    
    public void speak(byte[] content, AndroidAudioSender audioSender, IAudioSpeakCallBack audioSpeakCallBack)
    {
        if(content != null)
        {
            if(content.length > MAX_ACCEPT_SIZE)
            {
                throw new RuntimeException("overfloat the maxmium size");
            }
            else
            {
                int time = 1;
                for(int index = 0; index < time; index++)
                {

                    writeFreqAudioData(generateSeperatorCodes(), audioSender);
                    for (int i = 0; i < content.length; i++)
                    {
                        writeByteAudioData(content[i], contentData, audioSender);
                    }
                    if(index == time -1)
                    {
                        writeFreqAudioData(generateSeperatorCodes(), audioSender);
                    }

                }
                audioSpeakCallBack.onAudioSpeakFinish(0);
            }
        }

    }
    
    protected static float[] generateSeperatorCodes()
    {
        if (staticSeperatorFreqCode == null)
        {
            staticSeperatorFreqCode = new float[1024];
            float angle = (float)Math.PI/2.0f;
            float increment = (float)(2*Math.PI) * TRANSFER_START_END_FLAG_FREQ/ 44100;
            for (int i = 0; i < 1024; i++)
            {
                staticSeperatorFreqCode[i] = (float)Math.sin(angle);
                angle += increment;
            }
        }
        return staticSeperatorFreqCode;
      
    }
    
    protected static void writeFreqAudioData(float[] freq ,  AndroidAudioSender audioSender)
    {
        audioSender.writeAudioCodes(freq);
        for (int j = 0; j < 15; j++)
        {
            audioSender.writeAudioCodes(nullData);

        }
    }
    
    protected static void writeByteAudioData(byte byteContent, float[] freqData, AndroidAudioSender audioSender)
    {
        if(freqData == null || freqData.length == 0)
        {
            throw new RuntimeException("Argument freqData should not be null or length 0");
        }
        float angle = (float)Math.PI/2.0f;
        
        for (int partIndex = 0; partIndex < 2; partIndex++)
        {
            currentFreqList.removeAllElements();
            int lowPartValue = (byteContent>>(partIndex * 4)) & 0x0f;
            if (lowPartValue > 0)
            {
                int magic_mask = 0x01;

                
                for (int i = 0; i < 4; i++)
                {
                    if ((lowPartValue & (magic_mask << i)) > 0)
                    {
                        int currentFreq = 0;
                        switch (i)
                        {
                            case 0:
                                currentFreq = BIT_0_FREQ;
                                break;
                            case 1:
                                currentFreq = BIT_1_FREQ;
                                break;
                            case 2:
                                currentFreq = BIT_2_FREQ;
                                break;
                            case 3:
                                currentFreq = BIT_3_FREQ;
                                break;
                        }
                        if (currentFreq == 0)
                        {
                            new RuntimeException("Something wrong, pls check your code.");
                        }
                        currentFreqList.add(Integer.valueOf(currentFreq));
                    }
                }
                float increment = 0f;
                int freqNum = currentFreqList.size();
                int threholdValue = freqData.length / freqNum;
                Log.i("writeByteAudioData", "freqNum = " + freqNum + " freqData.length=" + freqData.length + " threholdValue =" + threholdValue);
                int freqIndex = 0;
                int lastFreqIndex = 0;
                for (int index = 0; index < freqData.length; index++)
                {
                    freqIndex = index / threholdValue;
                    if(lastFreqIndex != freqIndex)
                    {
                        lastFreqIndex = freqIndex;
                        angle = (float)Math.PI/2.0f;
                    }
//                    Log.i("writeByteAudioData", "freqIndex = " + freqIndex);
                    if(freqIndex >= freqNum)
                    {
                        freqIndex = freqNum -1;
                    }
                    increment = (float) (2 * Math.PI) * currentFreqList.get(freqIndex) / 44100;
                    freqData[index] = (float) Math.sin(angle);
                    angle += increment;
                }
                writeFreqAudioData(freqData, audioSender);
            }
            else
            {
                float increment = (float) (2 * Math.PI) * HALF_ZERO_FREQ / 44100;
                for (int i = 0; i < freqData.length; i++)
                {
                    freqData[i] = (float) Math.sin(angle);
                    angle += increment;
                }
                writeFreqAudioData(freqData, audioSender);
            }
        }
        
    }
    
}
