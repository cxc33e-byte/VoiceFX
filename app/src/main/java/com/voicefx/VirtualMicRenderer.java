package com.voicefx;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class VirtualMicRenderer {

    public static final int SAMPLE_RATE = 48000;
    public static final int CHANNELS = 2;
    public static final int BYTES_PER_SAMPLE = 2;

    public static final int RING_BUFFER_SIZE = 16384;

    private final ByteBuffer ringBuffer;
    private int writePos = 0;

    public VirtualMicRenderer() {
        ringBuffer = ByteBuffer
                .allocate(RING_BUFFER_SIZE)
                .order(ByteOrder.LITTLE_ENDIAN);
    }

    public synchronized int write(short[] monoSamples, int length) {

        if (monoSamples == null || length <= 0) {
            return 0;
        }

        int written = 0;

        for (int i = 0; i < length; i++) {

            short sample = monoSamples[i];

            // Mono → Stereo
            if (writePos + 4 > RING_BUFFER_SIZE) {
                writePos = 0;
            }

            ringBuffer.putShort(writePos, sample);
            ringBuffer.putShort(writePos + 2, sample);

            writePos += 4;
            written++;
        }

        return written;
    }

    public synchronized byte[] getSnapshot() {

        byte[] data = new byte[RING_BUFFER_SIZE];

        for (int i = 0; i < RING_BUFFER_SIZE; i++) {
            data[i] = ringBuffer.get(i);
        }

        return data;
    }

    public int getSampleRate() {
        return SAMPLE_RATE;
    }

    public int getChannels() {
        return CHANNELS;
    }

    public int getBytesPerSample() {
        return BYTES_PER_SAMPLE;
    }
}
