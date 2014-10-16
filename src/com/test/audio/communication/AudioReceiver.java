/**
 *AudioReceiver.java
 * Copyright2011 FreqencyGenerate
 */
package com.test.audio.communication;

import java.util.Vector;

import org.hermit.dsp.FFTTransformer;

import android.util.Log;


/**
 * @author albertma 
 * Sep 19, 2011
 */
public class AudioReceiver implements AndroidAudioReader.Listener, IAudioConstants
{
    static public interface IReceiverListener
    {
//        public void onReceivedFinished(int status);
        
        public void onReceived(String revContent, int status);
    }

    AndroidAudioReader audioReader;
    IReceiverListener revListener;
    FFTTransformer fftTransformer;
    float[] results;
    private int invalidFreqChunkCount;
    private boolean isGotStartFreq;
    private boolean isFinished;
    private Vector<Byte> halfByteResults;
    private byte currentHalfByte;
    private boolean isNewHalfByte;
    private boolean isGotData;
    private long timeOut;
    private long startTimeStamp;
    private int status;
    public AudioReceiver()
    {
        audioReader = new AndroidAudioReader();
        fftTransformer = new FFTTransformer(SIZE_PER_BIT);
        results = new float[SIZE_PER_BIT / 2];
        halfByteResults = new Vector<Byte>();
    }
    
    public void startReceive(IReceiverListener receiverListener, long timeOut)
    {
        this.timeOut = timeOut;
        this.status = 0;
        this.revListener = receiverListener;
        isFinished = false;
        isGotData = false;
        this.halfByteResults.removeAllElements();
        currentHalfByte = 0x00;
        audioReader.startReader(SAMPLE_FREQ, SIZE_PER_BIT, this);
        this.startTimeStamp = System.currentTimeMillis();
    }
    
    public void stopReceive()
    {
        this.revListener = null;
        audioReader.stopReader();
    }

    @Override
    public void onReadComplete(short[] buffer)
    {
        if(startTimeStamp > 0 && timeOut > 0)
        {
            if(System.currentTimeMillis() - startTimeStamp > timeOut)
            {
                this.revListener.onReceived("", -2);
                return;
            }
           
        }
        
        if(buffer != null)
        {
            handleRecordData(buffer);
        }
    }

    private void handleRecordData(short[] buffer)
    {
        fftTransformer.setInput(buffer, 0, buffer.length);
        fftTransformer.transform();
        fftTransformer.getResults(results);
        parseFrequencesData(results);
    }

    @Override
    public void onReadError(int error)
    {
        // TODO Auto-generated method stub
        
    }
    
    private void finishReceived()
    {
        Log.i("finishReceived", "Enter finishReceived");
        String resultConvert = convertHalfBytes2String(this.halfByteResults);
        Log.i("finishReceived", "Received Result:" + resultConvert);
        if(revListener != null)
        {
            this.revListener.onReceived(resultConvert, status);
        }
    }

