package de.m3y3r.ekl.api.model;

import java.math.BigDecimal;

public class ItemPost {

	private String name;
	private BigDecimal count;
	private String unit;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public BigDecimal getCount() {
		return count;
	}
	public void setCount(BigDecimal count) {
		this.count = count;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
}
