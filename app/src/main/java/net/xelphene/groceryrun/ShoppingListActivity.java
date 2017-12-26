package net.xelphene.groceryrun;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import net.xelphene.groceryrun.model.Model;
import net.xelphene.groceryrun.model.ShoppingListItem;

import java.util.ArrayList;

public class ShoppingListActivity extends AppCompatActivity {


    private ArrayList<ShoppingListItem> shoppingList;
    private RecyclerAdapter adapter;
    private static final String TAG = "ShoppingList";

    public ShoppingListActivity() {
        shoppingList = new ArrayList<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        shoppingList = Model.getInstance().getShoppingList();

        setContentView(R.layout.activity_shopping_list_show);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Shopping List");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(false);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RecyclerAdapter(this);
        recyclerView.setAdapter(adapter);

        DividerItemDecoration divider = new DividerItemDecoration(
                recyclerView.getContext(), LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(divider);

        ItemTouchHelper helper = new ItemTouchHelper(new Callback());
        helper.attachToRecyclerView(recyclerView);
    }

    private ArrayList<ShoppingListItem> getShoppingList() {
        return shoppingList;
    }



    private static class RecyclerAdapter
    extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder>
    {
        ShoppingListActivity activity;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public TextView descriptionTextView;
            public TextView shopNoteTextView;
            public ShoppingListItem inventoryItem;

            public ViewHolder(View rowView) {
                super(rowView);
            }
        }

        public RecyclerAdapter(ShoppingListActivity shoppingListActivity) {
            activity = shoppingListActivity;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            ShoppingListItem item = activity.getShoppingList().get(position);
            holder.inventoryItem = item;
            if( item==null ) {
                holder.descriptionTextView.setText("UNKNOWN");
                holder.shopNoteTextView.setText("?");
            } else {
                holder.descriptionTextView.setText(item.getDescription());

                /* if the shopNote is empty, remove the field for it from the layout. this will
                   allow the description text to be vertically in the center of the row, evenly
                   spaced from the top and bottom. the layout has a minHeight in the XML, so it
                   won't become too small when doing this.
                 */
                if( item.getShopNote()==null || item.getShopNote().isEmpty() ) {
                    holder.shopNoteTextView.setText("");
                    holder.shopNoteTextView.setVisibility(View.GONE);
                } else {
                    holder.shopNoteTextView.setText(item.getShopNote());
                    holder.shopNoteTextView.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View rowView = inflater.inflate(R.layout.shopping_list_row, parent, false);

            ViewHolder holder = new ViewHolder(rowView);
            holder.descriptionTextView = (TextView) rowView.findViewById(R.id.description);
            holder.shopNoteTextView = (TextView) rowView.findViewById(R.id.shopNote);

            return holder;
        }

        @Override
        public int getItemCount() {
            return activity.getShoppingList().size();
        }
    }

    private class Callback extends ItemTouchHelper.Callback {
        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder holder) {
            return makeMovementFlags(0, ItemTouchHelper.RIGHT);
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            RecyclerAdapter.ViewHolder adapterViewHolder = (RecyclerAdapter.ViewHolder) viewHolder;
            ShoppingListItem item = adapterViewHolder.inventoryItem;
            shoppingList.remove(item);
            adapter.notifyItemRemoved(adapterViewHolder.getAdapterPosition());

            Model.getInstance().itemNotNeeded(item.getId());

            Toast toast = Toast.makeText(
                    getApplicationContext(),
                    item.getDescription() + " marked as purchased",
                    Toast.LENGTH_SHORT
            );
            toast.show();
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder src, RecyclerView.ViewHolder dst){
            // should never be called. getMovementFlags says rows can't be moved.
            return true;
        }
    }
}
