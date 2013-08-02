package com.audioseparate;
import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jonathan Simon
 * Date: Mar 6, 2005
 * Time: 9:16:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class SingleWaveformPanel extends JPanel {
    /**
	 *
	 */
	private static final long serialVersionUID = -133514436822003255L;
	protected static final Color BACKGROUND_COLOR = Color.WHITE;
    protected static final Color REFERENCE_LINE_COLOR = Color.BLACK;
    protected static final Color WAVEFORM_COLOR = Color.RED;


    private AudioSep helper;
    private int channelIndex;

    public SingleWaveformPanel(AudioSep helper, int channelIndex) {
        this.helper = helper;
        this.channelIndex = channelIndex;
        setBackground(BACKGROUND_COLOR);
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int lineHeight = getHeight() / 2;
        g.setColor(REFERENCE_LINE_COLOR);
        g.drawLine(0, lineHeight, (int)getWidth(), lineHeight);

        drawWaveform(g, helper.getAudio(channelIndex));

    }

    protected void drawWaveform(Graphics g, int[] samples) {
        if (samples == null) {
            return;
        }

        int oldX = 0;
        int oldY = (int) (getHeight() / 2);
        int xIndex = 0;

        int increment = helper.getIncrement(helper.getXScaleFactor(getWidth()));
        g.setColor(WAVEFORM_COLOR);

        int t = 0;

        for (t = 0; t < increment; t += increment) {
            g.drawLine(oldX, oldY, xIndex, oldY);
            xIndex++;
            oldX = xIndex;
        }

        for (; t < samples.length; t += increment) {
            double scaleFactor = helper.getYScaleFactor(getHeight());
            double scaledSample = samples[t] * scaleFactor;
            int y = (int) ((getHeight() / 2) - (scaledSample));
            g.drawLine(oldX, oldY, xIndex, y);

            xIndex++;
            oldX = xIndex;
            oldY = y;
        }
    }
}
