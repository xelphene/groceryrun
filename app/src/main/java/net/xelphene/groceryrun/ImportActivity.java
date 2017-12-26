package net.xelphene.groceryrun;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ImportActivity extends AppCompatActivity {

    private static final String TAG = "ImportActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if( savedInstanceState==null ) {
            // if state is being restored, android will restore the state of
            // the fragment for us
            FrameLayout fragmentContainer = (FrameLayout) findViewById(R.id.fragment_container);

            ChooserFragment chooser = new ChooserFragment();
            getSupportFragmentManager().beginTransaction().add(
                    R.id.fragment_container, chooser
            ).commit();
        }

    }

    public void startImport(View view) {

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        Log.d(TAG,"ImportActivity.onActivityResult: requestCode="+requestCode);
        super.onActivityResult(requestCode, resultCode, resultData);
    }

    public static class ChooserFragment extends Fragment {
        private static int REQUEST_CODE_FILE_CHOSEN = 222;
        private TextView fileName;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
                savedInstanceState) {
            View layout = inflater.inflate(R.layout.fragment_import_chooser, container, false);

            final Button chooseButton = (Button) layout.findViewById(R.id.chooseFile);
            chooseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chooseFile();
                }
            });

            fileName = (TextView) layout.findViewById(R.id.fileName);

            return layout;
        }

        public void chooseFile () {
            Log.d(TAG,"chooseFile...");
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/*");
            startActivityForResult(intent, REQUEST_CODE_FILE_CHOSEN);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
            Log.d(TAG,"ChooserFragment.onActivityResult: requestCode="+requestCode);
            if( requestCode==REQUEST_CODE_FILE_CHOSEN ) {
                if( resultData!=null ) {
                    Uri uri = resultData.getData();
                    Log.d(TAG,"file chosen: "+uri.toString());

                    //fileName.setText(uri.toString());
                    new FileAnalysisTask().execute(uri);

                } else {
                    // this will happen if the user cancels the file selection process
                    //Log.e(TAG,"REQUEST_CODE_FILE_CHOSEN ActivityResult, but resultData==null");
                }
            } else {
                super.onActivityResult(requestCode, resultCode, resultData);
            }
        }

        private class FileAnalysisTask extends AsyncTask<Uri, Integer, String> {
            @Override
            protected void onPreExecute() {
                fileName.setText("starting analysis...");
            }

            protected String doInBackground(Uri... uris) {
                ImportActivity activity = (ImportActivity) getActivity();

                int lineCount=0;
                StringBuffer preview = new StringBuffer();

                InputStream inputStream=null;
                BufferedReader reader=null;

                try {
                    inputStream = activity.getContentResolver().openInputStream(uris[0]);
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while( (line=reader.readLine()) != null ) {
                        lineCount+=1;
                        Log.d(TAG,"line: "+line);
                        publishProgress(lineCount);
                        if( lineCount<=3 ) {
                            preview.append(line);
                            preview.append("\n");
                        }

                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            break;
                        }

                    }
                    preview.append("\n");
                    preview.append("total lines: "+lineCount);

                    return preview.toString();

                } catch( FileNotFoundException e ) {
                    return "file not found";
                } catch( IOException e ) {
                    return "IO error: "+e.toString();
                } finally {
                    if( inputStream != null ) {
                        try {
                            inputStream.close();
                            reader.close();
                        } catch( IOException e ) {

                        }
                    }
                }

                /*
                int i;
                for( i=0; i<5; i++) {

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        break;
                    }

                    // would do stuff here

                    publishProgress(i);

                    if( isCancelled() ) {
                        break;
                    }
                }

                return "file analyzed: "+i;
                */
            }

            @Override
            protected void onProgressUpdate(Integer... progress) {
                fileName.setText("analyzing... "+progress[0]);
            }

            @Override
            protected void onPostExecute(String result) {
                fileName.setText(result);
            }
        }
    }

}
