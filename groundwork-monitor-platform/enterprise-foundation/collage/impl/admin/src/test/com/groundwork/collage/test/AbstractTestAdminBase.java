/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com

 *	 This program is free software; you can redistribute it and/or modify
 *	 it under the terms of version 2 of the GNU General Public License
 *	 as published by the Free Software Foundation.

 *	 This program is distributed in the hope that it will be useful,
 *	 but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	 GNU General Public License for more details.

 *	 You should have received a copy of the GNU General Public License
 *	 along with this program; if not, write to the Free Software
 *	 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.groundwork.collage.test;

import org.groundwork.foundation.bs.metadata.MetadataService;

import com.groundwork.collage.CollageAccessor;
import com.groundwork.collage.CollageAdminInfrastructure;
import com.groundwork.collage.CollageAdminMetadata;
import com.groundwork.collage.CollageFactory;


/**
 * This is a convenience abstract class for test classes that test the
 * different methods of the CollageAdminInfrastructure services - it
 * instantiates a {@link CollageAccessor}, a {@link CollageAdminInfrastructure} service,
 * a {@link CollageAdminMetadata} service, and a {@link MetadataService} instance
 *
 * @author <a href="mailto:philippe.paravicini@eCommerceStudio.com">Philippe Paravicini</a>
 * @version $Id: AbstractTestAdminBase.java 8692 2007-10-15 20:49:04Z glee $
 */
public abstract class AbstractTestAdminBase extends AbstractTestCaseWithTransactionSupport {

	//protected CollageFactory collage = null;
	protected CollageAdminInfrastructure admin = null;
	protected CollageAdminMetadata   adminMeta = null;
	protected MetadataService metadataService;

	public AbstractTestAdminBase(String x) 
	{
		super(x);
	}

    public void setUp() {
        try {
            super.setUp();
            assertNotNull(collage);
        }
        catch (Exception exc) {
            exc.getMessage();
        }
        collage.loadSpringAssembly("META-INF/test-common-model-assembly.xml");
        collage.loadSpringAssembly("META-INF/admin-api-assembly.xml");

        admin     = (CollageAdminInfrastructure)collage.getAPIObject(CollageFactory.ADMIN_SERVICE);
        adminMeta = (CollageAdminMetadata)collage.getAPIObject(CollageFactory.ADMIN_METADATA_SERVICE);
        metadataService  = collage.getMetadataService();
    }
}
