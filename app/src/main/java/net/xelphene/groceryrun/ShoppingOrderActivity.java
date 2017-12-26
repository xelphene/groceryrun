package net.xelphene.groceryrun;

import android.support.v4.view.MotionEventCompat;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.xelphene.groceryrun.model.Item;
import net.xelphene.groceryrun.model.Model;
import net.xelphene.groceryrun.model.ShoppingListItem;

import java.util.ArrayList;
import java.util.Collections;

public class ShoppingOrderActivity extends AppCompatActivity {

    private ItemTouchHelper itemTouchHelper;
    private ShoppingOrderAdapter adapter;
    private ArrayList<Item> shoppingList;
    private static final String TAG = "ShoppingOrderActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_order);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Shopping Order");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        shoppingList = Model.getInstance().getItemsInShoppingOrder();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(false);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ShoppingOrderAdapter(this);
        recyclerView.setAdapter(adapter);

        itemTouchHelper = new ItemTouchHelper(new Callback());
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.shopping_order_toolbar_menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if( item.getItemId()==R.id.save_button) {
            Log.d(TAG,"save new order!");
            Model.getInstance().setShoppingOrder(shoppingList);
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public ItemTouchHelper getItemTouchHelper() {
        return itemTouchHelper;
    }

    public ArrayList<Item> getList() {
        return shoppingList;
    }

    public class Callback extends ItemTouchHelper.Callback {
        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeMovementFlags(
                    ItemTouchHelper.UP | ItemTouchHelper.DOWN, // allow drag
                    0 // do not allow swipe
            );
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder src, RecyclerView.ViewHolder dst) {
            Collections.swap(shoppingList, src.getAdapterPosition(), dst.getAdapterPosition());
            adapter.notifyItemMoved(src.getAdapterPosition(), dst.getAdapterPosition());
            return true;
        }
    }

    public static class ShoppingOrderAdapter
    extends RecyclerView.Adapter<ShoppingOrderAdapter.ViewHolder>
    {
        private ShoppingOrderActivity mActivity;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public View mRowView;
            public TextView description;
            public ImageView dragHandle;

            public ViewHolder(View rowView) {
                super(rowView);
                mRowView = rowView;
            }
        }

        public ShoppingOrderAdapter(ShoppingOrderActivity activity) {
            mActivity = activity;
        }

        @Override
        public ShoppingOrderAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View rowView = inflater.inflate(R.layout.shopping_order_row, parent, false);
            ViewHolder holder = new ViewHolder(rowView);
            holder.description = (TextView) rowView.findViewById(R.id.description);
            holder.dragHandle = (ImageView) rowView.findViewById(R.id.dragHandle);
            holder.mRowView.setClickable(false);
            return holder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final Item item = mActivity.getList().get(position);
            holder.description.setText(item.description);

            // relay a touch on the drag handle to the RecyclerView's ItemTouchHelper which will
            // do the drag
            holder.dragHandle.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if( MotionEventCompat.getActionMasked(event)==MotionEvent.ACTION_DOWN) {
                        mActivity.getItemTouchHelper().startDrag(holder);
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mActivity.getList().size();
        }
    }
}
