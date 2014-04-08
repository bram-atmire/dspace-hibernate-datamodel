package org.dspace.workflow;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.InProgressSubmissionManager;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;

import java.sql.SQLException;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 7/04/14
 * Time: 15:52
 */
public interface WorkflowItemManager extends InProgressSubmissionManager<WorkflowItem> {

    public WorkflowItem create(Context context, Item item, Collection collection) throws SQLException, AuthorizeException;

    public WorkflowItem find(Context c, int id) throws SQLException;

    public List<WorkflowItem> findAll(Context c) throws SQLException;

    public List<WorkflowItem> findByEPerson(Context context, EPerson ep) throws SQLException;

    public List<WorkflowItem> findByPooledTasks(Context context, EPerson ePerson) throws SQLException;

    public List<WorkflowItem> findByCollection(Context context, Collection c) throws SQLException;

    public WorkflowItem findByItem(Context context, Item i) throws SQLException;
}