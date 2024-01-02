package ca.mcgill.ecse321.opls.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

/**
 * Model containing a user-friendly string ID and display name.
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class NameIdModel {
	@Id
	private String id;

	@Column(nullable = false)
	private String displayName;

	public String getId() {
		return id;
	}

	/**
	 * Set string ID independently of the display name.
	 */
	public void setId(String id) {
		this.id = id;
	}

	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Set the display name and convert into an ID to store as the ID. The
	 * conversion replaces spaces with dash characters and converts everything
	 * to lowercase.
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
		this.id = displayName.replace(' ', '-').toLowerCase();
	}
}
