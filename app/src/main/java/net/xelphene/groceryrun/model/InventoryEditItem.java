package net.xelphene.groceryrun.model;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * Created by steve on 4/28/17.
 */

public class InventoryEditItem
        implements Parcelable
{
    private String description;
    private long id;
    private boolean exists;
    private boolean temporary;
    private boolean descriptionUpdated;

    private boolean needed;
    private boolean neededUpdated;

    public InventoryEditItem(long itemId, String itemDescription) {
        description = itemDescription;
        id = itemId;
        exists = true;
        temporary = false;
        descriptionUpdated = false;
    }

    public InventoryEditItem(String itemDescription) {
        description = itemDescription;
        id = 0;
        exists = false;
        temporary = false;
        descriptionUpdated = false;
    }

    public boolean isTemporary() { return temporary; }

    public void setTemporary(boolean isTemporary) {
        temporary = isTemporary;
    }

    public String getDescription(){ return description; }

    public void setDescription(String newDescription) {
        descriptionUpdated = true;
        description = newDescription;
    }

    public void setNeeded(boolean neededFlag) {
        needed = neededFlag;
        neededUpdated = true;
    }

    public boolean getNeeded() {
        return needed;
    }

    public void toggleNeeded() {
        if( getNeeded() ) {
            setNeeded(false);
        } else {
            setNeeded(true);
        }
    }

    public void itemSaved() {
        descriptionUpdated = false;
        neededUpdated = false;
    }

    public boolean getDescriptionUpdated() {
        return descriptionUpdated;
    }

    public boolean getNeededUpdated() {
        return neededUpdated;
    }

    public long getId() { return id;}

    public void setId(long newId) {
        id = newId;
    }

    public boolean getExists() { return exists; }

    public boolean exists() { return exists;}

    public void setExists(boolean newExists) {
        exists = newExists;
    }

    public String toString() {
        if( exists ) {
            return "[" + id + "] " + getDescription();
        } else {
            return "[N] "+getDescription();
        }
    }

    // TODO: set the descriptionUpdated flag in the Parcle/unparcle process
    // TODO: set the needed and neededUpdated flag in the parcle/unparcle process

    public static final Parcelable.Creator<InventoryEditItem> CREATOR =
            new Parcelable.Creator<InventoryEditItem>() {

                public InventoryEditItem createFromParcel(Parcel in) {
                    long id = in.readLong();
                    String description = in.readString();
                    int exists = in.readInt();
                    if( exists==0 ) {
                        return new InventoryEditItem(description);
                    } else {
                        return new InventoryEditItem(id, description);
                    }
                }

                public InventoryEditItem[] newArray(int size) {
                    return new InventoryEditItem[size];
                }
            };

    public int describeContents() {
        // we have no file descriptors or other special things
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(description);
        if( exists ) {
            dest.writeInt(1);
        } else {
            dest.writeInt(0);
        }
    }
}
