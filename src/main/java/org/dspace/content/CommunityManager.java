package org.dspace.content;

import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.dspace.eperson.Group;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.MissingResourceException;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 17/03/14
 * Time: 13:48
 */
public interface CommunityManager extends DSpaceObjectManager<Community> {

    public Community find(Context context, int id) throws SQLException;

    public Community create(Community parent, Context context) throws SQLException, AuthorizeException;

    public Community create(Community parent, Context context, String handle) throws SQLException, AuthorizeException;

    public List<Community> findAll(Context context) throws SQLException;

    public List<Community> findAllTop(Context context) throws SQLException;

    public void setName(Community community, String value)throws MissingResourceException;

    public Bitstream setLogo(Context context, Community community, InputStream is) throws AuthorizeException, IOException, SQLException;

    public Group createAdministrators(Context context, Community community) throws SQLException, AuthorizeException;

    public void removeAdministrators(Context context, Community community) throws SQLException, AuthorizeException;

    public List<Community> getAllParents(Community community) throws SQLException;

    public List<Collection> getAllCollections(Community community) throws SQLException;

    public void addCollection(Context context, Community community, Collection collection) throws SQLException,
            AuthorizeException;

    public Community createSubcommunity(Context context, Community parentCommunity) throws SQLException,
            AuthorizeException;

    public Community createSubcommunity(Context context, Community parentCommunity, String handle) throws SQLException,
            AuthorizeException;

    public void addSubcommunity(Context context, Community parentCommunity, Community childCommunity) throws SQLException, AuthorizeException;

    public void removeCollection(Context context, Community community, Collection c) throws SQLException,
            AuthorizeException, IOException;

    public void removeSubcommunity(Context context, Community parentCommunity, Community childCommunity) throws SQLException,
            AuthorizeException, IOException;

    public void delete(Context context, Community community) throws SQLException, AuthorizeException, IOException;

    public boolean canEditBoolean(Context context, Community community) throws java.sql.SQLException;

    public void canEdit(Context context, Community community) throws AuthorizeException, SQLException;

    public DSpaceObject getAdminObject(Community community, int action) throws SQLException;

    public DSpaceObject getParentObject(Context context, Community community) throws SQLException;
}
