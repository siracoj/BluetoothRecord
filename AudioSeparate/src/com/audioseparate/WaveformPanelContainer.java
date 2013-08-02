package com.audioseparate;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.sound.sampled.AudioInputStream;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class WaveformPanelContainer extends JPanel {    
	/**
	 *
	 */
	private static final long serialVersionUID = -275269905102149949L;
	private ArrayList<SingleWaveformPanel> singleChannelWaveformPanels = new ArrayList<SingleWaveformPanel>();
	private AudioSep audioSep = null;
   
	public WaveformPanelContainer() {
		setLayout(new GridLayout(0,1));
	}

	public void setAudioToDisplay(AudioInputStream audioInputStream){
		singleChannelWaveformPanels = new ArrayList<SingleWaveformPanel>();
		audioSep = new AudioSep(audioInputStream);
		for (int t=0; t<audioSep.getNumberOfChannels(); t++){
			SingleWaveformPanel waveformPanel
				= new SingleWaveformPanel(audioSep, t);
			singleChannelWaveformPanels.add(waveformPanel);
			add(createChannelDisplay(waveformPanel, t));
		}
	}
	private JComponent createChannelDisplay(
			SingleWaveformPanel waveformPanel,
			int index) {

       JPanel panel = new JPanel(new BorderLayout());
	   panel.add(waveformPanel, BorderLayout.CENTER);

	   JLabel label = new JLabel("Channel " + ++index);
	   panel.add(label, BorderLayout.NORTH);

	   return panel;
	}
}
