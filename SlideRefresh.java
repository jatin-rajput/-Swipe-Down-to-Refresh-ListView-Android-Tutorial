1. Android SwipeRefreshLayout:

Implementing SwipeRefreshLayout is very easy. Whenever you want to detect the swipe down on any view, just the wrap the view around 
SwipeRefreshLayout element. In our case, we are going to use it with ListView. And implement your activity class from SwipeRefreshLayout.
OnRefreshListener. When user swipes down the view, onRefresh() method will be triggered. In you need to take appropriate action in that 
function like making an http call and fetch the latest data.

<android.support.v4.widget.SwipeRefreshLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/swipe_refresh_layout"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">
 
    <!-- place your view here -->
 
</android.support.v4.widget.SwipeRefreshLayout>

//2.Creating Android Project:

1. In Android Studio, create a new project by navigating to File ⇒ New Project and fill all the required details. When it prompts to select a default activity, select Blank Activity and proceed.

2. Open build.gradle located under app folder and add volley library dependency. We are going to use volley to make HTTP calls to fetch the json.

com.mcxiaoke.volley:library-aar:1.0.0

build.gradle
dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
    compile 'com.android.support:appcompat-v7:22.1.0'
    compile 'com.mcxiaoke.volley:library-aar:1.0.0'
}
3. Open colors.xml under res ⇒ values and add below color resources. If you don’t find colors.xml, create a new file with the name. The color resources added below are used to set background color for movies rank in list view.

colors.xml

<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string-array name="movie_serial_bg">
        <item>#24c6d5</item>
        <item>#57dd86</item>
        <item>#ad7dcf</item>
        <item>#ff484d</item>
        <item>#fcba59</item>
        <item>#24c6d5</item>
    </string-array>
</resources>

3. Now under your project’s package, create three packages named app, activity and helper.

4. Under app package, create a class named MyApplication.java and add below code. This is a singleton Application class which initiates volley core objects on app launch.
    
MyApplication.java

package info.androidhive.swiperefresh.app;
 
import android.app.Application;
import android.text.TextUtils;
 
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
 
/**
 * Created by Ravi on 13/05/15.
 */
 
public class MyApplication extends Application {
 
    public static final String TAG = MyApplication.class
            .getSimpleName();
 
    private RequestQueue mRequestQueue;
 
    private static MyApplication mInstance;
 
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }
 
    public static synchronized MyApplication getInstance() {
        return mInstance;
    }
 
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
 
        return mRequestQueue;
    }
 
    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }
 
    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }
 
    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
}

5. Open AndroidManifest.xml and add MyApplication.java class to <application> tag. Also you need to add INTERNET permission as we need to make http calls.

 AndroidManifest.xml
 
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="info.androidhive.swiperefresh">
 
    <uses-permission android:name="android.permission.INTERNET"/>
 
    <application
        android:name=".app.MyApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/AppTheme">
        <activity
            android:name=".activity.MainActivity"
            android:label="@string/app_name">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
 
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
 
</manifest>
6. Now let’s create a custom adapter class for our list view. Under res ⇒ layout folder, create an xml layout named list_row.xml. This xml renders single list row in the ListView

list_row.xml

<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="horizontal"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">
 
    <TextView
        android:id="@+id/serial"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:padding="25dp"
        android:layout_margin="5dp"
        android:layout_alignParentLeft="true"
        android:textSize="20dp"
        android:textStyle="bold" />
 
    <TextView
        android:id="@+id/title"
        android:layout_toRightOf="@id/serial"
        android:layout_centerVertical="true"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textStyle="bold"
        android:paddingLeft="20dp"
        android:textSize="18dp" />
 
</RelativeLayout>
7. Under helper package, create a java class named Movie.java and add below code. This is a model class required to create movie objects to provide data to the List View

Movie.java

package info.androidhive.swiperefresh.helper;
 
/**
 * Created by Ravi on 13/05/15.
 */
public class Movie {
    public int id;
    public String title;
 
    public Movie() {
    }
 
    public Movie(int id, String title) {
        this.title = title;
        this.id = id;
    }
}

8. Under helper package, create another class named SwipeListAdapter.java. This class is a custom adapter class which inflates the list_row.xml by applying proper data.

SwipeListAdapter.java
package info.androidhive.swiperefresh.helper;
 
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
 
import java.util.List;
 
import info.androidhive.swiperefresh.R;
 
/**
 * Created by Ravi on 13/05/15.
 */
