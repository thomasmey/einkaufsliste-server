package de.m3y3r.common.model;

import java.io.Serializable;
import javax.persistence.*;

@Entity
@Inheritance(strategy=InheritanceType.JOINED)
@DiscriminatorColumn(name="ROLE_TYPE", discriminatorType=DiscriminatorType.STRING, length=32)
public abstract class Role implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	private Integer id;

}
