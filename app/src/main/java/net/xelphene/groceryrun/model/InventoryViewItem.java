package net.xelphene.groceryrun.model;

/**
 * Created by steve on 7/27/17.
 */

public class InventoryViewItem {
    private long mId;
    private String mDescription;
    private boolean mNeeded;
    private String mShopNote;

    public InventoryViewItem(long id, String description, boolean needed, String shopNote) {
        mId=id;
        mDescription=description;
        mNeeded=needed;
        mShopNote=shopNote;
    }

    public long getId() {
        return mId;
    }

    public String getDescription() {
        return mDescription;
    }

    public boolean getNeeded() {
        return mNeeded;
    }

    public String getShopNote() {
        return mShopNote;
    }

    public String toString() {
        return "[" + getId() + "] " + getDescription();
    }
}
