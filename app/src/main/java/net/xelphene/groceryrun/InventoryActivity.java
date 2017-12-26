package net.xelphene.groceryrun;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import net.xelphene.groceryrun.model.InventoryEditItem;
import net.xelphene.groceryrun.model.InventoryEditList;
import net.xelphene.groceryrun.model.InventoryViewItem;
import net.xelphene.groceryrun.model.Model;
import net.xelphene.groceryrun.model.ModelUpdatedListener;

import java.util.ArrayList;

/**
 * InventoryActivity implements inventory checking and editing.
 *
 * I would have preferred checking and editing be two separate Activities, but as far as I can
 * tell there is no easy way to share the scroll position between two different RecyclerViews. It
 * is important that when switching back and forth between editing/checking inventory that the
 * list is scrolled to the same position.
 *
 * I also tried making two different Adapters, but calling RecyclerView.setAdapter() resets the
 * RecyclerView's scroll position, creating the same problem.
 *
 * I have found solutions out there for sharing scroll positions between RecyclerViews by
 * trapping scroll-related events and relaying them to the other RecyclerView, but that is
 * allegedly unreliable with flings and is also an even bigger mess than making one Activity and
 * one Adapter that just does both edit and check.
 *
 * RecyclerView.scrollTo() doesn't work - nothing will happen and an error will be logged that
 * RecyclerView doesn't support that.
 *
 * RecyclverView.LayoutManager.scollToPosition() only scrolls enough so that the given adapter
 * position is visible in the list. It can't scroll to a precise position.
 */
