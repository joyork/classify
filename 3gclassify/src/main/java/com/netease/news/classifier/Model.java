package com.netease.news.classifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.netease.news.classifier.naivebayes.NaiveBayesModel;

public class Model {

	public Model(){}
	
	public Model(String naiveBayesModelFile,String dfcountFile,String dictionaryFile,
			String freqFile,String labelIndexFile){
		this.naiveBayesModelFile = naiveBayesModelFile;
		this.dfcountFile = dfcountFile;
		this.dictionaryFile = dictionaryFile;
		this.freqFile = freqFile;
		this.labelIndexFile = labelIndexFile;
	}

	private String naiveBayesModelFile = null;
	private String dfcountFile = null;
	private String dictionaryFile = null;
	private String freqFile = null;
	private String labelIndexFile = null;
	
	private NaiveBayesModel naiveBayesModel = null;
	private long featureCount = 0;
	private long vectorCount = 0;
	private HashMap<String, Integer> dictionary = null;
	private HashMap<Integer, String> inverseDic = null;
	private HashMap<Integer, Long> frequency = null;
	private Map<Integer, String> labelMap = null;
	
	public NaiveBayesModel getNaiveBayesModel() {
		return naiveBayesModel;
	}
	public long getFeatureCount() {
		return featureCount;
	}
	public long getVectorCount() {
		return vectorCount;
	}
	public HashMap<String, Integer> getDictionary() {
		return dictionary;
	}
	public HashMap<Integer, String> getInverseDic() {
		return inverseDic;
	}
	public HashMap<Integer, Long> getFrequency() {
		return frequency;
	}
	public Map<Integer, String> getLabelMap() {
		return labelMap;
	}

	
	public void setNaiveBayesModelFile(String naiveBayesModelFile) {
		this.naiveBayesModelFile = naiveBayesModelFile;
	}
	public void setDfcountFile(String dfcountFile) {
		this.dfcountFile = dfcountFile;
	}
	public void setDictionaryFile(String dictionaryFile) {
		this.dictionaryFile = dictionaryFile;
	}
	public void setFreqFile(String freqFile) {
		this.freqFile = freqFile;
	}
	public void setLabelIndexFile(String labelIndexFile) {
		this.labelIndexFile = labelIndexFile;
	}
	
	public void load(){
		loadNaiveBayesModel();
		System.out.println("NaiveBayesModel loaded."); 
		loadDfcount();
		System.out.println("Document Frequency loaded.");
		loadDictionary();
		System.out.println("Dictionary loaded.");
		loadFrequency();
		System.out.println("Term Frequency loaded.");
		loadLabelIndex();
		System.out.println("NaiveBayesModel loaded.");
	}
	
	private void loadNaiveBayesModel(){
		
		try {
			naiveBayesModel = NaiveBayesModel.materializeLocal(naiveBayesModelFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void loadDfcount(){
		if(dfcountFile == null)
			return;
		BufferedReader reader = null;
		try {
			ClassLoader loader = this.getClass().getClassLoader();
			InputStream in=loader.getResourceAsStream(dfcountFile);
			reader = new BufferedReader(new InputStreamReader(in,"UTF-8"));
			String line = null;
			while((line=reader.readLine())!=null){
				String[] seps = line.split(":");
				
				if(seps.length == 4){
			        int key = Integer.parseInt(seps[1].trim());
			        
			        if (key == -1) {
			          vectorCount = Integer.parseInt(seps[3].trim());
			        }
			        featureCount = Math.max(key, featureCount);
				}
			}
			featureCount++;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException ioe) {
				// ignore
			}
		}
	}
	
	private void loadDictionary(){
		if(dictionaryFile == null )
			return;
		dictionary = new HashMap<String,Integer>((int)this.featureCount);
		inverseDic = new HashMap<Integer,String>((int)this.featureCount);
		BufferedReader reader = null ;
		try {
			ClassLoader loader = this.getClass().getClassLoader();
			InputStream in=loader.getResourceAsStream(dictionaryFile);
			reader = new BufferedReader(new InputStreamReader(in,"UTF-8"));
			String line = null;
			while((line=reader.readLine())!=null){
				String[] seps = line.split(":");
				if(seps.length == 4){
			        String word = seps[1].trim();
			        int wordid = Integer.parseInt(seps[3].trim());
			        dictionary.put(word, wordid);
			        inverseDic.put(wordid, word);
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException ioe) {
				// ignore
			}
		}
	}
	
	
	private void loadFrequency(){
		if( freqFile == null)
			return;
		frequency = new HashMap<Integer, Long>((int)this.featureCount);
		InputStream in = null;
		BufferedReader reader = null;
		try {
			ClassLoader loader = this.getClass().getClassLoader();
			in=loader.getResourceAsStream(freqFile);
			reader = new BufferedReader(new InputStreamReader(in,"UTF-8"));
			String line = null;
			while((line=reader.readLine())!=null){
				String[] seps = line.split(":");
				if(seps.length == 4){
			        int wordid = Integer.parseInt(seps[1].trim());
			        long freq = Long.parseLong(seps[3].trim());
			        frequency.put(wordid, freq);
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException ioe) {
				// ignore
			}
		}
	}
	
	private void loadLabelIndex(){
		if( labelIndexFile == null)
			return;
		InputStream in = null;
		BufferedReader reader = null;
		labelMap = new HashMap<Integer,String>();
		try {
			ClassLoader loader = this.getClass().getClassLoader();
			in=loader.getResourceAsStream(labelIndexFile);
			reader = new BufferedReader(new InputStreamReader(in,"UTF-8"));
			String line = null;
			while((line=reader.readLine())!=null){
				String[] seps = line.split(":");
				if(seps.length == 4){
			        String label = seps[1].trim();
			        int labelid = Integer.parseInt(seps[3].trim());
			        labelMap.put( labelid,label);
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException ioe) {
				// ignore
			}
		}
	}
	
	public static void main(String[] args) {
		Model model = new Model();
		model.setNaiveBayesModelFile("model/naiveBayesModel.bin");
		model.setDfcountFile("deserial/cnews-dfcount.out");
		model.setDictionaryFile("deserial/cnews-dic.file-0.out");
		model.setFreqFile("deserial/cnews-freq.file-0.out");
		model.setLabelIndexFile("deserial/cnews-label.out"); 
		model.load();
		System.out.println("end"); 
		
	}
}
