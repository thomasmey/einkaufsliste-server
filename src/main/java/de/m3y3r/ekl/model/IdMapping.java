package de.m3y3r.ekl.model;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name="id_mapping",
	uniqueConstraints = {
		@UniqueConstraint(columnNames = {"object_name", "id_intern"}),
		@UniqueConstraint(columnNames = {"id_extern"})
})
@NamedQueries( {
	@NamedQuery(name = "IdMapping.getByIdIntern", query = "SELECT o FROM IdMapping o where o.idIntern = :idIntern and o.objectName = :objectName")
})
public class IdMapping implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	private Integer id;

	@Column(name = "object_name", nullable=false)
	@NotNull
	private String objectName;

	@Column(name = "id_intern", nullable=false)
	@NotNull
	private Integer idIntern;

	@Column(name = "id_extern", nullable=false)
	@NotNull
	private UUID idExtern;

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getIdIntern() {
		return idIntern;
	}
	public void setIdIntern(Integer idIntern) {
		this.idIntern = idIntern;
	}
	public UUID getIdExtern() {
		return idExtern;
	}
	public void setIdExtern(UUID idExtern) {
		this.idExtern = idExtern;
	}
	public String getObjectName() {
		return objectName;
	}
	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}
}
