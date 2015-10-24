package de.m3y3r.ekl.model;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
public class Item implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
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
}
