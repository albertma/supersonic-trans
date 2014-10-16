package com.test.audio.communication;

public interface IAudioConstants
{
    public static int SIZE_PER_BIT = 1024;
    public static int SAMPLE_FREQ = 44100;
   
    
    public static final int BIT_0_FREQ = 19000;
    public static final int BIT_1_FREQ = 20000;
    public static final int BIT_2_FREQ = 21000;
    public static final int BIT_3_FREQ = 22000;
    public static final int HALF_ZERO_FREQ = 18000;
    
    public static final int TRANSFER_START_END_FLAG_FREQ = 17000;
    
    public static final int HALF_BYTE_TRANSFER_TIME = 500;
    /**
     * it need to tuning with {@code HALF_BYTE_TRANSFER_TIME} at same time;
     */
    public static final int RECV_NEXT_HALF_BYTE_COUNT = 7;
}
