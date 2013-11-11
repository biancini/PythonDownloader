package fr.twitternalyzer.utils;

import java.util.ArrayList;

public class TweetList<E> extends ArrayList<E> {
	
	private static final long serialVersionUID = 1334569298679496397L;

	public String getAllElements(String separator) {
    	StringBuilder sb = new StringBuilder();
    	for(int i = 0; i < this.size(); i++)
    	{
    		sb.append(this.get(i));
    		if (i < this.size() - 1) sb.append(separator);
    	}
    	return sb.toString();				
	}
}
