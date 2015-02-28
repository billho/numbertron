package main.java.iitb.neo.training.ds;
/**
 * Represents the Location Relation Graph
 * @author aman
 *
 */

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import edu.washington.multirframework.multiralgorithm.SparseBinaryVector;



/*
 * Adapted from MultiR. Each location relation forms an LRGraph.
 * Copied from original: 
 * 
 * "The purpose of this data structure is to keep all relevant information for
 * learning while using as little memory as possible. Using less memory helps
 * keeping more records in memory at the same time, thus improving speed."
 * 
 * TODO: Move the list of Z nodes connected to a number node to the 
 * SparseBinaryRepresentation
 */
public class LRGraph {
	public static final int MNT_CAPACITY = 2;
	
	public String location, relation;
	
	public int random = 0;
	

	
	// mentions of this entity pair
	public int numMentions = 0;
	public int[] mentionIDs;
	public int[] Z;
	public Number[] n;
	public SparseBinaryVector[] features;
	
	public LRGraph() {
		mentionIDs = new int[MNT_CAPACITY];
		Z = new int[MNT_CAPACITY];
		features = new SparseBinaryVector[MNT_CAPACITY];
	}
	
	public void clear() {
		numMentions = 0;
	}
	
	public void setCapacity(int targetSize) {
		int[] newMentionIDs = new int[targetSize];
		int[] newZ = new int[targetSize];
		SparseBinaryVector[] newFeatures = new SparseBinaryVector[targetSize];
		if (numMentions > 0) {
			System.arraycopy(mentionIDs, 0, newMentionIDs, 0, numMentions);
			System.arraycopy(Z, 0, newZ, 0, numMentions);
			System.arraycopy(features, 0, newFeatures, 0, numMentions);
		}
		mentionIDs = newMentionIDs;
		Z = newZ;
		features = newFeatures;
	}
	
	
	public boolean read(DataInputStream dis) throws IOException {
		try {
			random = dis.readInt();
			location = dis.readUTF();
			relation = dis.readUTF();			
			int lenN = dis.readInt();
			n = new Number[lenN];
			for (int i=0; i < lenN; i++) {
				n[i] = new Number();
				n[i].deserialize(dis);
			};
			int numMentions = dis.readInt();
			if (numMentions > mentionIDs.length) setCapacity(numMentions);
			this.numMentions = numMentions;
			for (int i=0; i < numMentions; i++) {
				mentionIDs[i] = dis.readInt();
				Z[i] = dis.readInt();
				if (features[i] == null) features[i] = new SparseBinaryVector();
				features[i].deserialize(dis);
			}
			
			//location = relation = null;
			//mentionIDs = null;
			
			return true;
		} catch (EOFException e) { return false; }
	}
	
	public void write(DataOutputStream dos) throws IOException {
		dos.writeInt(random);
		dos.writeUTF(location);
		dos.writeUTF(relation);
		dos.writeInt(n.length);
		for (int i=0; i < n.length; i++)
			n[i].serialize(dos);
		dos.writeInt(numMentions);
		for (int i=0; i < numMentions; i++) {
			dos.writeInt(mentionIDs[i]);
			dos.writeInt(Z[i]);
			features[i].serialize(dos);
		}
	}
	
	@Override
	public String toString(){

		StringBuilder sb = new StringBuilder();
		sb.append(location);
		sb.append("\t");
		sb.append(relation);
		sb.append("\t");
		for(Number n_node: n){
			sb.append(n_node);
			sb.append("|");
		}
		sb.setLength(sb.length()-1);
		sb.append("\n");
		for(int i = 0; i < numMentions; i++){
			sb.append("Mention " + i+ " ");
			for(int feat: features[i].ids){
				sb.append(feat);
				sb.append(" ");
			}
			sb.append("\n");
		}
		sb.setLength(sb.length()-1);

		
		
		return sb.toString().trim();
	}
	
	public static void main(String[] args) throws IOException{
		
		LRGraph d = new LRGraph();
		DataInputStream dis = new DataInputStream(new FileInputStream(new File(args[0])));
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(args[1])));
		while(d.read(dis)){
			bw.write(d.toString()+"\n");
		}
		bw.close();
		dis.close();
	}
}