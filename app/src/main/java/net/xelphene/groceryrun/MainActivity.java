package net.xelphene.groceryrun;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.content.Intent;
import android.support.v7.widget.Toolbar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int READ_REQUEST_CODE = 222;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"MAIN onDestory called");
    }

    public void showShoppingList(View view) {
        Intent intent = new Intent(this, ShoppingListActivity.class);
        startActivity(intent);
    }

    public void showShoppingOrder(View view) {
        Intent intent = new Intent(this, ShoppingOrderActivity.class);
        startActivity(intent);
    }

    public void showRecyclerTest(View view) {
        Intent intent = new Intent(this, InventoryActivity.class);
        startActivity(intent);
    }

    public void quickAdd(View view) {
        Intent intent = new Intent(this, QuickAddActivity.class);
        startActivity(intent);
    }

    public void doImport(View view) {
        Intent intent = new Intent(this, ImportActivity.class);
        startActivity(intent);

        /*
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
        */
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if( requestCode==READ_REQUEST_CODE ) {
            if( resultData!=null ) {
                Uri uri = resultData.getData();
                Log.d(TAG, "content chosen: "+uri.toString());
                try {
                    importFile(uri);
                } catch( IOException e ) {
                    Log.e(TAG,"IOException: "+e.toString());
                }
            }
        }
    }

    private void importFile(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line = reader.readLine();
        while( line!=null ) {
            Log.d(TAG,"READ: "+line);
            line = reader.readLine();
        }
        inputStream.close();
    }
}
