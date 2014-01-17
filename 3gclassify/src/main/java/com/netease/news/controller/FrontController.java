package com.netease.news.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.mahout.classifier.ClassifierResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.netease.news.classifier.NewsClassifier;
import com.netease.news.utils.PropertyLoader;

@Controller
@RequestMapping("/")
public class FrontController {
	
	private static PropertyLoader labelMap = new PropertyLoader("labelmap.properties");

	private Logger logger = Logger.getLogger(this.getClass());
	
	@RequestMapping(value = "test")
	public ModelAndView test(@RequestParam(value = "content", required = false) String content) {
		ModelAndView mv = new ModelAndView("test");
		String info = "failed";
		NewsClassifier classifier = new NewsClassifier();
		System.out.println("c:"+content); 
		Map<String,Object> result = classifier.classify(content);
		if(result!=null){
			info = result.get("label")+" "+result.get("score"); 
		}else{
			info = "No result" ;
		}
		System.out.println("info:"+info);
//		Map<String, Object> map = new HashMap<String, Object>();
//		map.put("info", info);
		mv.addObject("info",info);
		return mv;
	}

	@RequestMapping(value = "classify")
	@ResponseBody
	public Map<String, Object> classify(@RequestParam(value = "content", required = false) String content,
			HttpServletRequest request) {
		String label = "";
		double score = 0;
		Map<String,Double> wordscore = null;
		NewsClassifier classifier = new NewsClassifier();
		System.out.println("c:"+content); 
		Map<String,Object> result = classifier.classify(content);
		Map<String, Object> map = new HashMap<String, Object>();
		if(result!=null){
			label = (String)result.get("label");
			score = (Double)result.get("score");
			wordscore = (Map<String,Double>)result.get("wordScore");
		}else{
			label = "No result" ;
		}
		System.out.println("label:"+label);
		map.put("label", labelMap.getValue(label));
		map.put("score", score);
		map.put("wordScore", wordscore);
		map.put("success", "true");
		return map;

	}
	

	@RequestMapping(value = "correct")
	public ModelAndView correct(@RequestParam(value = "content", required = false) String content,
			@RequestParam(value = "label", required = false) String label) {
		ModelAndView mv = new ModelAndView("correct");
		String labels = "财经、IT、健康、体育、旅游、教育、招聘、文化、军事";
		List<String> labelList = new ArrayList<String>();
		labelList = Arrays.asList(labels.split("、"));
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("content", content);
		map.put("label", label);
		map.put("labels", labelList);
		mv.addAllObjects(map);
		return mv;
	}
	
	@RequestMapping(value = "manual")
	@ResponseBody
	public Map<String, Object> manual(@RequestParam(value = "content", required = false) String content,
			@RequestParam(value = "target", required = false) String target) {

		Map<String, Object> map = new HashMap<String, Object>();
		if(content!=null && target!=null){
			logger.info("##c##"+content+"##t##"+target);
		}else{
			logger.info("invalid manual labeled"); 
		}

		map.put("success", "true");
		return map;

	}
}
