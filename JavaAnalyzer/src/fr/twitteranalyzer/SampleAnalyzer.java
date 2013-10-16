package fr.twitteranalyzer;


public class SampleAnalyzer extends HTTPUtilities {

	String elasticSearchHost = "localhost";
	int elasticSearchPort = 9200;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SampleAnalyzer analyzer = new SampleAnalyzer();
		System.out.println(analyzer.getHTML(args[0]));

	}

}
