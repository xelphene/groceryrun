package net.xelphene.groceryrun;

import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import net.xelphene.groceryrun.model.InventoryEditItem;
import net.xelphene.groceryrun.model.InventoryViewItem;
import net.xelphene.groceryrun.model.Model;


/**
 * Created by steve on 6/2/17.
 * <p>
 * I would have preferred to make this a non-static inner class within InventoryActivity,
 * however that is impossible because the adapter's onCreateViewHolder must return a
 * RecyclerAdapter.ViewHolder instance. RecyclerView.ViewHolder is a static inner class, so our
 * subclass of it must be as well in order for our onCreateViewHolder override to have a
 * compatible return type. However, it is not possible to have a static inner subclass of a
 * non-static inner class. In otherwords, this is illegal:
 *
 * public class InventoryActivity {
 *     public class InventoryRecyclerAdapter {
 *          public static class ViewHolder {
 *                 ^^ ERROR: Inner classes cannot have static declarations.
 *          }
 *     }
 * }
 *
 * We could make InventoryRecyclerAdapter a static inner of InventoryActivity, but that
 * wouldn't really be of any benefit over having it completely separate.
 */

public class InventoryRecyclerAdapter
        extends RecyclerView.Adapter<InventoryRecyclerAdapter.ViewHolder>
{
    private final static String TAG = "InventoryRecycAdapter";
    private InventoryActivity activity;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View mRowView;
        public TextView descriptionTextView;
        public ImageView dragHandle;
        public ImageView addBelowButton;
        public ImageView editItemButton;
        public CheckBox checkBox;

        public ViewHolder(View rowView) {
            super(rowView);
            mRowView = rowView;
        }
    }

    public InventoryRecyclerAdapter(InventoryActivity inventoryActivity) {
        activity = inventoryActivity;
    }

    public void modeChanged() {
        notifyDataSetChanged();

        // the following used to work but now doesn't. not sure why.
        // it only calls onBindViewHolder for the first item. .size() is correct.
        //notifyItemChanged(0,activity.getInventoryEditList().size());

    }

    @Override
    public InventoryRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View rowView = inflater.inflate(R.layout.inventory_row, parent, false);

        ViewHolder holder = new ViewHolder(rowView);
        holder.descriptionTextView = (TextView) rowView.findViewById(R.id.description);
        holder.dragHandle = (ImageView) rowView.findViewById(R.id.dragHandle);
        holder.addBelowButton = (ImageView) rowView.findViewById(R.id.addBelow);
        holder.editItemButton = (ImageView) rowView.findViewById(R.id.editItem);
        holder.checkBox = (CheckBox) rowView.findViewById(R.id.checkBox);

        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if( activity.mode.inCheckMode() ) {
            onBindViewHolderCheckMode(holder, position);
        } else {
            onBindViewHolderEditMode(holder, position);
        }
    }

    public void onBindViewHolderEditMode(final ViewHolder holder, final int position) {
        InventoryEditItem inventoryEditItem = activity.getModelManager()
                .getInventoryEditList().get(position);
        //Log.d(TAG,"onBindViewHolder for "+inventoryEditItem.toString());

        // set the text field to this InventoryEditItem's description
        holder.descriptionTextView.setText(inventoryEditItem.toString());

        // set up the "add a new InventoryEditItem below this one" icon button
        holder.addBelowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* we are not using the inventoryEditItem initialized at the top of
                onBindViewHolderEditMode because we'd hold a reference to an InventoryEditItem
                in here possibly even after the model were invalidated and reloaded. This
                orphaned InventoryEditItem would remain until the view were rebound.
                 */
                InventoryEditItem inventoryEditItem = activity.getModelManager()
                        .getInventoryEditList().get(position);

                //Log.d(TAG,"onBindViewHolder for "+inventoryEditItem.to
                Log.d(TAG, "add new item after "+holder.getAdapterPosition());
                activity.showAddItemDialog(inventoryEditItem);
            }
        });

        // set up the "edit this item" icon button
        holder.editItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                InventoryEditItem inventoryEditItem = activity.getModelManager()
                        .getInventoryEditList().get(position);

                //Log.d(TAG,"onBindViewHolder for "+inventoryEditItem.to
                activity.showEditItemDialog(inventoryEditItem);
            }
        });

        // make the drag handle start a drag event when it is touched
        holder.dragHandle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                InventoryEditItem inventoryEditItem = activity.getModelManager()
                        .getInventoryEditList().get(position);

                //Log.d(TAG,"onBindViewHolder for "+inventoryEditItem.to
                if( MotionEventCompat.getActionMasked(event)==MotionEvent.ACTION_DOWN) {
                    Log.d(TAG,"got a ACTION_DOWN for "+ inventoryEditItem.toString());
                    activity.getItemTouchHelper().startDrag(holder);
                }
                return false;
            }
        });

        holder.checkBox.setOnClickListener(null);

        holder.mRowView.setClickable(false);
        holder.editItemButton.setVisibility(View.VISIBLE);
        holder.checkBox.setVisibility(View.GONE);
        holder.addBelowButton.setVisibility(View.VISIBLE);
        holder.dragHandle.setVisibility(View.VISIBLE);
    }

    public void onBindViewHolderCheckMode(final ViewHolder holder, final int position) {

        InventoryEditItem inventoryEditItem = activity.getModelManager()
                .getInventoryEditList().get(position);

        /*
        final InventoryViewItem inventoryViewItem = activity.getModelManager()
                .getInventoryViewItemArrayList().get(position);
        */

        // set the text field to this InventoryEditItem's description
        holder.descriptionTextView.setText(inventoryEditItem.toString());

        holder.addBelowButton.setOnClickListener(null);
        holder.editItemButton.setOnClickListener(null);
        holder.dragHandle.setOnTouchListener(null);

        // set up the "need to buy this" / "dont need to buy this" checkbox
        holder.checkBox.setChecked(inventoryEditItem.getNeeded());
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* we are not using the inventoryEditItem initialized at the top of
                onBindViewHolderCheckMode because we'd hold a reference to an InventoryEditItem
                in here possibly even after the model were invalidated and reloaded. This
                orphaned InventoryEditItem would remain until the view were rebound.
                 */
                InventoryEditItem inventoryEditItem = activity.getModelManager()
                        .getInventoryEditList().get(position);

                inventoryEditItem.setNeeded( holder.checkBox.isChecked() );
                activity.getModelManager().saveInventoryEditList();


                //Model.getInstance().itemNeeded(inventoryViewItem.getId());

                /*
                if( inventoryViewItem.getNeeded()) {
                    Log.d(TAG,"item IS currently needed");
                    Model.getInstance().itemNotNeeded(inventoryViewItem.getId());
                    holder.checkBox.setChecked(false);
                } else {
                    Log.d(TAG,"item is NOT currently needed");
                    Model.getInstance().itemNeeded(inventoryViewItem.getId());
                    holder.checkBox.setChecked(true);
                }
                */
            }
        });

        holder.mRowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                InventoryEditItem inventoryEditItem = activity.getModelManager()
                        .getInventoryEditList().get(position);

                inventoryEditItem.toggleNeeded();
                holder.checkBox.setChecked(inventoryEditItem.getNeeded());
                activity.getModelManager().saveInventoryEditList();


                //Model.getInstance().itemNeeded(inventoryEditItem.getId());

                /*
                if( inventoryViewItem.getNeeded()) {
                    Model.getInstance().itemNotNeeded(inventoryViewItem.getId());
                    holder.checkBox.setChecked(false);
                } else {
                    Model.getInstance().itemNeeded(inventoryViewItem.getId());
                    holder.checkBox.setChecked(true);
                }
                */
            }
        });

        holder.mRowView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                InventoryEditItem inventoryEditItem = activity.getModelManager()
                        .getInventoryEditList().get(position);

                activity.showItemNeededDialog(inventoryEditItem.getId());
                return true;
            }
        });

        holder.mRowView.setClickable(true);
        holder.editItemButton.setVisibility(View.GONE);
        holder.checkBox.setVisibility(View.VISIBLE);
        holder.addBelowButton.setVisibility(View.GONE);
        holder.dragHandle.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return activity.getModelManager().getItemCount();
    }
}
