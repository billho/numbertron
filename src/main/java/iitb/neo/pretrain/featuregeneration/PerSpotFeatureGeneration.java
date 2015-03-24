package main.java.iitb.neo.pretrain.featuregeneration;
/**
 * Copy pasted the default feature generation code to modify get SAPs
 * TODO: Refactor the multirframework to prevent this copy pasting
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import edu.washington.multirframework.corpus.Corpus;
import edu.washington.multirframework.corpus.CorpusInformationSpecification;
import edu.washington.multirframework.corpus.CorpusInformationSpecification.SentGlobalIDInformation.SentGlobalID;
import edu.washington.multirframework.featuregeneration.FeatureGeneration.SententialArgumentPair;
import edu.washington.multirframework.featuregeneration.FeatureGenerator;
import edu.washington.multirframework.util.BufferedIOUtils;

public class PerSpotFeatureGeneration {
	
	private FeatureGenerator fg;
	public PerSpotFeatureGeneration(FeatureGenerator fg){
		this.fg = fg;
	}
	
	public void run(List<String> dsFileNames, List<String> featureFileNames, Corpus c, CorpusInformationSpecification cis) throws FileNotFoundException, IOException, SQLException, InterruptedException, ExecutionException{
		
		long originalStart = System.currentTimeMillis();
    	long start = System.currentTimeMillis();
		//initialize variables
    	
    	List<SententialArgumentPair> saps = getSaps(dsFileNames,featureFileNames);
    	//System.out.println("size of pairs " + saps.size() + "\n" + saps.get(0));
    	long end = System.currentTimeMillis();
    	System.out.println("Sentential Argument Pair collection took " + (end-start) + "milliseconds");
    	
    	//get map from SentID to Sap
    	start = System.currentTimeMillis();
    	//There can be several matchings in a sentence, collect them
    	Map<Integer,List<SententialArgumentPair>> sapMap = new HashMap<>();
    	for(SententialArgumentPair sap : saps){
    		Integer id = sap.sentID;
    		if(sapMap.containsKey(id)){
    			sapMap.get(id).add(sap);
    		}
    		else{
    			List<SententialArgumentPair> sameIdSaps = new ArrayList<>();
    			sameIdSaps.add(sap);
    			sapMap.put(id,sameIdSaps);
    		}
    	}
    	end = System.currentTimeMillis();
    	System.out.println("Map from sentence ids to saps created in " + (end-start) + " milliseconds");
    	
    	//initialize feature Writers
    	Map<String,BufferedWriter> writerMap = new HashMap<>();
    	for(int i =0; i < dsFileNames.size(); i++){
    		String dsFileName = dsFileNames.get(i);
    		String featureFileName = featureFileNames.get(i);
    		BufferedWriter bw= new BufferedWriter(new FileWriter(new File(featureFileName)));
    		writerMap.put(dsFileName, bw);
    	}
    	
    	//iterate over corpus
    	Iterator<Annotation> di = c.getDocumentIterator();
    	int docCount =0;
    	start = System.currentTimeMillis();
    	while(di.hasNext()){
    		Annotation doc = di.next();
    		
    		List<CoreMap> sentences = doc.get(CoreAnnotations.SentencesAnnotation.class);
    		for(CoreMap sentence: sentences){
    				
    			Integer currSentID = sentence.get(SentGlobalID.class);
    			if(sapMap.containsKey(currSentID)){
    				//System.out.println(sentence);
    				List<SententialArgumentPair> sentenceSaps = sapMap.get(currSentID);
    				writeFeatures(sentenceSaps,doc,sentence,writerMap);
    			}
    		}
    		docCount++;
    		if(docCount % 100000 == 0){
    			end = System.currentTimeMillis();
    			System.out.println(docCount + " documents processed in " + (end-start) + "milliseconds");
    			start = System.currentTimeMillis();
    		}
    	}
    	
    	
    	//close writers
    	for(String key : writerMap.keySet()){
    		BufferedWriter bw = writerMap.get(key);
    		bw.close();
    	}
		
    	end = System.currentTimeMillis();
    	System.out.println("Feature Generation took " + (end-originalStart) + " millisseconds");
    	
	}

	private void writeFeatures(List<SententialArgumentPair> currentSaps,
			Annotation doc, CoreMap sentence,
			Map<String, BufferedWriter> writerMap) throws IOException {
		//System.out.println(currentSaps.size());
		for(SententialArgumentPair sap : currentSaps){
			BufferedWriter bw = writerMap.get(sap.partitionID);

			List<String> features = fg.generateFeatures(sap.arg1Offsets.first,sap.arg1Offsets.second
					,sap.arg2Offsets.first,sap.arg2Offsets.second,sap.arg1ID,sap.arg2ID,sentence,doc);
			bw.write(makeFeatureString(sap,features)+"\n");
		}
	}

	private List<SententialArgumentPair> getSaps(List<String> dsFileNames,
			List<String> featureFileNames) throws FileNotFoundException, IOException {
    	List<SententialArgumentPair> saps = new ArrayList<>(); 
    	
    	for(int i =0; i < dsFileNames.size(); i++){
    		
    		String dsFileName = dsFileNames.get(i);
    		String featureFileName = featureFileNames.get(i);
			BufferedReader in;
			BufferedWriter bw;
			in = BufferedIOUtils.getBufferedReader(new File(dsFileName));
			bw = BufferedIOUtils.getBufferedWriter(new File(featureFileName));
	
			String nextLine = in.readLine();
			List<SententialArgumentPair> currentSaps = new ArrayList<>();
			while(nextLine != null){
				SententialArgumentPair sap = SententialArgumentPair.parseSAP(nextLine);
				sap.setPartitionId(dsFileName);
				currentSaps.add(sap);
				nextLine = in.readLine();
			}
			bw.close();
			in.close();
			saps.addAll(currentSaps);
    	}
		
    	//sort saps by global sentence id
    	Collections.sort(saps,new Comparator<SententialArgumentPair>(){
			@Override
			public int compare(SententialArgumentPair arg0,
					SententialArgumentPair arg1) {
				return (arg0.sentID - arg1.sentID);
			}
    		
    	});
    	
		return saps;
	}

	private   String makeFeatureString(SententialArgumentPair sap,
			List<String> features) {
		StringBuilder sb = new StringBuilder();
		sb.append(String.valueOf(sap.getSentID()));
		sb.append("\t");
		sb.append(sap.getArg1Id());
		sb.append("\t");
		sb.append(sap.getArg2Id());
		sb.append("\t");
		for(String rel : sap.getRelations()){
			sb.append(rel);
			sb.append("&&");
		}
		sb.setLength(sb.length()-2);
		sb.append("\t");
		for(String f: features){
			sb.append(f);
			sb.append("\t");
		}
		return sb.toString().trim();
	}
	
	
}
