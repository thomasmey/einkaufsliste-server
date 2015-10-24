package de.m3y3r.common.model;

import java.io.Serializable;
import javax.persistence.*;

@Entity
@Table(name="oauth_user_role")
@DiscriminatorValue("OAUTH_USER")
public class OauthUserRole extends Role implements Serializable {

	private static final long serialVersionUID = 1L;

}
