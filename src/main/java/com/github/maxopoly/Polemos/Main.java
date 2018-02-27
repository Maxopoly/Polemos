package com.github.maxopoly.Polemos;

import com.github.maxopoly.Polemos.action.AbstractAction;
import com.github.maxopoly.Polemos.output.HtmlGenerator;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class Main {

	public static void main(String [] args) {
		if (args.length < 2) {
			System.out.println("Must specify path to log and output");
			System.exit(0);
		}
		String logPath = args [0];
		File folder = new File(logPath);
		List<AbstractAction> actions = new LinkedList<AbstractAction>();
		/*for(File f : folder.listFiles()) {
			LogAnalyzer analyzer = new LogAnalyzer(FileUtil.parseFile(f.getAbsolutePath()));
			analyzer.parse();
			actions.addAll(analyzer.getResult());
		} */
		LogAnalyzer analyzer = new LogAnalyzer(FileUtil.parseFile(logPath));
		analyzer.parse();
		actions.addAll(analyzer.getResult());
		DataAggregator aggregator = new DataAggregator(actions, analyzer.getMetaData());
		String outPath = args [1];
		File output = new File(outPath);
		FileUtil.saveToFile(output,new HtmlGenerator(aggregator.getStats(), analyzer.getMetaData()).generate());
	}

}
