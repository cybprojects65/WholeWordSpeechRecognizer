package org.unirender.asr.wholeword.recognizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.sound.sampled.AudioFormat;

import org.it.cnr.asr.wholeword.audio.AudioBits;
import org.it.cnr.asr.wholeword.audio.AudioProcessing;
import org.it.cnr.asr.wholeword.audio.MfccExtraction;
import org.unirender.asr.wholeword.language.ILanguageModel;
import org.unirender.asr.wholeword.language.LanguageModelGM;
import org.unirender.asr.wholeword.language.SupportedLanguages;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationVector;

public class SingleWordSpeechRecognizer {

	private ILanguageModel lm;
	private double bestScore = -Double.MAX_VALUE;
	private String bestWord = "";
	private LinkedList<Hmm<ObservationVector>> hmmsList;
	private LinkedList<String> wordsToRecognize;
	private File modelsFolder ;
	public SingleWordSpeechRecognizer(SupportedLanguages language,
			List<String> words, File modelsFolder)
			throws Exception {
		this.modelsFolder = modelsFolder;
		init(language, words);
	}

	public SingleWordSpeechRecognizer(SupportedLanguages language, File modelsFolder)
			throws Exception {
		this(language, null, modelsFolder);
	}

	@SuppressWarnings("unchecked")
	public void init(SupportedLanguages language, List<String> words) throws Exception {

		hmmsList = new LinkedList<Hmm<ObservationVector>>();

		//String resource = "MODELS/" + language.name();
		File resource = new File(modelsFolder, language.name());
		
		if (language == SupportedLanguages.IT)
			lm = new LanguageModelGM();

		wordsToRecognize = new LinkedList<String>();

		//URL in = ClassLoader.getSystemClassLoader().getResource(resource);
		
		//File[] files = new File(in.toURI()).listFiles();
		File[] files = resource.listFiles();
		System.out.println("All models in the path:" + Arrays.asList(files));
		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			String hmmPath = f.getAbsolutePath();
			String hmmName = f.getName().substring(0,
					f.getName().lastIndexOf("."));

			if (words == null || words.contains(hmmName)) {

				ObjectInputStream ois = new ObjectInputStream(
						new FileInputStream(new File(hmmPath)));
				Object hmmobs = ois.readObject();
				Hmm<ObservationVector> hmm = null;
				if (hmmobs instanceof Hmm)
					hmm = (Hmm<ObservationVector>) hmmobs;
				ois.close();
				wordsToRecognize.add(hmmName);
				hmmsList.add(hmm);
			}
		}

	}

	/**
	 * Recognize a sequence of numeric observations
	 * 
	 * @param X
	 * @return
	 */
	public String recognizeObservations(double[][] X) {

		int bestindex = 0;
		double bestvalue = -Double.MAX_VALUE;
		int index = 0;

		List<ObservationVector> oseq = AudioProcessing.selectObservations(0,
				X.length - 1, X);
		for (Hmm<ObservationVector> hmm : hmmsList) {
			
			// apply Viterbi
			double like = hmm.lnProbability(oseq);

			String hname = wordsToRecognize.get(index);
			

			// apply language model
			like = lm.singleWordLanguageModel(hname, like);
			//System.out.println("Like of " + hname + " : " + like);
			//System.out.println("Post Like of " + hname + " : " + like);
			
			if (like > bestvalue) {
				bestindex = index;
				bestvalue = like;
			}
			index++;
		}

		bestWord = wordsToRecognize.get(bestindex);
		bestScore = bestvalue;

		return bestWord;
	}

	/**
	 * Recognizes an audio file: transforms from audio to string
	 */
	public String recognize(File audiofile) throws Exception {
		AudioBits audio = new AudioBits(audiofile);
		short[] shortaudio = audio.getShortVectorAudio();
		AudioFormat af = audio.getAudioFormat();
		String supportedAudioFormat = "PCM_SIGNED 8000.0 Hz, 16 bit, mono, 2 bytes/frame, little-endian";
		
		//System.out.println("AudioFormat: " + (""+af));
		//System.out.println("Supported AudioFormat" + supportedAudioFormat);
		
		if (!supportedAudioFormat.equals(""+af))
			throw new Exception("Audio format not supported");

		float sf = af.getSampleRate();
		//System.out.println("Sample Rate: " + sf);
		audio.deallocateAudio();

		// framesize - frame corresponding to 5 ms
		int frameSize = (int) (sf * 0.005);
		shortaudio = AudioProcessing.trim(shortaudio, frameSize);

		// features matrix
		double X[][] = null;
		MfccExtraction mfcc = new MfccExtraction(sf);

		X = mfcc.extractMFCC(shortaudio);

		return recognizeObservations(X);

	}

	public ILanguageModel getLm() {
		return lm;
	}

	public void setLm(ILanguageModel lm) {
		this.lm = lm;
	}


	public double getBestScore() {
		return bestScore;
	}

	public void setBestScore(double bestScore) {
		this.bestScore = bestScore;
	}

	public String getBestWord() {
		return bestWord;
	}

	public void setBestWord(String bestWord) {
		this.bestWord = bestWord;
	}

	public LinkedList<Hmm<ObservationVector>> getHmmsList() {
		return hmmsList;
	}

	public void setHmmsList(LinkedList<Hmm<ObservationVector>> hmmsList) {
		this.hmmsList = hmmsList;
	}

	public LinkedList<String> getWordsToRecognize() {
		return wordsToRecognize;
	}

	public void setWordsToRecognize(LinkedList<String> words) {
		this.wordsToRecognize = words;
	}

}
