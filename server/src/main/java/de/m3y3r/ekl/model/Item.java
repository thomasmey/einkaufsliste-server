package de.m3y3r.ekl.model;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@SequenceGenerator(initialValue = 100, name="ItemSeqGen", sequenceName="seq_item")
public class Item implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator="ItemSeqGen")
	private Integer id;

	@Column(nullable=false)
	@NotNull
	private String name;

	@Column
	private BigDecimal menge;

	@Column(nullable=false)
	@Enumerated(EnumType.STRING)
	private ItemUnit unit;

	@ManyToOne
	private Einkaufsliste ekl;

	@Column(name="data_version")
	@Version
	private int dataVersion;

	@Column(nullable=false)
	@Enumerated(EnumType.STRING)
	private ItemStatus status;

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public BigDecimal getMenge() {
		return menge;
	}
	public void setMenge(BigDecimal menge) {
		this.menge = menge;
	}
	public ItemUnit getUnit() {
		return unit;
	}
	public void setUnit(ItemUnit unit) {
		this.unit = unit;
	}
	public Einkaufsliste getEkl() {
		return ekl;
	}
	public void setEkl(Einkaufsliste ekl) {
		this.ekl = ekl;
	}
	public int getDataVersion() {
		return dataVersion;
	}
	public void setDataVersion(int dataVersion) {
		this.dataVersion = dataVersion;
	}
	public ItemStatus getStatus() {
		return status;
	}
	public void setStatus(ItemStatus status) {
		this.status = status;
	}
}
