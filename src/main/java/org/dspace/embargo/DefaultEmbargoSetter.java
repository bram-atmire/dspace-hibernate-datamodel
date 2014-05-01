/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.embargo;

import java.sql.SQLException;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.dspace.authorize.*;
import org.dspace.authorize.service.ResourcePolicyService;
import org.dspace.content.*;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.core.Constants;
import org.dspace.eperson.Group;
import org.dspace.eperson.service.GroupService;
import org.dspace.factory.DSpaceServiceFactory;
import org.dspace.license.CreativeCommons;

/**
 * Default plugin implementation of the embargo setting function.
 * The parseTerms() provides only very rudimentary terms logic - entry
 * of a configurable string (in terms field) for 'unlimited' embargo, otherwise
 * a standard ISO 8601 (yyyy-mm-dd) date is assumed. Users are encouraged 
 * to override this method for enhanced functionality.
 *
 * @author Larry Stone
 * @author Richard Rodgers
 */
public class DefaultEmbargoSetter implements EmbargoSetter
{
    protected static final GroupService GROUP_SERVICE = DSpaceServiceFactory.getInstance().getGroupService();
    protected static final ResourcePolicyService RESOURCE_POLICY_SERVICE = DSpaceServiceFactory.getInstance().getResourcePolicyService();
    protected String termsOpen = null;
	
    public DefaultEmbargoSetter()
    {
        super();
        termsOpen = ConfigurationManager.getProperty("embargo.terms.open");
    }
    
    /**
     * Parse the terms into a definite date. Terms are expected to consist of
     * either: a token (value configured in 'embargo.terms.open' property) to indicate
     * indefinite embargo, or a literal lift date formatted in ISO 8601 format (yyyy-mm-dd)
     * 
     * @param context the DSpace context
     * @param item the item to embargo
     * @param terms the embargo terms
     * @return parsed date in DCDate format
     */
    public DCDate parseTerms(Context context, Item item, String terms)
        throws SQLException, AuthorizeException
    {
    	if (terms != null && terms.length() > 0)
    	{
    		if (termsOpen.equals(terms))
            {
                return EmbargoManager.FOREVER;
            }
            else
            {
                return new DCDate(terms);
            }
    	}
        return null;
    }

    /**
     * Enforce embargo by turning off all read access to bitstreams in
     * this Item.
     *
     * @param context the DSpace context
     * @param item the item to embargo
     */
    public void setEmbargo(Context context, Item item)
        throws SQLException, AuthorizeException
    {
        DCDate liftDate = EmbargoManager.getEmbargoTermsAsDate(context, item);
        for (Bundle bn : item.getBundles())
        {
            // Skip the LICENSE and METADATA bundles, they stay world-readable
            String bnn = bn.getName();
            if (!(bnn.equals(Constants.LICENSE_BUNDLE_NAME) || bnn.equals(Constants.METADATA_BUNDLE_NAME) || bnn.equals(CreativeCommons.CC_BUNDLE_NAME)))
            {
                //AuthorizeManager.removePoliciesActionFilter(context, bn, Constants.READ);
                generatePolicies(context, liftDate.toDate(), null, bn, item.getOwningCollection());
                for (Bitstream bs : bn.getBitstreams())
                {
                    //AuthorizeManager.removePoliciesActionFilter(context, bs, Constants.READ);
                    generatePolicies(context, liftDate.toDate(), null, bs, item.getOwningCollection());
                }
            }
        }
    }

    protected void generatePolicies(Context context, Date embargoDate,
                                        String reason, DSpaceObject dso, Collection owningCollection) throws SQLException, AuthorizeException {

        // add only embargo policy
        if(embargoDate!=null){

            List<Group> authorizedGroups = AuthorizeManager.getAuthorizedGroups(context, owningCollection, Constants.DEFAULT_ITEM_READ);

            // look for anonymous
            boolean isAnonymousInPlace=false;
            for(Group g : authorizedGroups){
                if(g.getID()==0){
                    isAnonymousInPlace=true;
                }
            }

            if(!isAnonymousInPlace){
                // add policies for all the groups
                for(Group g : authorizedGroups){
                    ResourcePolicy rp = AuthorizeManager.createOrModifyPolicy(null, context, null, g, null, embargoDate, Constants.READ, reason, dso);
                    if(rp!=null)
                        RESOURCE_POLICY_SERVICE.update(context, rp);
                }

            }
            else{
                // add policy just for anonymous
                ResourcePolicy rp = AuthorizeManager.createOrModifyPolicy(null, context, null, GROUP_SERVICE.find(context, 0), null, embargoDate, Constants.READ, reason, dso);
                if(rp!=null)
                    RESOURCE_POLICY_SERVICE.update(context, rp);
            }
        }

    }





    /**
     * Check that embargo is properly set on Item: no read access to bitstreams.
     *
     * @param context the DSpace context
     * @param item the item to embargo
     */
    public void checkEmbargo(Context context, Item item)
        throws SQLException, AuthorizeException
    {
        for (Bundle bn : item.getBundles())
        {
            // Skip the LICENSE and METADATA bundles, they stay world-readable
            String bnn = bn.getName();
            if (!(bnn.equals(Constants.LICENSE_BUNDLE_NAME) || bnn.equals(Constants.METADATA_BUNDLE_NAME) || bnn.equals(CreativeCommons.CC_BUNDLE_NAME)))
            {
                // don't report on "TEXT" or "THUMBNAIL" bundles; those
                // can have READ long as the bitstreams in them do not.
                if (!(bnn.equals("TEXT") || bnn.equals("THUMBNAIL")))
                {
                    // check for ANY read policies and report them:
                    for (ResourcePolicy rp : AuthorizeManager.getPoliciesActionFilter(context, bn, Constants.READ))
                    {
                        System.out.println("CHECK WARNING: Item "+item.getHandle(context)+", Bundle "+bn.getName()+" allows READ by "+
                          ((rp.getEPerson() == null) ? "Group "+rp.getGroup().getName() :
                                                      "EPerson "+rp.getEPerson().getFullName()));
                    }
                }

                for (Bitstream bs : bn.getBitstreams())
                {
                    for (ResourcePolicy rp : AuthorizeManager.getPoliciesActionFilter(context, bs, Constants.READ))
                    {
                        System.out.println("CHECK WARNING: Item "+item.getHandle(context)+", Bitstream "+bs.getName()+" (in Bundle "+bn.getName()+") allows READ by "+
                          ((rp.getEPerson() == null) ? "Group "+rp.getGroup().getName() :
                                                      "EPerson "+rp.getEPerson().getFullName()));
                    }
                }
            }
        }
    }
}