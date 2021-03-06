package org.unirender.asr.wholeword.examples;

import java.io.File;

import org.unirender.asr.wholeword.language.SupportedLanguages;
import org.unirender.asr.wholeword.recognizer.SingleWordSpeechRecognizer;

public class RecognizeRecordedAudio {

	public static void main(String[] args) throws Exception {
		SingleWordSpeechRecognizer asr = new SingleWordSpeechRecognizer(
				SupportedLanguages.IT,new File ("./MODELS/"));
		System.out.println("ASR initialised.");
		System.out.println("N. of loaded models:" + asr.getHmmsList().size());

		File[] wavelist = new File("./").listFiles();

		for (int i = 0; i < wavelist.length; i++) {
			if (wavelist[i].getName().endsWith(".wav")) {
				asr.recognize(wavelist[i]);
				System.out.print(wavelist[i]+"->"+asr.getBestWord()+" ("+asr.getBestScore()+")");
				if (!wavelist[i].getName().replace(".wav", "").equalsIgnoreCase(asr.getBestWord()))
					System.out.println("*");
				else
					System.out.println("");
			}
		}
	}

}
