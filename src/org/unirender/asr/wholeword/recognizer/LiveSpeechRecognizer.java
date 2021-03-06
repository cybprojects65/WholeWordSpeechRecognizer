package org.unirender.asr.wholeword.recognizer;

import java.io.File;
import java.util.List;

import org.it.cnr.asr.wholeword.audio.VAD;
import org.unirender.asr.wholeword.language.SupportedLanguages;


public class LiveSpeechRecognizer {

	String recordFile = "capturedaudio";
	SingleWordSpeechRecognizer recognizer;
	
	public LiveSpeechRecognizer (SupportedLanguages language,
			List<String> words, File modelsDirectory) throws Exception{
		recognizer = new SingleWordSpeechRecognizer(language,words,modelsDirectory);
	}
	
	public LiveSpeechRecognizer (SupportedLanguages language, File modelsDirectory)  throws Exception {
		recognizer = new SingleWordSpeechRecognizer(language,modelsDirectory);
	}
	
	public double getScore(){
		return recognizer.getBestScore();
	}
	public String listenAndRecognize() throws Exception{

		boolean caught= false;
		File waveFile = new File(recordFile + ".wav");
		VAD voiceActivityDetector = new VAD();
		try {
			caught = voiceActivityDetector.catchAudio(waveFile);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(">Error in audio capture");
			System.exit(0);
		}
		String recognizedWord = null;
		if (caught) {
			System.out.println("Audio caught");
			recognizedWord = recognizer.recognize(waveFile);
			System.out.println(">Recognized "+recognizedWord+" ("+recognizer.getBestScore()+")");
		}
		
		if (recordFile.equals("capturedaudio"))
			recordFile = "capturedaudio1";
		else
			recordFile = "capturedaudio";

		System.out.println();
		return recognizedWord;
	}
}
