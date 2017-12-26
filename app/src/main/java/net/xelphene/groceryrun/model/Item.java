package net.xelphene.groceryrun.model;

/**
 * Created by steve on 8/2/17.
 */

public class Item {
    public final long id;
    public final String description;

    public Item(long itemId, String itemDescription) {
        id = itemId;
        description = itemDescription;
    }
}
