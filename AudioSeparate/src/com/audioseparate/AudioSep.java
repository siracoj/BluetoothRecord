package com.audioseparate;
import javax.sound.sampled.AudioInputStream;


public class AudioSep {
    private static final int NUM_BITS_PER_BYTE = 8;

    private AudioInputStream audioInputStream;
    private int[][] samplesContainer;

    //cached values
    protected int sampleMax = 0;
    protected int sampleMin = 0;
    protected double biggestSample;

    public AudioSep(AudioInputStream aiStream) {
        this.audioInputStream = aiStream;
        createSampleArrayCollection();
    }

    public int getNumberOfChannels(){
        int numBytesPerSample = audioInputStream.getFormat().getSampleSizeInBits() / NUM_BITS_PER_BYTE;
        return audioInputStream.getFormat().getFrameSize() / numBytesPerSample;
    }

    private void createSampleArrayCollection() {
        try {
            audioInputStream.mark(Integer.MAX_VALUE);
            //audioInputStream.reset();
            byte[] bytes = new byte[(int) (audioInputStream.getFrameLength()) * ((int) audioInputStream.getFormat().getFrameSize())];
            @SuppressWarnings("unused")
			int result = 0;
            try {
                result = audioInputStream.read(bytes);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //convert sample bytes to channel separated 16 bit samples
            samplesContainer = getSampleArray(bytes);

            //find biggest sample. used for interpolating the yScaleFactor
            if (sampleMax > sampleMin) {
                biggestSample = sampleMax;
            } else {
                biggestSample = Math.abs(((double) sampleMin));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected int[][] getSampleArray(byte[] eightBitByteArray) {
        int[][] toReturn = new int[getNumberOfChannels()][eightBitByteArray.length / (2 * getNumberOfChannels())];
        int index = 0;
        int size = eightBitByteArray.length;
        //loop through the byte[]
        for (int t = 0; t < size;t++) {
            //for each iteration, loop through the channels
        	int v = t+1;
        	
        	if(v < size){
        		for (int a = 0; a < getNumberOfChannels(); a++) {
        			//do the byte to sample conversion
        			//see AmplitudeEditor for more info
        			int low = (int) eightBitByteArray[t];
                
        			int high = (int) eightBitByteArray[v];
                
        			int sample = (high << 8) + (low & 0x00ff);

        			if (sample < sampleMin) {
        				sampleMin = sample;
        			} else if (sample > sampleMax) {
        				sampleMax = sample;
        			}
        			//set the value.
        			toReturn[a][index] = sample;
        		}
        		index++;
        		t = v;
        	}
        }

        return toReturn;
    }

    public double getXScaleFactor(int panelWidth){
        return (panelWidth / ((double) samplesContainer[0].length));
    }

    public double getYScaleFactor(int panelHeight){
        return (panelHeight / (biggestSample * 2 * 1.2));
    }

    public int[] getAudio(int channel){
        return samplesContainer[channel];
    }
    public int[][] getSampleContainer(){
    	return samplesContainer;
    }

    protected int getIncrement(double xScale) {
        try {
            int increment = (int) (samplesContainer[0].length / (samplesContainer[0].length * xScale));
            return increment;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

}
