package net.xelphene.groceryrun.model;

import android.util.Log;

import net.xelphene.groceryrun.db.LocalDatabase;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by steve on 7/26/17.
 */

public class Model {
    private static final Model ourInstance = new Model();

    public static Model getInstance() {
        return ourInstance;
    }
    private static final String TAG = "Model";
    private HashSet<ModelUpdatedListener> updateListeners;

    private Model() {
        updateListeners = new HashSet<>();
    }

    ////////////////////////////////////////////////////////////////

    public ArrayList<InventoryViewItem> getInventoryViewArrayList() {
        Log.e(TAG,"getInventoryViewArrayList");
        return LocalDatabase.getInstance().getInventoryViewArrayList();
    }

    public ArrayList<ShoppingListItem> getShoppingList() {
        return LocalDatabase.getInstance().getShoppingList();
    }

    public ArrayList<Item> getItemsInShoppingOrder() {
        return LocalDatabase.getInstance().getItemsInShoppingOrder();
    }

    public void setShoppingOrder(ArrayList<Item> items) {
        LocalDatabase.getInstance().setShoppingOrder(items);
    }

    public InventoryViewItem getInventoryViewItem( long itemId) {
        return LocalDatabase.getInstance().getInventoryViewItem(itemId);
    }

    ///////////////////////////////////////////////////////////////////

    public InventoryEditList getInventoryEditList() {
        return LocalDatabase.getInstance().getInventoryEditList();
    }

    public void saveInventoryEditList( InventoryEditList list ) {
        LocalDatabase.getInstance().saveInventoryEditList(list);
        fireModelUpdated();
    }

    /////////////////////////////////////////////////////////////

    public void itemNotNeeded(long itemId) {
        //LocalDatabase.getInstance().itemPurchased(itemId);
        LocalDatabase.getInstance().setItemNeed(itemId, false, null);
        fireModelUpdated();
    }

    public void itemNeeded(long itemId) {
        LocalDatabase.getInstance().setItemNeed(itemId, true, "");
        fireModelUpdated();
    }

    public void itemNeeded(long itemId, String shopNote) {
        LocalDatabase.getInstance().setItemNeed(itemId, true, shopNote);
        fireModelUpdated();
    }

    public long addTemporaryItem(String description, String shopNote) {
        long id = LocalDatabase.getInstance().addTemporaryItem(description, shopNote);
        return id;
    }

    public long addInventoryItem(String description, String shopNote, boolean needed) {
        long id = LocalDatabase.getInstance().addInventoryItem(description, shopNote, needed);
        return id;
    }

    ///////////////////////////////////////////////////////////////


    public void addModelUpdatedListener(ModelUpdatedListener listener) {
        Log.d(TAG,"addModelUpdatedListener: "+listener.toString());
        updateListeners.add(listener);
    }

    public void removeModelUpdatedListener(ModelUpdatedListener listener){
        Log.d(TAG,"removeModelUpdatedListener: "+listener.toString());
        updateListeners.remove(listener);
    }

    private void fireModelUpdated() {
        for( ModelUpdatedListener listener : updateListeners ) {
            listener.modelUpdated();
        }
    }

}