    private void parseFrequencesData(float[] results)
    {
        if(isFinished)
        {
            return;
        }
        Log.i("parseFrequencesData", "start spectrum--------------");
        boolean hasValidUltraSound = false;
        boolean hasValidHalfByte = false;
        int enterCount = 0;
        int frequence = 0;
        float power = 0f;
        for(int i = 1; i < results.length; ++i)
        {
            power = (float) (Math.log10(results[i]) / 6.0 + 1f) * 311f;
            frequence = i * SAMPLE_FREQ / (2 * results.length);
            if (power > 200.0f && frequence > 16500)
            {
                Log.i("parseFrequencesData", "Freq:" + frequence);
                hasValidUltraSound = true;
//                Log.i("parseFrequencesData", "idleRuningCount:" + invalidFreqChunkCount + " isCheckedStartPoint:"
//                        + isGotStartFreq);

                if (isGotStartFreq)
                {
                    Log.i("parseFrequencesData", "start to Got UltraSound");
                    if (this.invalidFreqChunkCount > RECV_NEXT_HALF_BYTE_COUNT)
                    {
                        isNewHalfByte = true;
                    }
                    else
                    {
                        isNewHalfByte = false;
                    }

                    Log.i("parseFrequencesData", "isNewHalfByte:" + isNewHalfByte);

                    if (!isFinished)
                    {
                        if (isGotData)
                        {
                            isFinished = checkStartPoint(frequence);
                        }
                        Log.i("parseFrequencesData", "Got New HalfByte isFinished:" + isFinished);
                        if (!isFinished && enterCount < 1 && isNewHalfByte)
                        {
                            if(isGotData)
                            {
                                Log.i("parseFrequencesData", "---Add currentHalfByte:" + Integer.toBinaryString(currentHalfByte));
                                halfByteResults.add(Byte.valueOf(currentHalfByte));
                            }
                            currentHalfByte = 0x00;
                        }
                        else if (isFinished)
                        {
                            if(isGotData && enterCount < 1)
                            {
                                Log.i("parseFrequencesData", "---Add last currentHalfByte:" + Integer.toBinaryString(currentHalfByte));
                                halfByteResults.add(Byte.valueOf(currentHalfByte));
                            }
                            break;
                        }
                    }
                    else
                    {
                        break;
                    }
                    

                    // Log.i("parseFrequencesData", "idleRuningCount:" +
                    // invalidFreqChunkCount + " isCheckedStartPoint:"
                    // + isGotStartFreq + " isFinished:" + isFinished);

                    byte parsedResultBit = parseHalfByteBit(frequence);
                    Log.i("parseFrequencesData", "parsedResultBit" + Integer.toHexString(parsedResultBit));
                    if (parsedResultBit < 0x10)
                    {
                        hasValidHalfByte = true;
                        Log.i("parseFrequencesData", "Before currentHalfByte:" + Integer.toHexString(currentHalfByte)
                                + " parsedResultBit:" + Integer.toHexString(parsedResultBit));
                        currentHalfByte = (byte) (currentHalfByte | parsedResultBit);
                        isGotData = true;
//                        Log.i("parseFrequencesData", "After currentHalfByte:" + Integer.toHexString(currentHalfByte));
                    }
                    enterCount++;
                }
                else
                {
                    isGotStartFreq = checkStartPoint(frequence);
                    Log.i("parseFrequencesData", "checkStartPoint:" + isGotStartFreq);
                    if (isGotStartFreq)
                    {
                        break;
                    }

                }
            }

        }

        if(!hasValidUltraSound)
        {
            this.invalidFreqChunkCount ++;
        }
        else
        {
            invalidFreqChunkCount = 0;
            Log.i("parseFrequencesData", "hasValidHalfByte:" + hasValidHalfByte);
           
            Log.i("parseFrequencesData", "isFinished:" + isFinished);
            if (isFinished)
            {
                finishReceived();
            }
        }
        
    }
    
    private String convertHalfBytes2String(Vector<Byte> halfByteList)
    {
      
        int resultSize = halfByteList.size();
       
        byte[] resultBytes = new byte[resultSize / 2];
        Log.i("convertHalfBytes2String", "halfByteList.size:" + resultSize);    
        for(int i = 0; i < resultSize; i+=2)
        {
           byte lowHalfByte = 0x00;
           byte highHalfByte = 0x00;
            try
            {
                highHalfByte = (byte) (halfByteList.get(i + 1) & 0x0f);
                lowHalfByte = (byte) (halfByteList.get(i) & 0x0f);
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
                Log.i("convertHalfBytes2String", "The recv Data is incompliete");
                Log.i("convertHalfBytes2String", "lowHalfByte:" + lowHalfByte + " highHalfByte:" + highHalfByte);
                status = -1;
                break;
            }
           
           Log.i("convertHalfBytes2String", "lowHalfByte:" + lowHalfByte + " highHalfByte:" + highHalfByte);
           highHalfByte = (byte)(highHalfByte << 4);
           resultBytes[i/2] = (byte)(highHalfByte | lowHalfByte);
        }
        
        String resultString = new String(resultBytes);
       
        return resultString;
    }

    private byte parseHalfByteBit(int frequence)
    {
        int freq = parseSingleFrequence(frequence);
        byte parseHalfByte = 0x10;
        switch(freq)
        {
            case HALF_ZERO_FREQ/1000:
                parseHalfByte = 0x00;
                break;
            case BIT_0_FREQ/1000:
                parseHalfByte = 0x01;
                break;
            case BIT_1_FREQ/1000:
                parseHalfByte = 0x02;
                break;
            case BIT_2_FREQ/1000:
                parseHalfByte = 0x04;
                break;
            case BIT_3_FREQ/1000:
                parseHalfByte = 0x08;
                break;
            default:
                parseHalfByte = 0x10;
                break;
        }
        Log.i("parseHalfByte", "HalfByte:" + Integer.toHexString(parseHalfByte));
        return parseHalfByte;
        
    }

    private static int parseSingleFrequence(int frequence)
    {
        int tempFreqLevel = frequence / 1000;
        int frequenceTail = frequence % 1000;
        int frequenceLevel = 0;
        if(frequenceTail > 900)
        {
            frequenceLevel = tempFreqLevel + 1;
        }
        else if(frequenceTail < 100)
        {
            frequenceLevel = tempFreqLevel;
        }
//        Log.i("parseSingleFrequence", "Frequence level:" + frequenceLevel);
        return frequenceLevel;
    }
    
    private static boolean checkStartPoint(int frequence)
    {
        int freq = parseSingleFrequence(frequence);
        if(freq == (TRANSFER_START_END_FLAG_FREQ /1000))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    
}
