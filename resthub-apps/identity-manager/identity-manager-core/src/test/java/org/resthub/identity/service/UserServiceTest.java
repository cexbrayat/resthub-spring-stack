package org.resthub.identity.service;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Test;
import org.resthub.core.service.ResourceGenericService;
import org.resthub.core.test.AbstractResourceServiceTest;
import org.resthub.identity.domain.model.User;

public class UserServiceTest extends AbstractResourceServiceTest<User, ResourceGenericService<User>> {

	@Inject
	@Named("userService")
	@Override
	public void setResourceService(ResourceGenericService<User> resourceService) {
		super.setResourceService(resourceService);
	}
	
	@Override
    @Test(expected = UnsupportedOperationException.class)
    public void testUpdate() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
