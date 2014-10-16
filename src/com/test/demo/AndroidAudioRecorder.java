/**
 * AudioRecorder.java
 * Copyright2011 FreqencyGenerate
 */
package com.test.demo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.Environment;

/**
 * @author albertma 
 * Sep 12, 2011
 */

public class AndroidAudioRecorder extends Thread
{
    private boolean stopped = false;

    public AndroidAudioRecorder()
    {
      
    }
    
    public void startRecording()
    {
        start();
        stopped = false;
    }

    @Override
    public void run()
    {
        AudioRecord recorder = null;
      
        FileOutputStream fos = null;
        try
        { // ... initialise
            File file = new File(Environment.getExternalStorageDirectory() + "/recorder");
            if(file.exists())
            {
                file.delete();
            }
            file.createNewFile();
            fos = new FileOutputStream(file);
            int N = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            recorder = new AudioRecord(AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, N);
            
            recorder.startRecording();

            // ... loop

            while (!stopped)
            {
                byte[] buffer = new byte[1024];

                N = recorder.read(buffer, 0, buffer.length);

//                fos.write((byte[])buffer);
                process(buffer, fos);
            }
        }
        catch (Throwable x)
        {
            x.printStackTrace();
        }
        finally
        {
            if (recorder != null)
            {
                close(recorder);
            }
            if (fos != null)
            {
                try
                {
                    fos.flush();
                    fos.close();
                }
                catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    private void close(AudioRecord recorder)
    {
        if(recorder != null)
        {
            recorder.stop();
        }
    }

    private void process(byte[] buffer, OutputStream os)
    {
        if(buffer != null)
        {
           try
        {
            os.write(buffer);
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        }
    }

    public void stopRecording()
    {
        stopped = true;
    }

}
