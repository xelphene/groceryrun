package net.xelphene.groceryrun;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RadioButton;
import android.widget.TextView;

import net.xelphene.groceryrun.model.InventoryEditItem;
import net.xelphene.groceryrun.model.InventoryViewItem;
import net.xelphene.groceryrun.model.Model;

import java.util.ArrayList;

public class QuickAddActivity extends AppCompatActivity
implements AdapterView.OnItemClickListener
{

    private static final String TAG = "QuickAdd";
    ArrayList<InventoryViewItem> inventoryItems;
    InventoryViewItem chosenItem;
    InventoryArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_add);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        inventoryItems = Model.getInstance().getInventoryViewArrayList();
        for(InventoryViewItem item: inventoryItems ) {
            Log.d(TAG,"loaded: "+item);
        }

        adapter = new InventoryArrayAdapter(this,
                android.R.layout.simple_dropdown_item_1line, inventoryItems);

        setPreSelected();

        AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id
                .autoCompleteTextView);
        textView.setOnItemClickListener(this);
        textView.setAdapter(adapter);
    }

    private void setPreSelected() {
        Bundle extras = getIntent().getExtras();
        if( extras==null ) {
            Log.d(TAG,"no preselected item");
            return;
        } else {
            long preSelectedItemId = getIntent().getExtras().getLong("itemId");
            Log.d(TAG,"preSelectedItemId: "+preSelectedItemId);
            chosenItem = Model.getInstance().getInventoryViewItem(preSelectedItemId);
            Log.d(TAG,"  item: "+chosenItem);
            updateChosenItemState();
        }
    }

    public void doAdd(View view) {
        AutoCompleteTextView descriptionTextView = (AutoCompleteTextView) findViewById(R.id
                .autoCompleteTextView);
        TextView noteTextView = (TextView) findViewById(R.id.note);

        RadioButton oneTimeRadioButton = (RadioButton) findViewById(R.id.radio_onetime);

        boolean perm = ! oneTimeRadioButton.isChecked();
        String note = noteTextView.getText().toString();
        String description = descriptionTextView.getText().toString();

        Log.i(TAG,"quick add:");
        if( chosenItem!=null) {
            Log.i(TAG,"  add EXISTING inventory item: "+chosenItem.toString());
            //Model.getInstance().quickAddInventoryItem(chosenItem, note);
            Model.getInstance().itemNeeded(chosenItem.getId(), note);
        } else {
            Log.i(TAG,"  add new:");
            Log.i(TAG,"  description: "+description);
            Log.i(TAG,"  note: "+note);
            Log.i(TAG,"  perm: "+perm);
            if( perm ) {
                Model.getInstance().addInventoryItem(description, note, true);
            } else {
                Model.getInstance().addTemporaryItem(description, note);
            }
        }

        finish();
    }

    /** called when the user selects something from the description autocomplete drop-down */
    public void onItemClick(AdapterView parent, View view, int position, long id) {
        try {
            TextView textView = (TextView) view;
            /* note that position reflects the position in the *filtered*
               list, not the whole original list (inventoryItems). to find out
               what InventoryEditItem is at that position, ask the adapter. don't
               use position as in index into inventoryItems
             */
            //chosenItem = inventoryItems.get(position);
            chosenItem = adapter.getItem(position);
        } catch( ClassCastException e) {
            Log.d(TAG,"whatever they clicked, it wasn't a TextView...????");
        }
        updateChosenItemState();
    }

    /** called when the Clear button is clicked to clear the currently entered or selected
     * InventoryITem
     * @param view
     */
    public void clearItem(View view) {
        chosenItem=null;

        AutoCompleteTextView descriptionTextView = (AutoCompleteTextView) findViewById(R.id
                .autoCompleteTextView);
        descriptionTextView.setText("");

        updateChosenItemState();
    }

    private void updateChosenItemState() {
        AutoCompleteTextView descriptionTextView = (AutoCompleteTextView) findViewById(R.id
                .autoCompleteTextView);

        RadioButton oneTimeRadioButton = (RadioButton) findViewById(R.id.radio_onetime);
        RadioButton regularRadioButton = (RadioButton) findViewById(R.id.radio_regular);

        if( chosenItem!=null ) {
            Log.d(TAG,"updateChosenState: a specific InventoryEditItem is chosen: "+chosenItem);
            descriptionTextView.setEnabled(false);

            oneTimeRadioButton.setEnabled(false);
            oneTimeRadioButton.setChecked(false);

            regularRadioButton.setEnabled(false);
            regularRadioButton.setChecked(true);

            descriptionTextView.setText(chosenItem.getDescription()+" (in Inventory)");
        } else {
            Log.d(TAG,"updateChosenState: no specific item is chosen");
            descriptionTextView.setEnabled(true);

            oneTimeRadioButton.setEnabled(true);
            oneTimeRadioButton.setChecked(true);

            regularRadioButton.setEnabled(true);
            regularRadioButton.setChecked(false);
        }
    }

    private class InventoryArrayAdapter extends ArrayAdapter<InventoryViewItem> {

        public InventoryArrayAdapter(Context context, int resource, ArrayList<InventoryViewItem>
                list) {
            super(context, resource, list);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            try {
                TextView textView = (TextView) view;
                InventoryViewItem item = getItem(position);
                textView.setText(item.getDescription()+" (in Inventory)");
            } catch (ClassCastException e) {
                // TODO: just return super.getView's View, whatever it is...
            }
            return view;
        }
    }
}