public class SwipeListAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private List<Movie> movieList;
    private String[] bgColors;
 
    public SwipeListAdapter(Activity activity, List<Movie> movieList) {
        this.activity = activity;
        this.movieList = movieList;
        bgColors = activity.getApplicationContext().getResources().getStringArray(R.array.movie_serial_bg);
    }
 
    @Override
    public int getCount() {
        return movieList.size();
    }
 
    @Override
    public Object getItem(int location) {
        return movieList.get(location);
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }
 
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
 
        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.list_row, null);
 
        TextView serial = (TextView) convertView.findViewById(R.id.serial);
        TextView title = (TextView) convertView.findViewById(R.id.title);
 
        serial.setText(String.valueOf(movieList.get(position).id));
        title.setText(movieList.get(position).title);
 
        String color = bgColors[position % bgColors.length];
        serial.setBackgroundColor(Color.parseColor(color));
 
        return convertView;
    }
 
}

10. Now we have all the required files in place, let’s start implementing the actual swipe refresh view.
Open the layout file of your main activity (activity_main.xml) and modify the layout as shown below. 
I have added a ListView to show list of movies and wrapped it around SwipeRefreshLayout to get the swipe to refresh.
activity_main.xml
<android.support.v4.widget.SwipeRefreshLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/swipe_refresh_layout"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">
 
    <ListView
        android:layout_width="fill_parent"
        android:layout_height="wrap_content"
        android:id="@+id/listView">
 
    </ListView>
 
</android.support.v4.widget.SwipeRefreshLayout>

11. Finally open MainActivity.java and do the below changes to achieve the swipe refresh list view.

> Implement the activity from SwipeRefreshLayout.OnRefreshListener and override the onRefresh() method.

> Call fetchMovies() which is a volley’s json array call to fetch the json and update the list view.

> onRefresh() is triggered whenever user swipes down the view.
So call fetchMovies() inside this method to get the next set of movies response.
MainActivity.java
package info.androidhive.swiperefresh.activity;
 
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;
 
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
 
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
 
import java.util.ArrayList;
import java.util.List;
 
import info.androidhive.swiperefresh.R;
import info.androidhive.swiperefresh.app.MyApplication;
import info.androidhive.swiperefresh.helper.Movie;
import info.androidhive.swiperefresh.helper.SwipeListAdapter;
 
 
public class MainActivity extends ActionBarActivity implements SwipeRefreshLayout.OnRefreshListener {
 
    private String TAG = MainActivity.class.getSimpleName();
 
    private String URL_TOP_250 = "https://api.androidhive.info/json/imdb_top_250.php?offset=";
 
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listView;
    private SwipeListAdapter adapter;
    private List<Movie> movieList;
 
    // initially offset will be 0, later will be updated while parsing the json
    private int offSet = 0;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
 
        listView = (ListView) findViewById(R.id.listView);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
 
        movieList = new ArrayList<>();
        adapter = new SwipeListAdapter(this, movieList);
        listView.setAdapter(adapter);
 
        swipeRefreshLayout.setOnRefreshListener(this);
 
        /**
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */
        swipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        swipeRefreshLayout.setRefreshing(true);
 
                                        fetchMovies();
                                    }
                                }
        );
 
    }
 
    /**
     * This method is called when swipe refresh is pulled down
     */
    @Override
    public void onRefresh() {
        fetchMovies();
    }
 
    /**
     * Fetching movies json by making http call
     */
    private void fetchMovies() {
 
        // showing refresh animation before making http call
        swipeRefreshLayout.setRefreshing(true);
 
        // appending offset to url
        String url = URL_TOP_250 + offSet;
 
        // Volley's json array request object
        JsonArrayRequest req = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, response.toString());
 
                        if (response.length() > 0) {
 
                            // looping through json and adding to movies list
                            for (int i = 0; i < response.length(); i++) {
                                try {
                                    JSONObject movieObj = response.getJSONObject(i);
 
                                    int rank = movieObj.getInt("rank");
                                    String title = movieObj.getString("title");
 
                                    Movie m = new Movie(rank, title);
 
                                    movieList.add(0, m);
 
                                    // updating offset value to highest value
                                    if (rank >= offSet)
                                        offSet = rank;
 
                                } catch (JSONException e) {
                                    Log.e(TAG, "JSON Parsing error: " + e.getMessage());
                                }
                            }
 
                            adapter.notifyDataSetChanged();
                        }
 
                        // stopping swipe refresh
                        swipeRefreshLayout.setRefreshing(false);
 
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Server Error: " + error.getMessage());
 
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
 
                // stopping swipe refresh
                swipeRefreshLayout.setRefreshing(false);
            }
        });
 
        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(req);
    }
 
}

Run the project and test it. 
You should able see the swipe refresh animation on app launch and list view updated each time you swipe down it.
