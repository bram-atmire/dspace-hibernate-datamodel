/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.util;

import java.sql.SQLException;
import java.util.List;

import org.dspace.authorize.AuthorizeConfiguration;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.content.*;
import org.dspace.content.service.CollectionService;
import org.dspace.content.service.CommunityService;
import org.dspace.content.service.ItemService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.factory.DSpaceServiceFactory;

/**
 * This class is an addition to the AuthorizeManager that perform authorization
 * check on not CRUD (ADD, WRITE, etc.) actions.
 * 
 * @author bollini
 * 
 */
public class AuthorizeUtil
{

    private static final CommunityService COMMUNITY_SERVICE = DSpaceServiceFactory.getInstance().getCommunityService();
    private static final CollectionService COLLECTION_SERVICE = DSpaceServiceFactory.getInstance().getCollectionService();
    private static final ItemService ITEM_SERVICE = DSpaceServiceFactory.getInstance().getItemService();

    /**
     * Is allowed manage (create, remove, edit) bitstream's policies in the
     * current context?
     * 
     * @param context
     *            the DSpace Context Object
     * @param bitstream
     *            the bitstream that the policy refer to
     * @throws AuthorizeException
     *             if the current context (current user) is not allowed to
     *             manage the bitstream's policies
     * @throws SQLException
     *             if a db error occur
     */
    public static void authorizeManageBitstreamPolicy(Context context,
            Bitstream bitstream) throws AuthorizeException, SQLException
    {
        Bundle bundle = bitstream.getBundles().iterator().next();
        authorizeManageBundlePolicy(context, bundle);
    }

    /**
     * Is allowed manage (create, remove, edit) bundle's policies in the
     * current context?
     * 
     * @param context
     *            the DSpace Context Object
     * @param bundle
     *            the bundle that the policy refer to
     * @throws AuthorizeException
     *             if the current context (current user) is not allowed to
     *             manage the bundle's policies
     * @throws SQLException
     *             if a db error occur
     */
    public static void authorizeManageBundlePolicy(Context context,
            Bundle bundle) throws AuthorizeException, SQLException
    {
        Item item = bundle.getItems().iterator().next();
        authorizeManageItemPolicy(context, item);
    }

    /**
     * Is allowed manage (create, remove, edit) item's policies in the
     * current context?
     * 
     * @param context
     *            the DSpace Context Object
     * @param item
     *            the item that the policy refer to
     * @throws AuthorizeException
     *             if the current context (current user) is not allowed to
     *             manage the item's policies
     * @throws SQLException
     *             if a db error occur
     */
    public static void authorizeManageItemPolicy(Context context, Item item)
            throws AuthorizeException, SQLException
    {
        if (AuthorizeConfiguration.canItemAdminManagePolicies())
        {
            AuthorizeManager.authorizeAction(context, item, Constants.ADMIN);
        }
        else if (AuthorizeConfiguration.canCollectionAdminManageItemPolicies())
        {
            AuthorizeManager.authorizeAction(context, item
                    .getOwningCollection(), Constants.ADMIN);
        }
        else if (AuthorizeConfiguration.canCommunityAdminManageItemPolicies())
        {
            AuthorizeManager
                    .authorizeAction(context, item.getOwningCollection()
                            .getCommunities().iterator().next(), Constants.ADMIN);
        }
        else if (!AuthorizeManager.isAdmin(context))
        {
            throw new AuthorizeException(
                    "Only system admin are allowed to manage item policies");
        }
    }

    /**
     * Is allowed manage (create, remove, edit) collection's policies in the
     * current context?
     * 
     * @param context
     *            the DSpace Context Object
     * @param collection
     *            the collection that the policy refer to
     * @throws AuthorizeException
     *             if the current context (current user) is not allowed to
     *             manage the collection's policies
     * @throws SQLException
     *             if a db error occur
     */
    public static void authorizeManageCollectionPolicy(Context context,
            Collection collection) throws AuthorizeException, SQLException
    {
        if (AuthorizeConfiguration.canCollectionAdminManagePolicies())
        {
            AuthorizeManager.authorizeAction(context, collection,
                    Constants.ADMIN);
        }
        else if (AuthorizeConfiguration
                .canCommunityAdminManageCollectionPolicies())
        {
            AuthorizeManager.authorizeAction(context, collection.getCommunities().iterator().next(), Constants.ADMIN);
        }
        else if (!AuthorizeManager.isAdmin(context))
        {
            throw new AuthorizeException(
                    "Only system admin are allowed to manage collection policies");
        }
    }

