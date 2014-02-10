package com.netease.news.classifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.mahout.classifier.ClassifierResult;
import org.apache.mahout.classifier.naivebayes.BayesUtils;
import org.apache.mahout.common.Pair;
import org.apache.mahout.common.StringTuple;
import org.apache.mahout.common.iterator.sequencefile.PathType;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileDirIterable;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileIterable;
import org.apache.mahout.common.lucene.AnalyzerUtils;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.Vector.Element;
import org.apache.mahout.vectorizer.TFIDF;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.google.common.io.Closeables;
import com.netease.news.classifier.naivebayes.AbstractNaiveBayesClassifier;
import com.netease.news.classifier.naivebayes.ComplementaryNaiveBayesClassifier;
import com.netease.news.classifier.naivebayes.NaiveBayesModel;

public class NewsClassifier {

	private boolean dictRebuilt = false;
	private HashMap<String, Integer> dictionary = new HashMap<String, Integer>();
	private HashMap<Integer, String> inverseDic = new HashMap<Integer, String>();
	private HashMap<Integer, Long> frequency = new HashMap<Integer, Long>();
	private long FEATURE_COUNT = 0;
	private long VECTOR_COUNT = 0;
//	private String DIR_PATH = "WEB-INF/classes/";
	private String DIR_PATH = "/tmp/classify/";
	
