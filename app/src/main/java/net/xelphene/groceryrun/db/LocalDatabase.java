package net.xelphene.groceryrun.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import net.xelphene.groceryrun.model.InventoryEditItem;
import net.xelphene.groceryrun.model.InventoryEditList;
import net.xelphene.groceryrun.model.Item;
import net.xelphene.groceryrun.model.ShoppingListItem;
import net.xelphene.groceryrun.model.InventoryViewItem;

import java.util.ArrayList;

/**
 * Created by steve on 6/23/17.
 */

public class LocalDatabase {
    // singleton static members
    private static final LocalDatabase ourInstance = new LocalDatabase();
    public static LocalDatabase getInstance() { return ourInstance; }

    // normal members
    private SQLiteDatabase db;
    public static final String DB_PATH = "/storage/emulated/0/_me/groceryrun.db";
    private static final String TAG = "LocalDatabase";

    private LocalDatabase() {
        db=null;
    }

    public void open() {
        if( db!=null ) {
            // already open
            return;
        }
        Log.d(TAG,"opening");
        db = SQLiteDatabase.openDatabase(
                DB_PATH,
                null,
                SQLiteDatabase.OPEN_READWRITE
        );
    }

    public void close() {
        if( db!=null ) {
            Log.d(TAG,"closing");
            db.close();
            db=null;
        }
    }

    public InventoryEditList getInventoryEditList() {
        open();
        InventoryEditList inventory = new InventoryEditList();
        //ArrayList<InventoryEditItem> inventory = new ArrayList<InventoryEditItem>();
        //Log.d(TAG,"opened. loading...");

        Cursor cursor = db.rawQuery(
                "SELECT InventoryItemID, description, temporary, needed FROM " +
                "InventoryItem WHERE NOT temporary ORDER BY sortOrder ASC",null);
        while(cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String description = cursor.getString(1);
            boolean temporary = cursor.getInt(2) !=0;
            boolean needed = cursor.getInt(3) !=0;
            InventoryEditItem item = new InventoryEditItem(id,description);
            item.setTemporary(temporary);
            item.setNeeded(needed);
            item.itemSaved(); // clear InventoryEditItem's internal "need saving" flags
            //inventory.add(item);
            inventory.addLoaded(item);
            //Log.d(TAG,"loaded: "+item.toString());
        }
        cursor.close();

        return inventory;
    }


    public ArrayList<InventoryViewItem> getInventoryViewArrayList() {
        open();
        ArrayList<InventoryViewItem> items = new ArrayList<>();

        Cursor cursor = db.rawQuery(
                "SELECT InventoryItemId, description, needed, shopNote FROM InventoryItem " +
                "WHERE NOT temporary ORDER BY sortOrder ASC",
                null);
        while(cursor.moveToNext()) {
            long id = cursor.getLong(0);
            String description = cursor.getString(1);
            boolean needed = cursor.getInt(2) != 0;
            String shopNote = cursor.getString(3);
            InventoryViewItem item = new InventoryViewItem(
                    id, description, needed, shopNote
            );
            items.add(item);
        }
        cursor.close();

        return items;
    }

    public InventoryViewItem getInventoryViewItem(long itemId) {
        open();
        String[] args = {Long.toString(itemId)};
        InventoryViewItem item=null;

        Cursor cursor = db.rawQuery(
                "SELECT InventoryItemId, description, needed, shopNote FROM InventoryItem "+
                "WHERE InventoryItemId = ?",
                args
        );
        if( cursor.moveToNext() ) {
            long id = cursor.getLong(0);
            String description = cursor.getString(1);
            boolean needed = cursor.getInt(2) != 0;
            String shopNote = cursor.getString(3);
            item = new InventoryViewItem(
                    id, description, needed, shopNote
            );
        }
        cursor.close();

        return item;
    }

    public ArrayList<Item> getItemsInShoppingOrder() {
        open();
        ArrayList<Item> items = new ArrayList<>();

        Cursor cursor = db.rawQuery(
                "SELECT InventoryItemId, description FROM InventoryItem " +
                "ORDER BY shopOrder ASC",null);
        while( cursor.moveToNext() ) {
            long id = cursor.getLong(0);
            String description = cursor.getString(1);
            Item item = new Item(id, description);
            items.add(item);
        }
        cursor.close();
        return items;
    }

    public void setShoppingOrder(ArrayList<Item> items) {
        for( int i=0; i<items.size(); i++ ) {
            Item item = items.get(i);
            Log.d(TAG," shopOrder: "+item.description+" -> "+i);
            ContentValues values = new ContentValues();
            values.put("shopOrder", i);
            String[] args = {
                    Long.toString(item.id)
            };
            db.update(
                    "InventoryItem",
                    values,
                    "InventoryItemID=?",
                    args
            );
        }
    }

