package fr.twitteranalyzer;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

public class Analyzer {
	
	protected String elasticSearchHost = "localhost";
	protected int elasticSearchPort = 9300;
	protected Client client = null;
	
	public Analyzer() {
		client = getElasticSearchClient(elasticSearchHost, elasticSearchPort);
	}
	
	protected Client getElasticSearchClient(String hostname, int port) {
		Settings settings = ImmutableSettings.settingsBuilder()
				.put("cluster.name", "frenchtweets")
				.build();
		
		TransportClient transportClient = new TransportClient(settings);
		transportClient = transportClient.addTransportAddress(new InetSocketTransportAddress(hostname, port));
		
		return transportClient;
	}

}