    /**
     * Is allowed manage (create, remove, edit) community's policies in the
     * current context?
     * 
     * @param context
     *            the DSpace Context Object
     * @param community
     *            the community that the policy refer to
     * @throws AuthorizeException
     *             if the current context (current user) is not allowed to
     *             manage the community's policies
     * @throws SQLException
     *             if a db error occur
     */
    public static void authorizeManageCommunityPolicy(Context context,
            Community community) throws AuthorizeException, SQLException
    {
        if (AuthorizeConfiguration.canCommunityAdminManagePolicies())
        {
            AuthorizeManager.authorizeAction(context, community,
                    Constants.ADMIN);
        }
        else if (!AuthorizeManager.isAdmin(context))
        {
            throw new AuthorizeException(
                    "Only system admin are allowed to manage community policies");
        }
    }

    /**
     * Throw an AuthorizeException if the current user is not a System Admin
     * 
     * @param context
     *            the DSpace Context Object
     * @throws AuthorizeException
     *             if the current user is not a System Admin
     * @throws SQLException
     *             if a db error occur
     */
    public static void requireAdminRole(Context context)
            throws AuthorizeException, SQLException
    {
        if (!AuthorizeManager.isAdmin(context))
        {
            throw new AuthorizeException(
                    "Only system admin are allowed to perform this action");
        }
    }

    /**
     * Is the current user allowed to manage (add, remove, replace) the item's
     * CC License
     * 
     * @param context
     *            the DSpace Context Object
     * @param item
     *            the item that the CC License refer to
     * @throws AuthorizeException
     *             if the current user is not allowed to
     *             manage the item's CC License
     * @throws SQLException
     *             if a db error occur
     */
    public static void authorizeManageCCLicense(Context context, Item item)
            throws AuthorizeException, SQLException
    {
        try
        {
            AuthorizeManager.authorizeAction(context, item, Constants.ADD);
            AuthorizeManager.authorizeAction(context, item, Constants.REMOVE);
        }
        catch (AuthorizeException authex)
        {
            if (AuthorizeConfiguration.canItemAdminManageCCLicense())
            {
                AuthorizeManager
                        .authorizeAction(context, item, Constants.ADMIN);
            }
            else if (AuthorizeConfiguration.canCollectionAdminManageCCLicense())
            {
                AuthorizeManager.authorizeAction(context, ITEM_SERVICE.getParentObject(item), Constants.ADMIN);
            }
            else if (AuthorizeConfiguration.canCommunityAdminManageCCLicense())
            {
                AuthorizeManager.authorizeAction(context, ITEM_SERVICE.getParentObject(item), Constants.ADMIN);
            }
            else
            {
                requireAdminRole(context);
            }
        }
    }

    /**
     * Is the current user allowed to manage (create, remove, edit) the
     * collection's template item?
     * 
     * @param context
     *            the DSpace Context Object
     * @param collection
     *            the collection
     * @throws AuthorizeException
     *             if the current user is not allowed to manage the collection's
     *             template item
     * @throws SQLException
     *             if a db error occur
     */
    public static void authorizeManageTemplateItem(Context context,
            Collection collection) throws AuthorizeException, SQLException
    {
        boolean isAuthorized = COLLECTION_SERVICE.canEditBoolean(context, collection, false);

        if (!isAuthorized
                && AuthorizeConfiguration
                        .canCollectionAdminManageTemplateItem())
        {
            AuthorizeManager.authorizeAction(context, collection,
                    Constants.ADMIN);
        }
        else if (!isAuthorized
                && AuthorizeConfiguration
                        .canCommunityAdminManageCollectionTemplateItem())
        {
            Community parent = collection.getCommunities().iterator().next();
            AuthorizeManager.authorizeAction(context, parent, Constants.ADMIN);
        }
        else if (!isAuthorized && !AuthorizeManager.isAdmin(context))
        {
            throw new AuthorizeException(
                    "You are not authorized to create a template item for the collection");
        }
    }

    /**
     * Can the current user manage (create, remove, edit) the submitters group of
     * the collection?
     * 
     * @param context
     *            the DSpace Context Object
     * @param collection
     *            the collection
     * @throws AuthorizeException
     *             if the current user is not allowed to manage the collection's
     *             submitters group
     * @throws SQLException
     *             if a db error occur
     */
    public static void authorizeManageSubmittersGroup(Context context,
            Collection collection) throws AuthorizeException, SQLException
    {
        if (AuthorizeConfiguration.canCollectionAdminManageSubmitters())
        {
            AuthorizeManager.authorizeAction(context, collection,
                    Constants.ADMIN);
        }
        else if (AuthorizeConfiguration
                .canCommunityAdminManageCollectionSubmitters())
        {
            AuthorizeManager.authorizeAction(context, collection.getCommunities().iterator().next(), Constants.ADMIN);
        }
        else if (!AuthorizeManager.isAdmin(context))
        {
            throw new AuthorizeException(
                    "Only system admin are allowed to manage collection submitters");
        }
    }

