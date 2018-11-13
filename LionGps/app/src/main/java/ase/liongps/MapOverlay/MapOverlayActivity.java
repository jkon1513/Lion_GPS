package ase.liongps.MapOverlay;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import ase.liongps.R;


public class MapOverlayActivity extends AppCompatActivity
        implements OnMapReadyCallback, MapOverlayContract.View {

    //widgets
    EditText searchBar;
    ListView leftPanel;
    GoogleMap map;

    private final String TAG = MapOverlayActivity.class.getName();
    private MapOverlayContract.Presenter presenter;

    // temp fields for Search History until user class implemented
    public static List<String> searchHistory;
    public static ArrayAdapter adapter;

    /* Firestore connection established here */
    public static FirebaseFirestore db = FirebaseFirestore.getInstance(); // move to model


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_overlay);
        presenter = new MapOverlayPresenter(this);

        //searchBar
        searchBar = (EditText) findViewById(R.id.searchText);
        showSearchBar();

        //Map
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //user panel
        leftPanel = (ListView) findViewById(R.id.left_drawer);
    }

    // Map Logic -------------------------------------------------------------------

    @Override
    public void onMapReady(GoogleMap theMap) {
        map = theMap;
        presenter.initMap();
    }

    public void centerCamera(LatLng geoLocation, float zoom){
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(geoLocation , zoom));
    }

    public void placeMarker(LatLng geoLocation, String markerName){
        map.addMarker(new MarkerOptions().position(geoLocation).title(markerName));
    }

    /*
    Routing is not implemented yet. for now this will take a geo-location and center the camera
    ontop of it
    */
    @Override
    public void showRoute(LatLng geoLocation) {
        map.moveCamera(CameraUpdateFactory.newLatLng(geoLocation));
    }

    // Search logic --------------------------------------------------------------------

    public void showSearchBar() {
        Log.d(TAG, "showSearchBar: initializing");


        //overides the enter button of keyboard to search and not create new lines
        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event.getAction() == event.ACTION_DOWN
                        || event.getAction() == event.KEYCODE_ENTER){

                    presenter.handleSearch(searchBar.getText().toString().toLowerCase().trim());
                }

                return false;
            }
        });
    }

    @Override
    public void onSearchError() {
        Toast.makeText(this, "that building is not in our records just yet", Toast.LENGTH_LONG).show();
    }


    @Override
    public void launchProfilePage() {
        //TODO: implement
    }

    @Override
    public void showRecentSearches() {
        //TODO: implement
    }




    // -------MOVE TO MODEL ---------------------------------------------------------------

        /*
    Takes a string input and stores it in the cloud as a Map<String, String> entry.
    This uses the .add() method, which auto-generates a key for the document we are
    adding to our "Search History" Collection. This is opposed to .set(), which re-
    quires we specify a key
     */

    public void saveToSearchHistory(String input) {
        HashMap<String, String> searchValue = new HashMap<>();
        searchValue.put("entry", input);


        db.collection("Search History")
                .add(searchValue)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "Search String Log => DocumentSnapshot added with ID: " + documentReference.getId());
                        }
                })
                .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Search String Log => Error adding document", e);
                        }
                });

    }

    public void loadFromSearchHistory() {
        db.collection("Search History")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> tmp = document.getData();
                                String searchField = (String) tmp.get("entry");
                                adapter.add(searchField);
                                Log.d(TAG, "Pull Data Method => String retrieved is: " + searchField);
                            }
                        }
                        else {
                            Log.d(TAG, "Pull Data Method => Error getting documents: ", task.getException());
                        }

                    }
                });
    }


}
