package fr.twitteranalyzer.model;

public class ElasticSearchConnection {

	private String clusterName = "frenchtweets";
	private String elasticSearchHost = "localhost";
	private int elasticSearchPort = 9300;

	public ElasticSearchConnection() {
		// Do nothing
	}

	public ElasticSearchConnection(String clusterName, String elasticSearchHost, int elasticSearchPort) {
		this.clusterName = clusterName;
		this.elasticSearchHost = elasticSearchHost;
		this.elasticSearchPort = elasticSearchPort;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public String getElasticSearchHost() {
		return elasticSearchHost;
	}

	public void setElasticSearchHost(String elasticSearchHost) {
		this.elasticSearchHost = elasticSearchHost;
	}

	public int getElasticSearchPort() {
		return elasticSearchPort;
	}

	public void setElasticSearchPort(int elasticSearchPort) {
		this.elasticSearchPort = elasticSearchPort;
	}

}