	public void classifyDocument(String filename){
		String content = null;
		BufferedReader reader = null;
		try {
			ClassLoader loader = this.getClass().getClassLoader();
			InputStream in=loader.getResourceAsStream(filename);
			StringBuffer sb = new StringBuffer();
			reader = new BufferedReader(new InputStreamReader(in,"UTF-8"));
			String line = null;
			while((line=reader.readLine())!=null){
				sb.append(line);
			}
			content = sb.toString();
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
		Map<String,Object> result = this.classify(content);
		if(result!=null){
			System.out.println(result.get("label")+" "+result.get("score"));

			List<Map.Entry<String,Double>> wordlist = (List<Map.Entry<String,Double>>)result.get("wordScoreList");
			for(Map.Entry<String,Double> entry : wordlist){
				System.out.println(entry.getKey()+" "+entry.getValue()); 
			}
		}else{
			System.out.println("No result."); 
		}
	}
	
	private List<Map.Entry<String,Double>> insertSortEntry(Map<String,Double> tosort){
		List<Map.Entry<String, Double>> sorted = new LinkedList<Map.Entry<String,Double>>();
		for(Entry<String,Double> entry : tosort.entrySet()){
			int size = sorted.size();
			int cur = 0;
			while(true){
				if(cur<size){
					Entry<String,Double> target = sorted.get(cur);
					if(target.getValue()>entry.getValue()){
						cur++;
						continue;
					}else{
						sorted.add(cur,entry);
						break;
					}
				}else{
					sorted.add(entry);
					break;
				}
				
			}
			
		}
		return sorted;
	}
	
	public Map<String,Object> classify(String text) {
		Configuration conf = new Configuration();
		AbstractNaiveBayesClassifier classifier;
		Map<String,Object> result = new HashMap<String,Object>() ;
		try {
			classifier = loadClassifier(conf);
			Vector tfVector = generateTFVector(text);
			Vector tfidfVector = generateTFIDFVector(tfVector);
			Map<String,Double> wordScoreMap = new HashMap<String,Double>();
//			Map<String,Map<String,Integer>> smap = new TreeMap<String,Map<String,Integer>>();
		    for (Element e : tfidfVector.nonZeroes()) {
		       wordScoreMap.put(inverseDic.get(e.index()), e.get());
		    }
			Vector r = classifier.classifyFull(tfidfVector);
			Path labelIndexPath = new Path(DIR_PATH+"labelIndex");
			Map<Integer, String> labelMap = BayesUtils.readLabelIndex(conf,
					labelIndexPath);
			int bestIdx = Integer.MIN_VALUE;
			double bestScore = Long.MIN_VALUE;
			HashMap<String, Double> resultMap = new HashMap<String, Double>();
			for (int i = 0; i < labelMap.size(); i++) {
				Vector.Element element = r.getElement(i);
				resultMap.put(labelMap.get(element.index()), element.get());
				if (element.get() > bestScore) {
					bestScore = element.get();
					bestIdx = element.index();
				}
			}
			List<Map.Entry<String,Double>> wordScoreList = insertSortEntry(wordScoreMap);
			List<String> topWords = new ArrayList<String>();
			for(Map.Entry<String,Double> entry : wordScoreList){
				topWords.add(entry.getKey());
				if(topWords.size()>100) {
					break;
				}
			}
			if (bestIdx != Integer.MIN_VALUE) {
				result.put("label", labelMap.get(bestIdx));
				result.put("score", bestScore);
				result.put("wordScore", wordScoreMap);
				result.put("wordScoreList", wordScoreList);
				result.put("topWords", topWords);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}


	private AbstractNaiveBayesClassifier loadClassifier(Configuration conf)
			throws IOException {
		NaiveBayesModel model = NaiveBayesModel.materialize(new Path(DIR_PATH+"model"),conf);
		AbstractNaiveBayesClassifier classifier = new ComplementaryNaiveBayesClassifier(
				model);
		return classifier;
	}

	private void reBuildDictionary() {
		if (dictRebuilt)
			return;
		
		Configuration conf = new Configuration();
		Path dfcountfile = new Path(DIR_PATH+"df-part");
	      long featureCount = 0;
	      long vectorCount = Long.MAX_VALUE;
//	      new SequenceFileDirIterable<IntWritable,LongWritable>(dfcountfile,PathType.GLOB,null,null,true,conf);
	      for (Pair<IntWritable,LongWritable> record
	           : new SequenceFileDirIterable<IntWritable,LongWritable>(dfcountfile,
	                                                                   PathType.GLOB,
	                                                                   null,
	                                                                   null,
	                                                                   true,
	                                                                   conf)) {
	        IntWritable key = record.getFirst();
	        LongWritable value = record.getSecond();
	        if (key.get() == -1) {
	          vectorCount = value.get();
	        }
	        featureCount = Math.max(key.get(), featureCount);

	      }
	      featureCount++;
	      FEATURE_COUNT = featureCount;
	      VECTOR_COUNT = vectorCount;
		Path dictionaryFile = new Path(DIR_PATH+"dictionary.file-0");
		// key is feature, value is the document frequency
		for (Pair<Text, IntWritable> record : new SequenceFileIterable<Text, IntWritable>(
				dictionaryFile, true, conf)) {
			dictionary.put(record.getFirst().toString(), record.getSecond()
					.get());
			inverseDic.put(record.getSecond().get(), record.getFirst().toString());
		}
		Path freqFile = new Path(DIR_PATH+"frequency.file-0");
		// key is feature, value is the document frequency
		for (Pair<IntWritable, LongWritable> record : new SequenceFileIterable<IntWritable, LongWritable>(
				freqFile, true, conf)) {
			frequency.put(record.getFirst().get(), record.getSecond().get());
		}

		dictRebuilt = true;
	}
	
	
	private StringTuple extractToken(String text) {
		Analyzer analyzer = AnalyzerUtils.createAnalyzer(IKAnalyzer.class);
		TokenStream stream;
		StringTuple document = new StringTuple();
		try {
			stream = analyzer.tokenStream("", new StringReader(text));
			stream.reset();
			CharTermAttribute termAtt = stream
					.addAttribute(CharTermAttribute.class);
			stream.reset();
			while (stream.incrementToken()) {
				if (termAtt.length() > 0) {
					document.add(new String(termAtt.buffer(), 0, termAtt
							.length()));
				}
			}
			stream.end();
			Closeables.close(stream, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return document;
	}


	private Vector generateTFVector(String text) {

		reBuildDictionary();

		Vector tfvector = new RandomAccessSparseVector((int)FEATURE_COUNT);

		StringTuple tokendoc = extractToken(text);
		System.out.println(tokendoc);
		for (String term : tokendoc.getEntries()) {
			if (!term.isEmpty() && dictionary.containsKey(term)) { // unigram
				int termId = dictionary.get(term);
				tfvector.setQuick(termId, tfvector.getQuick(termId) + 1);
			}
		}
		return tfvector;
	}

	private Vector generateTFIDFVector(Vector tfVector) {
		int minDf = 1;
		long maxDf = -1;
		// int maxDFPercent = 99;
		// double maxDFSigma = -1.0;
		// boolean shouldPrune = maxDFSigma >= 0.0 || maxDFPercent > 0.00;

		long vectorCount = VECTOR_COUNT;
		long featureCount = FEATURE_COUNT;
		TFIDF tfidf = new TFIDF();
		Vector tfidfVector = new RandomAccessSparseVector((int)FEATURE_COUNT,
				tfVector.getNumNondefaultElements());
		for (Vector.Element e : tfVector.nonZeroes()) {
			if (!frequency.containsKey(e.index())) {
				continue;
			}
			long df = frequency.get(e.index());
			if (maxDf > -1 && (100.0 * df) / vectorCount > maxDf) {
				continue;
			}
			if (df < minDf) {
				df = minDf;
			}
			tfidfVector.setQuick(e.index(), tfidf.calculate((int) e.get(),
					(int) df, (int) featureCount, (int) vectorCount));
		}
		return tfidfVector;
	}


	public static void main(String[] args) {

		String file = "toclassify.txt";
		NewsClassifier classifier = new NewsClassifier();
		classifier.classifyDocument(file);
	}
}