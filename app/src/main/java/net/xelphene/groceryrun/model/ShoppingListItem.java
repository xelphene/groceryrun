package net.xelphene.groceryrun.model;

/**
 * Created by steve on 6/30/17.
 */

public class ShoppingListItem {
    private long id;
    private String description;
    private String shopNote;

    public ShoppingListItem(long itemId, String itemDescription, String itemShopNote) {
        id=itemId;
        description=itemDescription;
        shopNote=itemShopNote;

    }

    public String getShopNote() { return shopNote; }
    public long getId() { return id; }
    public String getDescription() { return description; }

    public String toString() {
        return "["+id+"] "+description;
    }
}
