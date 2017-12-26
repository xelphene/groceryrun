package net.xelphene.groceryrun.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

/**
 * net.xelphene.groceryrun.model.InventoryEditList is a container for InventoryItems which keeps track of
 * adds/edits/deletions. InventoryActivity operates on one of these.
 * When it comes time to save, LocalDatabase can use one of these to do
 * the minimum I/O necessary to save all changes to the inventory.
 */

public class InventoryEditList
    implements Parcelable
{
    private ArrayList<InventoryEditItem> list;
    private boolean orderChanged;
    private static final String TAG = "InventoryEditList";
    private ArrayList<InventoryEditItem> deletions;

    public InventoryEditList() {
        list = new ArrayList<>();
        deletions = new ArrayList<>();
        orderChanged = false;
    }

    /**
     * Add a brand new item to inventory at a specific position. This will
     * be inserted into the database (eventually).
     * @param index the position at which the new item should appear
     * @param item the item to add
     */
    public void add(int index, InventoryEditItem item) {
        if( item.getExists() ) {
            Log.e(TAG,"item already exists??? changing it to non-existent: "+item);
            item.setExists(false);
        }
        list.add(index,item);
        orderChanged=true;
    }

    public void addLoaded(InventoryEditItem item) {
        if( ! item.getExists() ) {
            Log.e(TAG,"item loaded but exists flag not yet???");
            return;
        }
        list.add(item);
    }

    public void remove(int index) {
        InventoryEditItem item = list.get(index);
        if( item!=null ) {
            list.remove(index);
            deletions.add(item);
            /* we don't set orderChanged here because it isn't necessary.
             * order still makes sense. they're just arbitrary ints.
             */
        }
    }

    public ArrayList<InventoryEditItem> getNewItems() {
        ArrayList<InventoryEditItem> newItems = new ArrayList<>();
        for( InventoryEditItem item : list ) {
            if( ! item.getExists() ) {
                newItems.add(item);
            }
        }
        return newItems;
    }

    public ArrayList<InventoryEditItem> getDeletedItems() {
        return deletions;
    }

    public void swap(int srcIndex, int dstIndex) {
        //Log.d(TAG,"swap...");
        Collections.swap(list, srcIndex, dstIndex);
        orderChanged=true;
    }

    public int size() {
        return list.size();
    }

    public InventoryEditItem get(int index) {
        return list.get(index);
    }

    public ArrayList<InventoryEditItem> getList() { return list; }

    public int indexOf(InventoryEditItem afterItem) {
        return list.indexOf(afterItem);
    }

    public boolean getOrderChanged() {
        return orderChanged;
    }

    /**
     * Call this once the database has committed all changes (deleted all
     * deleted items, updated order if necessary, inserted new items and
     * assigned their IDs, etc). Should only be called from within LocalDatabase.
     */
    public void saveComplete() {
        deletions.clear();
        orderChanged=false;
        for( InventoryEditItem item : list ) {
            item.itemSaved();
        }
    }

    public void logContents() {
        Log.d(TAG,"START logContents()");
        Log.d(TAG,"  wanted items:");
        for( InventoryEditItem item : list ) {
            Log.d(TAG,"    "+item);
        }
        Log.d(TAG,"  deleted items:");
        for( InventoryEditItem item : deletions) {
            Log.d(TAG,"    "+item);
        }
        Log.d(TAG,"END logContents()");
    }

    public int describeContents() {
        // we have no file descriptors or other special things
        return 0;
    }

    /* TODO: need to store other items, like deletions and reorder flag
       and also restore it in CREATOR
     */

    public void writeToParcel(Parcel dest, int flags){
        Log.d(TAG,"in writeToParcel");
        dest.writeTypedList(list);
        Log.d(TAG,"writeToParcel complete");
    }

    public static final Parcelable.Creator<InventoryEditList> CREATOR =
            new Parcelable.Creator<InventoryEditList>() {
                public InventoryEditList createFromParcel(Parcel src) {
                    InventoryEditList inventoryEditList = new InventoryEditList();
                    src.readTypedList(inventoryEditList.list, InventoryEditItem.CREATOR);
                    Log.d(TAG,"createFromParcel about to return:");
                    inventoryEditList.logContents();
                    return inventoryEditList;
                }

                public InventoryEditList[] newArray(int size) {
                    return new InventoryEditList[size];
                }
            };
}