    /**
     * Can the current user manage (create, remove, edit) the workflow groups of
     * the collection?
     * 
     * @param context
     *            the DSpace Context Object
     * @param collection
     *            the collection
     * @throws AuthorizeException
     *             if the current user is not allowed to manage the collection's
     *             workflow groups
     * @throws SQLException
     *             if a db error occur
     */
    public static void authorizeManageWorkflowsGroup(Context context,
            Collection collection) throws AuthorizeException, SQLException
    {
        if (AuthorizeConfiguration.canCollectionAdminManageWorkflows())
        {
            AuthorizeManager.authorizeAction(context, collection,
                    Constants.ADMIN);
        }
        else if (AuthorizeConfiguration
                .canCommunityAdminManageCollectionWorkflows())
        {
            AuthorizeManager.authorizeAction(context, collection.getCommunities().iterator().next(), Constants.ADMIN);
        }
        else if (!AuthorizeManager.isAdmin(context))
        {
            throw new AuthorizeException(
                    "Only system admin are allowed to manage collection workflow");
        }
    }

    /**
     * Can the current user create/edit the admins group of the collection?
     * please note that the remove action need a separate check
     * 
     * @see #authorizeRemoveAdminGroup(Context, Collection)
     * 
     * @param context
     *            the DSpace Context Object
     * @param collection
     *            the collection
     * @throws AuthorizeException
     *             if the current user is not allowed to create/edit the
     *             collection's admins group
     * @throws SQLException
     *             if a db error occur
     */
    public static void authorizeManageAdminGroup(Context context,
            Collection collection) throws AuthorizeException, SQLException
    {
        if (AuthorizeConfiguration.canCollectionAdminManageAdminGroup())
        {
            AuthorizeManager.authorizeAction(context, collection,
                    Constants.ADMIN);
        }
        else if (AuthorizeConfiguration
                .canCommunityAdminManageCollectionAdminGroup())
        {
            AuthorizeManager.authorizeAction(context, collection.getCommunities().iterator().next(), Constants.ADMIN);
        }
        else if (!AuthorizeManager.isAdmin(context))
        {
            throw new AuthorizeException(
                    "Only system admin are allowed to manage collection admin");
        }
    }

    /**
     * Can the current user remove the admins group of the collection?
     * please note that the create/edit actions need separate check
     * 
     * @see #authorizeManageAdminGroup(Context, Collection)
     * 
     * @param context
     *            the DSpace Context Object
     * @param collection
     *            the collection
     * @throws AuthorizeException
     *             if the current user is not allowed to remove the
     *             collection's admins group
     * @throws SQLException
     *             if a db error occur
     */
    public static void authorizeRemoveAdminGroup(Context context,
            Collection collection) throws AuthorizeException, SQLException
    {
        List<Community> parentCommunities = collection.getCommunities();
        if (AuthorizeConfiguration
                .canCommunityAdminManageCollectionAdminGroup()
                && parentCommunities != null)
        {
            AuthorizeManager.authorizeAction(context, collection
                    .getCommunities().iterator().next(), Constants.ADMIN);
        }
        else if (!AuthorizeManager.isAdmin(context))
        {
            throw new AuthorizeException(
                    "Only system admin can remove the admin group of a collection");
        }
    }

    /**
     * Can the current user create/edit the admins group of the community?
     * please note that the remove action need a separate check
     * 
     * @see #authorizeRemoveAdminGroup(Context, Collection)
     * 
     * @param context
     *            the DSpace Context Object
     * @param community
     *            the community
     * @throws AuthorizeException
     *             if the current user is not allowed to create/edit the
     *             community's admins group
     * @throws SQLException
     *             if a db error occur
     */
    public static void authorizeManageAdminGroup(Context context,
            Community community) throws AuthorizeException, SQLException
    {
        if (AuthorizeConfiguration.canCommunityAdminManageAdminGroup())
        {
            AuthorizeManager.authorizeAction(context, community,
                    Constants.ADMIN);
        }
        else if (!AuthorizeManager.isAdmin(context))
        {
            throw new AuthorizeException(
                    "Only system admin are allowed to manage community admin");
        }
    }

