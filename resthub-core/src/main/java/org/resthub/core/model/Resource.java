package org.resthub.core.model;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Base resource model class defined by an id and a reference.
 * It is usually used as base class for RESTful application domain model.
 */
@SuppressWarnings("serial")
@MappedSuperclass
@Inheritance(strategy = InheritanceType.JOINED)
@XmlRootElement
public abstract class Resource implements Serializable {
    
    private Long id;
    private String ref;

	/**
     * Default constructor.
     */
    public Resource() {
        super();
    }
    
    public Resource(String ref) {
        this();
        this.ref = ref;
    }

    /**
     * Resource identifier automatically generated by the database.
     */
    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    /**
     * Human readable resource reference 
     */
    public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Resource other = (Resource) obj;
        if ((this.id == null) ? (other.getId() != null) : !this.id.equals(other.getId())) {
            return false;
        }
        if ((this.ref == null) ? (other.getRef() != null) : !this.ref.equals(other.getRef())) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 43 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }
}