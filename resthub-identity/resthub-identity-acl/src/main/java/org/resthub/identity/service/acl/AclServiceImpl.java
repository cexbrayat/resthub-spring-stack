package org.resthub.identity.service.acl;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.util.FileCopyUtils;

/**
 * Implementation of the ACL Service based on Spring Security.
 */
@Named("idmAclService")
public class AclServiceImpl implements AclService {

	private static final Logger logger = LoggerFactory.getLogger(AclServiceImpl.class);

	// -----------------------------------------------------------------------------------------------------------------
	// Protected attributes

	/**
	 * Spring Security's ACL facility. Injected by Spring.
	 */
	@Inject
	@Named("aclService")
	protected JdbcMutableAclService aclService;

	/**
	 * Datasource used to access to database.
	 */
	@Inject
	@Named("dataSource")
	protected DataSource datasource;

	/**
	 * Mapper between strings and permissions. Injected by Spring.
	 */
	@Inject
	protected ConfigurablePermissionFactory permissionFactory;

	// -----------------------------------------------------------------------------------------------------------------
	// public methods

	/**
	 * Initialization: creates Spring Security's tables, using datasourcE.
	 */
	@PostConstruct
	public void init() {

		try {
			Connection connection = datasource.getConnection();
			DatabaseMetaData metatData = connection.getMetaData();
			String productName = metatData.getDatabaseProductName().toLowerCase();

			if (productName.equals("postgresql")) {
				logger.info("PostgreSQL support activated for ACL Service");
				aclService.setClassIdentityQuery("select currval(pg_get_serial_sequence('acl_class', 'id'))");
				aclService.setSidIdentityQuery("select currval(pg_get_serial_sequence('acl_sid', 'id'))");
			}

		} catch (Exception exc) {
			throw new BeanCreationException("Cannot set SpringSecurity ACL tables in db", exc);
		}

	} // init().

	// -----------------------------------------------------------------------------------------------------------------
	// Inherited methods

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveAcl(Object domainObject, Serializable domainObjectId, String ownerId, String permission) {
		// Sid identifies the user.
		Sid owner = new PrincipalSid(ownerId);
		// ObjectIdentity is a unic identifier for the model object
		ObjectIdentity oid = new ObjectIdentityImpl(domainObject.getClass(), domainObjectId);

		// Creates the acl, or update the existing one.
		MutableAcl acl;
		try {
			acl = (MutableAcl) aclService.readAclById(oid);
		} catch (NotFoundException nfe) {
			acl = aclService.createAcl(oid);
		}

		// Update the acl for this user on this model object.
		acl.insertAce(acl.getEntries().size(), permissionFactory.buildFromName(permission), owner, true);
		aclService.updateAcl(acl);
	} // saveAcl().

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeAcl(Object domainObject, Serializable domainObjectId, String ownerId, String permission) {
		// Sid identifies the user.
		Sid owner = new PrincipalSid(ownerId);
		// ObjectIdentity is a unic identifier for the model object
		ObjectIdentity oid = new ObjectIdentityImpl(domainObject.getClass(), domainObjectId);

		// Gets the existing acl.
		MutableAcl acl = (MutableAcl) aclService.readAclById(oid);
		Permission effectivePerm = permissionFactory.buildFromName(permission);

		// Remove all permissions associated with this particular recipient
		// (string equality to KISS)
		List<AccessControlEntry> entries = acl.getEntries();
		int i = 0;
		for (AccessControlEntry entry : entries) {
			if (entry.getSid().equals(owner) && entry.getPermission().equals(effectivePerm)) {
				acl.deleteAce(i);
			}
			i++;
		}

		// Update the acl for this user on this model object.
		aclService.updateAcl(acl);
	} // removeAcl().

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Acl getAcls(Object domainObject, Serializable domainObjectId) {
		// ObjectIdentity is a unic identifier for the model object
		ObjectIdentity oid = new ObjectIdentityImpl(domainObject.getClass(), domainObjectId);
		MutableAcl acl = (MutableAcl) aclService.readAclById(oid);
		return acl;
	} // getAcl().

} // class AclServiceImpl.
