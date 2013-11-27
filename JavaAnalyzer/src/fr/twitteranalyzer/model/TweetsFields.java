package fr.twitteranalyzer.model;

import java.util.ArrayList;

public enum TweetsFields {
	CREATEDAT("created_at"), LOCATION("location"), NUMFRIENDS("num_friends"), COORDINATES(
			"coordinates"), HAPPINESS("happiness"), RELEVANCE("relevance"), USERID(
			"userid");

	private String fieldName = null;

	private TweetsFields(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldName() {
		return fieldName;
	}

	public static String[] getFieldList() {
		ArrayList<String> fieldList = new ArrayList<String>();

		fieldList.add(TweetsFields.CREATEDAT.getFieldName());
		fieldList.add(TweetsFields.LOCATION.getFieldName());
		fieldList.add(TweetsFields.NUMFRIENDS.getFieldName());
		fieldList.add(TweetsFields.COORDINATES.getFieldName());
		fieldList.add(TweetsFields.HAPPINESS.getFieldName());
		fieldList.add(TweetsFields.RELEVANCE.getFieldName());
		fieldList.add(TweetsFields.USERID.getFieldName());

		return fieldList.toArray(new String[] {});
	}
}
