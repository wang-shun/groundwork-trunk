/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2014  GroundWork Open Source Solutions info@groundworkopensource.com

 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.

 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.

 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.groundwork.foundation.bs;

import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.dao.FoundationDAO;
import org.groundwork.foundation.dao.SortCriteria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * UUIDEntityBusinessServiceImpl
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class UUIDEntityBusinessServiceImpl extends AbstractEntityBusinessServiceImpl {

    /*************************************************************************/
	/* Constructors */
    /*************************************************************************/

    /**
     * Constructor
     *
     * @param foundationDAO
     * @param interfaceName
     * @param componentName
     */
    protected UUIDEntityBusinessServiceImpl(FoundationDAO foundationDAO, String interfaceName, String componentName)
    {
        super(foundationDAO, interfaceName, componentName);
    }

    /*************************************************************************/
	/* Protected Methods */
    /*************************************************************************/

    protected void delete(UUID objectId) throws BusinessServiceException
    {
        try
        {
            _foundationDAO.deleteByUUID(_componentName, objectId);
        }
        catch (Exception e)
        {
            throw new BusinessServiceException(e);
        }
    }

    protected void delete(UUID[] objectIds) throws BusinessServiceException
    {
        _foundationDAO.deleteByUUID(_componentName, convertToUUIDCollection(objectIds));
    }

    protected void delete(String[] objectIds) throws BusinessServiceException
    {
        try {
            _foundationDAO.deleteByUUID(_componentName, convertToUUIDCollection(objectIds));
        }
        catch (Exception e)
        {
            throw new BusinessServiceException(e);
        }
    }

    protected Object queryById (UUID id)
            throws BusinessServiceException
    {
        try {
            return _foundationDAO.queryByUUID(_componentName, id);
        }
        catch (Exception e)
        {
            throw new BusinessServiceException(e);
        }
    }

    protected List queryById (UUID[] ids, SortCriteria sortCriteria)
            throws BusinessServiceException
    {
        try {
            return _foundationDAO.queryByUUID(_componentName,
                    convertToUUIDCollection(ids),
                    sortCriteria);
        }
        catch (Exception e)
        {
            throw new BusinessServiceException(e);
        }
    }

    protected List queryById (String[] ids, SortCriteria sortCriteria)
            throws BusinessServiceException
    {
        try {
            return _foundationDAO.queryByUUID(_componentName,
                    convertToUUIDCollection(ids),
                    sortCriteria);
        }
        catch (Exception e)
        {
            throw new BusinessServiceException(e);
        }
    }

    /*************************************************************************/
	/* Private Methods */
    /*************************************************************************/

    private Collection<UUID> convertToUUIDCollection (UUID[] vals)
    {
        // Convert array to list
        return ((vals != null) ? Arrays.asList(vals) : Collections.EMPTY_LIST);
    }

    private Collection<UUID> convertToUUIDCollection (String[] vals)
    {
        Collection<UUID> col = new ArrayList<UUID>();

        // Return empty collection
        if (vals == null)
            return col;

        int length = vals.length;
        int id;
        for (int i = 0; i < length; i++)
        {
            // Convert String to UUID
            col.add(UUID.fromString(vals[i]));
        }

        return col;
    }
}
