package main.java.iitb.neo.autoeval;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Map;

import main.java.iitb.neo.util.JsonUtils;

public class GeneratePRCurves {
	
	public static double startConf = 0.8;
	public static double endConf = 1.0;
	public static double confwidth = 0.05;
	
	public static String prCurveFilePrefix = "PR_data_";
	
	String modelName;
	EvaluateModel emodel = null;

	GeneratePRCurves(String propertiesFile) throws Exception{
		Map<String, Object> properties = JsonUtils.getJsonMap(propertiesFile);
		modelName = JsonUtils.getListProperty(properties, "models").get(0);

		emodel = new EvaluateModel(propertiesFile);
	}
	
	public void generate() throws SQLException, IOException{
		String outputFile = prCurveFilePrefix + modelName.substring(modelName.lastIndexOf("/")+1) + ".tsv";
		System.out.println("Writing to : "+outputFile);
		PrintWriter pw = new PrintWriter((new File(outputFile)));
		
		for(double conf = startConf; conf <= endConf; conf += confwidth){
			emodel.efc.setConfidence(conf);
			emodel.evaluate();
			pw.write(conf+"\t"+emodel.precision+"\t"+emodel.recall+"\t"+f(emodel.precision, emodel.recall)+"\n");
		}
		
		pw.close();
	}
	
	public static void main(String args[]) throws Exception {
		
		GeneratePRCurves pr = new GeneratePRCurves(args[0]);
		pr.generate();
	}
	
	public double f(double p, double r) {
		assert (p + r > 0);
		if (p + r == 0) {
			return 0;
		}
		return (2 * p * r) / (p + r);
	}
}