    /**
     * Can the current user remove the admins group of the community?
     * please note that the create/edit actions need separate check
     * 
     * @see #authorizeManageAdminGroup(Context, Community)
     * 
     * @param context
     *            the DSpace Context Object
     * @param community
     *            the community
     * @throws AuthorizeException
     *             if the current user is not allowed to remove the
     *             collection's admins group
     * @throws SQLException
     *             if a db error occur
     */
    public static void authorizeRemoveAdminGroup(Context context,
            Community community) throws SQLException, AuthorizeException
    {
        Community parentCommunity = (Community) COMMUNITY_SERVICE.getParentObject(community);
        if (AuthorizeConfiguration.canCommunityAdminManageAdminGroup()
                && parentCommunity != null)
        {
            AuthorizeManager.authorizeAction(context, parentCommunity,
                    Constants.ADMIN);
        }
        else if (!AuthorizeManager.isAdmin(context))
        {
            throw new AuthorizeException(
                    "Only system admin can remove the admin group of the community");
        }
    }

    /**
     * Can the current user remove or edit the supplied policy?
     * 
     * @param c
     *            the DSpace Context Object
     * @param rp
     *            a resource policy
     * @throws AuthorizeException
     *             if the current context (current user) is not allowed to
     *             remove/edit the policy
     * @throws SQLException
     *             if a db error occur
     */
    public static void authorizeManagePolicy(Context c, ResourcePolicy rp)
            throws SQLException, AuthorizeException
    {
        DSpaceServiceFactory serviceFactory = DSpaceServiceFactory.getInstance();
        switch (rp.getResourceType())
        {

        case Constants.BITSTREAM:
            authorizeManageBitstreamPolicy(c, serviceFactory.getBitstreamService().find(c, rp
                    .getResourceID()));
            break;
        case Constants.BUNDLE:
            authorizeManageBundlePolicy(c, serviceFactory.getBundleService().find(c, rp.getResourceID()));
            break;

        case Constants.ITEM:
            authorizeManageItemPolicy(c, serviceFactory.getItemService().find(c, rp.getResourceID()));
            break;
        case Constants.COLLECTION:
            authorizeManageCollectionPolicy(c, serviceFactory.getCollectionService().find(c, rp
                    .getResourceID()));
            break;
        case Constants.COMMUNITY:
            authorizeManageCommunityPolicy(c, serviceFactory.getCommunityService().find(c, rp
                    .getResourceID()));
            break;

        default:
            requireAdminRole(c);
            break;
        }
    }

    /**
     * Can the current user withdraw the item?
     * 
     * @param context
     *            the DSpace Context Object
     * @param item
     *            the item
     * @throws SQLException
     *             if a db error occur
     * @throws AuthorizeException
     *             if the current user is not allowed to perform the item
     *             withdraw
     */
    public static void authorizeWithdrawItem(Context context, Item item)
            throws SQLException, AuthorizeException
    {
        boolean authorized = false;
        if (AuthorizeConfiguration.canCollectionAdminPerformItemWithdrawn())
        {
            authorized = AuthorizeManager.authorizeActionBoolean(context, item
                    .getOwningCollection(), Constants.ADMIN);
        }
        else if (AuthorizeConfiguration.canCommunityAdminPerformItemWithdrawn())
        {
            authorized = AuthorizeManager
                    .authorizeActionBoolean(context, item.getOwningCollection().getCommunities().iterator().next(), Constants.ADMIN);
        }

        if (!authorized)
        {
            authorized = AuthorizeManager.authorizeActionBoolean(context, item
                    .getOwningCollection(), Constants.REMOVE, false);
        }

        // authorized
        if (!authorized)
        {
            throw new AuthorizeException(
                    "To withdraw item must be COLLECTION_ADMIN or have REMOVE authorization on owning Collection");
        }
    }

    /**
    * Can the current user reinstate the item?
    * 
    * @param context
    *            the DSpace Context Object
    * @param item
    *            the item
    * @throws SQLException
    *             if a db error occur
    * @throws AuthorizeException
    *             if the current user is not allowed to perform the item
    *             reinstatement
    */
    public static void authorizeReinstateItem(Context context, Item item)
            throws SQLException, AuthorizeException
    {
        List<Collection> colls = item.getCollections();

        for (Collection coll : colls)
        {
            if (!AuthorizeConfiguration
                    .canCollectionAdminPerformItemReinstatiate())
            {
                if (AuthorizeConfiguration
                        .canCommunityAdminPerformItemReinstatiate()
                        && AuthorizeManager.authorizeActionBoolean(context,
                        coll.getCommunities().iterator().next(), Constants.ADMIN))
                {
                    // authorized
                }
                else
                {
                    AuthorizeManager.authorizeAction(context, coll,
                            Constants.ADD, false);
                }
            }
            else
            {
                AuthorizeManager.authorizeAction(context, coll,
                        Constants.ADD);
            }
        }
    }
}