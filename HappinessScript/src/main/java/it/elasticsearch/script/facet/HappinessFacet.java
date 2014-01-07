package it.elasticsearch.script.facet;

import org.elasticsearch.search.facet.Facet;

public interface HappinessFacet extends Facet {

	public static final String TYPE = "happiness";

	public Object facet();

	public Object getFacet();

}