package org.openconceptlab.fhir.model;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the auth_group_permissions database table.
 * @author harpatel1
 */
@Entity
@Table(name="auth_group_permissions")
public class AuthGroupPermission extends BaseOclEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne
	@JoinColumn(name="group_id")
	private AuthGroup authGroup;

	@ManyToOne
	@JoinColumn(name="permission_id")
	private AuthPermission authPermission;

	public AuthGroupPermission() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public AuthGroup getAuthGroup() {
		return this.authGroup;
	}

	public void setAuthGroup(AuthGroup authGroup) {
		this.authGroup = authGroup;
	}

	public AuthPermission getAuthPermission() {
		return this.authPermission;
	}

	public void setAuthPermission(AuthPermission authPermission) {
		this.authPermission = authPermission;
	}

}
