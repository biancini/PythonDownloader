package fr.twitteranalyzer.model;

import com.vividsolutions.jts.geom.GeometryCollection;

public class FrenchDepartment {
	private String departmentName = null;
	private GeometryCollection geometry = null;

	public FrenchDepartment(String departmentName) {
		this.departmentName = departmentName;
	}

	public String getDepartmentName() {
		return departmentName;
	}

	public GeometryCollection getGeometry() {
		return geometry;
	}

	public void setGeometry(GeometryCollection geometry) {
		this.geometry = geometry;
	}

}
