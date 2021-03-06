package org.dspace.versioning;

import org.dspace.content.Item;
import org.dspace.eperson.EPerson;

import javax.persistence.*;
import java.util.Date;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 8/05/14
 * Time: 09:31
 */
@Entity
@Table(name="versionitem", schema = "public")
public class Version {

    @Id
    @Column(name="versionitem_id")
    @GeneratedValue(strategy = GenerationType.AUTO ,generator="versionitem_seq")
    @SequenceGenerator(name="versionitem_seq", sequenceName="versionitem_seq", allocationSize = 1, initialValue = 1)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @Column(name = "version_number")
    private int versionNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eperson_id")
    private EPerson ePerson;

    @Column(name = "version_date")
    private Date versionDate;

    @Column(name = "version_summary", length = 255)
    private String summary;

    @ManyToOne
    @JoinColumn(name = "versionhistory_id")
    private VersionHistory versionHistory;

    public int getId() {
        return id;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(int version_number) {
        this.versionNumber = version_number;
    }

    public EPerson getePerson() {
        return ePerson;
    }

    public void setePerson(EPerson ePerson) {
        this.ePerson = ePerson;
    }

    public Date getVersionDate() {
        return versionDate;
    }

    public void setVersionDate(Date versionDate) {
        this.versionDate = versionDate;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String versionSummary) {
        this.summary = versionSummary;
    }

    public VersionHistory getVersionHistory() {
        return versionHistory;
    }

    public void setVersionHistory(VersionHistory versionHistory) {
        this.versionHistory = versionHistory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
        {
            return true;
        }

        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        Version that = (Version) o;
        return getId() == that.getId();
    }

    @Override
    public int hashCode() {
        int hash=7;
        hash=79*hash+ this.getId();
        return hash;
    }
}
