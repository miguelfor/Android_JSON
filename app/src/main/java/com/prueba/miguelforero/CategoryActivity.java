package com.prueba.miguelforero;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hisham.jsonparsingdemo.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.prueba.miguelforero.helpers.TodoHelper;
import com.prueba.miguelforero.models.AppModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CategoryActivity extends ActionBarActivity {

    private final String URL_TO_HIT = "https://itunes.apple.com/us/rss/topfreeapplications/limit=20/json";
    private ListView lvMovies;
    private ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setMessage("Loading. Please wait...");
        // Create default options which will be used for every
        //  displayImage(...) call if no options will be passed to this method
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .build();
        ImageLoader.getInstance().init(config); // Do it on Application start

        lvMovies = (ListView) findViewById(R.id.lvMovies);


        new JSONTask().execute(URL_TO_HIT);


    }


    public class JSONTask extends AsyncTask<String, String, List<AppModel>> {
        TodoHelper todoHelper= new TodoHelper();
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }



        @Override
        protected List<AppModel> doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                String finalJson=null;

                if(todoHelper.isOnlineNet()){
                    URL url = new URL(params[0]);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();
                    InputStream stream = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(stream));
                    StringBuffer buffer = new StringBuffer();
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }
                     finalJson = buffer.toString();
                     writeMesssage(finalJson);
                }else{
                    finalJson=readMessage();
                }
                JSONObject parentObject = new JSONObject(finalJson);
                JSONObject firstJSON = parentObject.getJSONObject("feed");
                JSONArray parentArray = firstJSON.getJSONArray("entry");
                List<AppModel> appModelList = new ArrayList<>();
                Gson gson = new Gson();

                for (int i = 0; i < parentArray.length(); i++) {
                    JSONObject finalObject = parentArray.getJSONObject(i);
                    AppModel appModel = new AppModel();
                    appModel.setTitle(parentArray.getJSONObject(i).getJSONObject("title").getString("label"));
                    appModel.setCategory(parentArray.getJSONObject(i).getJSONObject("category").getJSONObject("attributes").getString("term"));
                    Random r = new Random();
                    appModel.setRating(r.nextInt(5 - 1 + 1) + 1);
                    appModel.setImage(parentArray.getJSONObject(i).getJSONArray("im:image").getJSONObject(2).getString("label"));
                    appModel.setStory(parentArray.getJSONObject(i).getJSONObject("summary").getString("label"));
                    boolean saber=false;
                    if(i==0){ appModelList.add(appModel);}
                    else{
                        for(int x=0; x<  appModelList.size(); x++){

                            if(appModelList.get(x).getCategory().equals(appModel.getCategory())){
                                saber=true;
                                x=appModelList.size();
                            }
                        }
                        if(!saber){
                            appModelList.add(appModel);
                        }
                    }
                }
                return appModelList;
                    ///////////////////



            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {

                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }


        @Override
        protected void onPostExecute(final List<AppModel> result) {
            super.onPostExecute(result);
            dialog.dismiss();
            if (result != null) {
                MovieAdapter adapter = new MovieAdapter(getApplicationContext(), R.layout.row_category, result);
                lvMovies.setAdapter(adapter);
                lvMovies.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        AppModel appModel = result.get(position);
                        Intent intent = new Intent(CategoryActivity.this, MainActivity.class);
                        intent.putExtra("appModel", new Gson().toJson(appModel));
                        startActivity(intent);
                    }
                });
                if(!todoHelper.isOnlineNet()) {
                    Toast.makeText(getApplicationContext(), "app don't have internet connection", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Not able to fetch data from server, please check url.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public class MovieAdapter extends ArrayAdapter {

        private List<AppModel> appModelList;
        private int resource;
        private LayoutInflater inflater;

        public MovieAdapter(Context context, int resource, List<AppModel> objects) {
            super(context, resource, objects);
            appModelList = objects;
            this.resource = resource;
            inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(resource, null);
                holder.ivMovieIcon = (ImageView) convertView.findViewById(R.id.ivIcon);
                holder.tvMovie = (TextView) convertView.findViewById(R.id.tvMovie);
                holder.tvYear = (TextView) convertView.findViewById(R.id.tvYear);
                holder.rbMovieRating = (RatingBar) convertView.findViewById(R.id.rbMovie);
                holder.tvStory = (TextView) convertView.findViewById(R.id.tvStory);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final ProgressBar progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);

            // Then later, when you want to display image
            ImageLoader.getInstance().displayImage(appModelList.get(position).getImage(), holder.ivMovieIcon, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    progressBar.setVisibility(View.GONE);
                }
            });
            holder.tvYear.setText(appModelList.get(position).getCategory());
            return convertView;
        }


        class ViewHolder {
            private ImageView ivMovieIcon;
            private TextView tvMovie;
            private TextView tvYear;
            private RatingBar rbMovieRating;
            private TextView tvStory;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            new JSONTask().execute(URL_TO_HIT);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void writeMesssage(String Json){
        String Message= Json;
        String file_name ="name_file";

        try {
            FileOutputStream fileOutputStream= openFileOutput(file_name, MODE_PRIVATE);
            fileOutputStream.write(Message.getBytes());
            fileOutputStream.close();
        //    Toast.makeText(getApplicationContext(),"Saved",Toast.LENGTH_LONG).show();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    public String readMessage (){
        String retorna=null;
            try {
             String Message;
             FileInputStream fileInputStream = openFileInput("name_file");
             InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
             StringBuffer stringBuffer=new StringBuffer();
             while((Message=bufferedReader.readLine())!=null)
             {
                 stringBuffer.append(Message);
             }
            retorna=stringBuffer.toString();
             } catch (FileNotFoundException e) {
             e.printStackTrace();
            } catch (IOException e) {
             e.printStackTrace();
            }
    return retorna;
 }





}