public class InventoryActivity extends AppCompatActivity
    implements ModelUpdatedListener
{
    private static final String TAG = "InventoryActivity";

    private RecyclerView recyclerView;
    private InventoryRecyclerAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ItemTouchHelper itemTouchHelper;
    public final Mode mode;
    public final ModelManager modelManager;

    public InventoryActivity() {
        super();
        mode = new Mode();
        modelManager = new ModelManager();
        // TODO: unbind this, in on* somewhere
    }

    public void modelUpdated() {
        Log.d(TAG,"modelUpdated");
        modelManager.modelUpdated();
    }

    public ModelManager getModelManager() {
        return modelManager;
    }

    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        Model.getInstance().addModelUpdatedListener(this);

        setupToolbar();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(false);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        if( savedInstanceState==null) {
            Log.d(TAG,"onCreate: savedInstanceState==null");
            mode.setMode(Mode.MODE_CHECK);
        } else {
            // state should be restored in onRestoreState()
            Log.d(TAG,"onCreate: savedInstanceState!=null. doing nothing; expecting onRestoreInstanceState() to load everything");
        }
        adapter = new InventoryRecyclerAdapter(this);
        recyclerView.setAdapter(adapter);

        itemTouchHelper = new ItemTouchHelper(new Callback());
        itemTouchHelper.attachToRecyclerView(recyclerView);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy()");
        Model.getInstance().removeModelUpdatedListener(this);
    }

    /*
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume()");
        //Model.getInstance().addModelUpdatedListener(this);
    }
    */


    @Override
    public void onSaveInstanceState(Bundle outState) {
        /* TODO: inventoryEditList.writeToParcel() never actually gets
           called. the same reference to inventoryEditList is saved
           somewhere. how and why???
           */
        Log.d(TAG,"onSaveInstanceState()");
        super.onSaveInstanceState(outState);

        outState.putParcelable("inventoryEditList", modelManager.getInventoryEditListOrNull());

        outState.putInt("mode", mode.getMode());
    }

    @Override
    public void onRestoreInstanceState(Bundle inState) {
        Log.d(TAG,"onRestoreInstanceState()");
        super.onRestoreInstanceState(inState);

        InventoryEditList restoredList = inState.getParcelable("inventoryEditList");
        modelManager.restore(restoredList);

        mode.setMode(inState.getInt("mode",Mode.MODE_CHECK));

    }

    /**
     * InventoryRecyclerAdapter uses this to set an OnTouchListener for each drag handle to call
     * our ItemTouchHelper's startDrag method.
     * @return
     */
    public ItemTouchHelper getItemTouchHelper() {
        return itemTouchHelper;
    }

    /** called by Android to create the app options menu for this activity
     *
     * @param menu the Menu item for the activity (given its current state)
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Log.d(TAG,"onCreateOptionsMenu called");
        Toolbar toolbar = (Toolbar) findViewById(R.id.inventory_edit_toolbar);
        if( mode.inCheckMode() ) {
            toolbar.inflateMenu(R.menu.inventory_check_toolbar_menu);
        } else {
            toolbar.inflateMenu(R.menu.inventory_edit_toolbar_menu);
        }
        return true;
    }

    /** Called whenever the activity toggles between view and edit modes. This will cause
     * onCreateOptionsMenu() to be called by Android. That will in turn set the correct activity
     * menu for the current mode.
     */
    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.inventory_edit_toolbar);
        if( mode.inCheckMode() ) {
            toolbar.setTitle("Check Inventory");
        } else {
            toolbar.setTitle("Edit Inventory");
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if( item.getItemId()==R.id.inventory_save_button) {
            //Log.d(TAG, "done editing. save.");
            modelManager.saveInventoryEditList();

            mode.goToCheckMode();
            //finish();
            return true;
        } else if( item.getItemId()==R.id.inventory_add_button) {
            //Log.d(TAG,"ADD (general)");
            showAddItemDialog(null);
            return true;
        } else if( item.getItemId()==R.id.inventory_edit_button) {
            //Log.d(TAG,"want to edit");
            mode.goToEditMode();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void showItemNeededDialog(long itemId) {
        //modelManager.invalidate();
        Intent intent = new Intent(this, QuickAddActivity.class);
        Bundle extras = new Bundle();
        extras.putLong("itemId",itemId);
        intent.putExtras(extras);
        startActivity(intent);
    }

    public void showAddItemDialog(InventoryEditItem afterItem) {
        int index;
        if( afterItem!=null ) {
            if (modelManager.getInventoryEditList().indexOf(afterItem) >= 0) {
                index= modelManager.getInventoryEditList().indexOf(afterItem)+1;
            } else {
                Log.e(TAG,"asked to insert after "+afterItem.toString()+" but that item does not exist! appending instead...");
                index= modelManager.getInventoryEditList().size();
            }
        } else {
            index= modelManager.getInventoryEditList().size();
        }

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        // TODO: delete any existing addItemDialog

        Bundle b = new Bundle();
        b.putString("mode","add");
        b.putInt("index",index);
        AddEditItemFragment fragment = new AddEditItemFragment();
        //fragment.setActivity(this);
        fragment.setArguments(b);

        fragment.show(ft, "addItemDialog");
    }

    public void showEditItemDialog(InventoryEditItem editItem) {
        int index = modelManager.getInventoryEditList().indexOf(editItem);

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        // TODO: delete any existing addItemDialog

        Bundle b = new Bundle();
        b.putString("mode","edit");
        b.putInt("index", index );
        b.putString("description", editItem.getDescription());
        AddEditItemFragment fragment = new AddEditItemFragment();
        //fragment.setActivity(this);
        fragment.setArguments(b);

        fragment.show(ft, "addItemDialog");
    }

    /** called by the AddEditItemFragment (add item dialog) when it is completed and we have a new
     * InventoryEditItem to add.
     * @param afterIndex the index (in inventoryEditList) after which the item should be added
     * @param description new InventoryEditItem's description
     */
    public void addItem(int afterIndex, String description) {
        //Log.d(TAG,"addItem: afterIndex="+afterIndex+" description="+description);
        if( afterIndex > modelManager.getInventoryEditList().size() ) {
            Log.e(TAG,"atIndex > inventoryEditList.size(); simply appending instead");
            afterIndex = modelManager.getInventoryEditList().size();
        }
        InventoryEditItem newItem = new InventoryEditItem(description);
        modelManager.getInventoryEditList().add(afterIndex,newItem);
        adapter.notifyDataSetChanged();
    }

    /** called by the AddEditItemFragment when it is completed and some InventoryEditItem
     * neews a new description set.
     * @param index the index (in inventoryEditList) of the InventoryEditItem to be changed
     * @param newDescription the new description for that InventoryEditItem
     */
    public void saveItem(int index, String newDescription ) {
        InventoryEditItem item = modelManager.getInventoryEditList().get(index);
        if( item!=null ) {
            item.setDescription(newDescription);
            adapter.notifyDataSetChanged();
        } else {
            Log.e(TAG,"asked to save edit to item with index "+index+" which doesn't exist in " +
                    "inventoryEditList");
        }
    }

    // this class is static because Android will want to instantiate it without having an instance of the parent Activity
    public static class AddEditItemFragment extends DialogFragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_add_edit_inventory_item, container, false);

            Button cancelButton = (Button) v.findViewById(R.id.cancel_button);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });

            final EditText editText = (EditText) v.findViewById(R.id.editText);
            final String mode = getArguments().getString("mode","");

            if (mode.equals("edit")) {
                editText.setText(getArguments().getString("description",""));
            }

            Button addButton = (Button) v.findViewById(R.id.add_button);

            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveItem(editText.getText().toString());
                    dismiss();
                }
            });

            return v;
        }

        private void saveItem(String description) {
            try {
                InventoryActivity activity = (InventoryActivity) getActivity();
                if(activity!=null) {
                    sendItemToActivity(activity, description);
                } else {
                    Log.e(TAG,"AddEditItemFragment has no activity reference!");
                }
            } catch (ClassCastException e) {
                Log.e(TAG,"AddEditItemFragment is associated with an Activity other than " +
                        "InventoryActivity!");
            }
        }

        private void sendItemToActivity(InventoryActivity inventoryActivity, String description) {
            String mode = getArguments().getString("mode","");
            int index = getArguments().getInt("index");

            if( mode.equals("add") ) {
                inventoryActivity.addItem(index, description);
            } else if( mode.equals("edit") ) {
                inventoryActivity.saveItem(index,description);
            } else {
                Log.e(TAG,"mode argument to AddEditItemFragment has unknown value: "+mode);
            }
        }
    }

    public class Callback extends ItemTouchHelper.Callback {
        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            /*
            return makeFlag(
                    ItemTouchHelper.ACTION_STATE_SWIPE,
                    ItemTouchHelper.RIGHT
            );
            */
            if( mode.inCheckMode() ) {
                return makeMovementFlags(0,0);
            } else {
                return makeMovementFlags(
                        ItemTouchHelper.UP | ItemTouchHelper.DOWN, // allow drag
                        ItemTouchHelper.RIGHT // allow swipe
                );
            }
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            InventoryRecyclerAdapter.ViewHolder holder = (InventoryRecyclerAdapter.ViewHolder) viewHolder;

            Toast toast = Toast.makeText(
                    viewHolder.itemView.getContext(),
                    "Delete "+holder.descriptionTextView.getText().toString(),
                    Toast.LENGTH_SHORT
            );
            toast.show();

            modelManager.getInventoryEditList().remove(holder.getAdapterPosition());
            adapter.notifyItemRemoved(holder.getAdapterPosition());
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder src, RecyclerView.ViewHolder dst) {
            modelManager.getInventoryEditList().swap(src.getAdapterPosition(), dst
                    .getAdapterPosition());
            adapter.notifyItemMoved(src.getAdapterPosition(), dst.getAdapterPosition());
            return true;
        }
    }

    public class Mode {
        public static final int MODE_EDIT=1;
        public static final int MODE_CHECK=2;
        private int mode; // either MODE_EDIT or MODE_CHECK

        public Mode () {
            mode=MODE_EDIT;
        }

        public boolean inCheckMode() {
            return mode==MODE_CHECK;
        }

        public boolean inEditMode() {
            return mode==MODE_EDIT;
        }

        public void toggleMode() {
            if( inEditMode() ) {
                mode=MODE_CHECK;
            } else {
                mode=MODE_EDIT;
            }
            modeChanged();
        }

        public void setMode(int newMode) {
            if( newMode!=MODE_CHECK && newMode!=MODE_EDIT ) {
                Log.e(TAG,"Mode.setMode called with invalid newMode: "+newMode);
                return;
            }

            if( newMode!=mode ) {
                mode=newMode;
                modeChanged();
            }
        }

        public int getMode() {
            return mode;
        }

        public void goToEditMode() {
            mode=MODE_EDIT;
            modeChanged();
        }

        public void goToCheckMode() {
            mode=MODE_CHECK;
            modeChanged();
        }

        private void modeChanged() {

            setupToolbar();

            // adapter is null when this method is called in the constructor
            if( adapter!=null ) {
                adapter.modeChanged();
            }

        }
    }

    public class ModelManager {

        /* ModelManager caches an inventoryEditList because getInventoryEditList() may be called
        many times in quick succession, such as by the adapter's onBindViewHolder(), which is called
        to fill in new rows when scrolling. The adapter calls getInventoryEditList() so often
        rather than keeping references to InventoryEditItems because:

         we'd hold a reference to an InventoryEditItem
         in here possibly even after the model were invalidated and reloaded. This
         orphaned InventoryEditItem would remain until the view were rebound.
         */

        InventoryEditList inventoryEditList;
        private static final String TAG = "ModelManager";

        public void restore(InventoryEditList restoredList) {
            inventoryEditList = restoredList;
        }

        public void invalidate() {
            inventoryEditList=null;
        }

        public InventoryEditList getInventoryEditList() {
            if( inventoryEditList==null ) {
                Log.d(TAG,"getting a new InventoryEditList");
                inventoryEditList = Model.getInstance().getInventoryEditList();

                /* we might be here on activity startup, at which point recyclerView is still
                computing a layout. doing notifyDataSetChanted() then will cause an exception.
                 */
                if( recyclerView!=null && ! recyclerView.isComputingLayout() ) {
                    adapter.notifyDataSetChanged();
                }
            }
            return inventoryEditList;
        }

        public InventoryEditList getInventoryEditListOrNull() {
            return inventoryEditList;
        }


        /** return the number of items in whichever model we're currently using */
        public int getItemCount() {
            return getInventoryEditList().size();
        }

        public void modelUpdated() {
            Log.d(TAG,"modelUpdated");
            if( inventoryEditList!=null ) {
                inventoryEditList = Model.getInstance().getInventoryEditList();
                if( ! recyclerView.isComputingLayout() ) {
                    adapter.notifyDataSetChanged();
                }
            }
        }

        public void saveInventoryEditList() {
            if( inventoryEditList!=null ) {
                Model.getInstance().removeModelUpdatedListener(InventoryActivity.this);
                Model.getInstance().saveInventoryEditList(inventoryEditList);
                Model.getInstance().addModelUpdatedListener(InventoryActivity.this);
            } else {
                Log.e(TAG,"asked to saveInventoryEditList(), but inventoryEditList is null");
            }
        }
    }

}
