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

package com.groundwork.collage.biz;

import com.groundwork.collage.CollageAdminInfrastructure;
import com.groundwork.collage.CollageAdminMetadata;
import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.test.AbstractTestCaseWithTransactionSupport;
import org.groundwork.foundation.bs.metadata.MetadataService;


public abstract class AbstractTestBizBase extends AbstractTestCaseWithTransactionSupport {

	//protected CollageFactory collage = null;
	protected CollageAdminInfrastructure admin = null;
	protected CollageAdminMetadata   adminMeta = null;
	protected MetadataService metadataService;

	public AbstractTestBizBase(String x)
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
        collage = CollageFactory.getInstance();
        collage.loadSpringAssembly("META-INF/test-common-model-assembly.xml");
        collage.loadSpringAssembly("META-INF/admin-api-assembly.xml");
        collage.loadSpringAssembly("META-INF/biz-assembly.xml");

        admin     = (CollageAdminInfrastructure)collage.getAPIObject(CollageFactory.ADMIN_SERVICE);
        adminMeta = (CollageAdminMetadata)collage.getAPIObject(CollageFactory.ADMIN_METADATA_SERVICE);
        metadataService  = collage.getMetadataService();
    }
}