    public ArrayList<ShoppingListItem> getShoppingList() {
        Log.d(TAG,"getShoppingList(): strart");

        open();
        ArrayList<ShoppingListItem> shoppingList = new ArrayList<>();

        /* we say "WHERE needed OR temporary" below since there is no such thing as a temporary
        item that we don't currently need. Temporary items are always needed; they're deleted as
        soon as they're marked purchased. However if there is some bug which causes items to be
        inserted with temporary=1 AND needed=0 then the item would never appear in the UI and
        would just be stuck in the database. This way the item will always at least show up in
        the shopping list where it can at least be deleted.
        There should probably be a more normalized DB structure in the future so that this
        situation isn't even possible.
         */

        Cursor cursor = db.rawQuery(
                "SELECT InventoryItemId, description, temporary, shopNote "+
                "FROM InventoryItem "+
                "WHERE needed OR temporary "+
                "ORDER BY shopOrder ASC",
                null
        );
        while(cursor.moveToNext()) {
            long id = cursor.getLong(0);
            String description = cursor.getString(1);
            boolean temporary = cursor.getInt(2) !=0;
            String shopNote = cursor.getString(3);

            ShoppingListItem item = new ShoppingListItem(id, description, shopNote);
            //item.setTemporary(temporary);
            shoppingList.add(item);
            Log.d(TAG," loaded "+item);
        }
        cursor.close();

        return shoppingList;
    }

    public void setItemNeed(long inventoryItemID, boolean needed, String shopNote) {
        // TODO: handle temporary items
        String[] args = {Long.toString(inventoryItemID)};

        /* see if the item exists and load its temporary fag */
        boolean temporary=false;
        boolean found=false;
        Cursor cursor = db.rawQuery(
                "SELECT temporary FROM InventoryItem " +
                "WHERE InventoryItemID=?",
                args
        );
        if( cursor.moveToNext() ) {
            temporary = cursor.getInt(0) != 0;
            found=true;
        }
        cursor.close();

        if( ! found ) {
            Log.e(TAG,"Asked to delete an item with id "+inventoryItemID+" which does not exist");
            return;
        }

        String[] where = {
                Long.toString(inventoryItemID)
        };

        if( needed || ! temporary ) {
            // if it is kept in inventory or we still need it then just update it
            ContentValues newValues = new ContentValues();
            if (needed) {
                newValues.put("needed", 1);
                newValues.put("shopNote", shopNote);
            } else {
                newValues.put("needed", 0);
                newValues.put("shopNote", "");
            }
            db.update(
                    "InventoryItem", newValues,
                    "InventoryItemID=?", where
            );
        } else {
            // temporary items that have been purchased just get deleted
            db.delete("InventoryItem","InventoryItemID=?", where);
        }
    }

    public long addTemporaryItem(String description, String shopNote) {
        open();
        ContentValues v = new ContentValues();
        v.put("description", description);
        v.put("temporary", true);
        v.put("shopNote", shopNote);
        long id = db.insert("InventoryItem",null,v);
        return id;
    }

    public long addInventoryItem(String description, String shopNote, boolean needed) {
        open();
        ContentValues v = new ContentValues();
        v.put("description", description);
        v.put("temporary", false);
        v.put("shopNote", shopNote);
        v.put("needed", needed);
        long id = db.insert("InventoryItem",null,v);
        return id;

    }

    public void saveInventoryEditList(InventoryEditList list) {
        open();
        Log.d(TAG,"saving an InventoryEditList...");

        // INSERT new items
        for(InventoryEditItem item : list.getNewItems()) {
            ContentValues v = new ContentValues();
            v.put("description", item.getDescription());
            v.put("temporary",false);
            long id = db.insert("InventoryItem",null,v);
            item.setId(id);
            item.setExists(true);
            Log.d(TAG,"item inserted: "+item.toString());
        }

        // DELETE deleted items
        for(InventoryEditItem item : list.getDeletedItems()) {
            if( item.getExists() ) {
                String[] args = {Long.toString(item.getId())};
                db.delete("InventoryItem", "InventoryItemID=?", args);
                Log.d(TAG, "item deleted: " + item);
            } else {
                Log.e(TAG,"asked to delete an item that doesn't exist: "+item);
            }
        }

        // UPDATE changed description fields
        for( InventoryEditItem item : list.getList()) {
            if( item.getDescriptionUpdated() || item.getNeededUpdated() ) {
                ContentValues values = new ContentValues();

                if( item.getDescriptionUpdated() ) {
                    Log.d(TAG,"update description: "+item.toString());
                    values.put("description", item.getDescription());
                }
                if( item.getNeededUpdated() ) {
                    Log.d(TAG,"update needed flag: "+item.toString());
                    if( item.getNeeded() ) {
                        values.put("needed", 1);
                    } else {
                        values.put("needed",0 );
                    }
                }

                String[] args = {
                        Long.toString(item.getId())
                };

                db.update(
                        "InventoryItem",
                        values,
                        "InventoryItemID=?",
                        args
                );
            }

        }

        // UPDATE order on everything, if necessary
        if( list.getOrderChanged()) {
            Log.d(TAG,"reordering...");
            for(int i=0; i<list.size(); i++) {
                InventoryEditItem item = list.get(i);
                if( item.getExists() ) {
                    ContentValues values = new ContentValues();
                    values.put("sortOrder", i);
                    String[] args = {
                            Long.toString(list.get(i).getId())
                    };
                    db.update(
                            "InventoryItem",
                            values,
                            "InventoryItemID=?",
                            args
                    );
                    Log.d(TAG,"  set item "+item+" to order "+i);
                } else {
                    Log.e(TAG,"  asked to update order for an item with no ID: "+item);
                }
            }
        }

        Log.d(TAG,"save complete");
        list.saveComplete();
    }
}
