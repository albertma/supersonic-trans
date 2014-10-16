package com.test.audio.communication;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class AndroidAudioSender
{
    AudioTrack track;
    short[] buffer = new short[1024];
  
    public AndroidAudioSender()
    {
       int minSize =AudioTrack.getMinBufferSize( 44100, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT );        
       track = new AudioTrack( AudioManager.STREAM_MUSIC, 44100, 
                                         AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, 
                                         minSize, AudioTrack.MODE_STREAM);
       track.play();
    }       
  
    public void writeAudioCodes(float[] codes) 
    {    
       fillBuffer( codes );
       track.write( buffer, 0, codes.length );
     //  track.flush();
//       track.play();
    }
    
    public void close()
    {
        if(track != null)
        {
            track.stop();
        }
    }
  
    private void fillBuffer( float[] samples )
    {
       if( buffer.length < samples.length )
          buffer = new short[samples.length];
  
       for( int i = 0; i < samples.length; i++ )
          buffer[i] = (short)(samples[i] * Short.MAX_VALUE);;
    }
    public void writeSamples(byte[] samples)
    {
        if(samples != null)
        {
            track.write(samples, 0, samples.length);
        }
    }
}
