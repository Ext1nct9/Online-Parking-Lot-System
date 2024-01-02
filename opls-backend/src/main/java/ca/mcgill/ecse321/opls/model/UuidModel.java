package ca.mcgill.ecse321.opls.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

/**
 * Model containing an integer ID for internal use and UUID for external use.
 */
@Entity
@Inheritance(
    strategy = InheritanceType.TABLE_PER_CLASS
)
public abstract class UuidModel {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@Column(unique = true, nullable = false, updatable = false)
	private UUID uuid;
	
	public UuidModel() {
		uuid = UUID.randomUUID();
	}
	
	/**
	 * Get the integer ID for the entry. Represents the primary key in the database. Do not return publicly. 
	 */
	public int getId() {
		return id;
	}
	
	public void overrideId(int id) {
		this.id = id;
	}

	/**
	 * Get the UUID for the entry. Return this for public identification.
	 */
	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}
}
