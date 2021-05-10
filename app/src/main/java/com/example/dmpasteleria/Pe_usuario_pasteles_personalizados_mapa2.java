package com.example.dmpasteleria;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Pe_usuario_pasteles_personalizados_mapa2 extends FragmentActivity implements OnMapReadyCallback {
    FirebaseFirestore fStore;
    FirebaseAuth fAuth;
    GoogleMap map;
    SupportMapFragment mapFragment;
    SearchView searchView;
    FirebaseUser Usuario;
    private static final String TAG = "Pedidos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        Usuario = fAuth.getCurrentUser();
        setContentView(R.layout.activity_pe_usuario_pasteles_personalizados_mapa2);
        searchView = findViewById(R.id.sv_location);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.google_map);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = searchView.getQuery().toString();
                List<Address> addressList = null;

                if(location != null || !(location.equals(""))){
                    Geocoder geocoder = new Geocoder(Pe_usuario_pasteles_personalizados_mapa2.this);
                    try{
                        addressList = geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (addressList == null  || addressList.size() == 0) {
                        Toast.makeText(Pe_usuario_pasteles_personalizados_mapa2.this, "address_not_found", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    Address address = addressList.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(),address.getLongitude());
                    System.out.println(address);
                    map.addMarker(new MarkerOptions().position(latLng).title(location));
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));

                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

    }

    public void Enviar(View view) {
        //final DocumentReference docref = FirebaseFirestore.getInstance()
        //.collection("pedidos")
        //.document();
        //String searchView_2 = searchView.toString();
        //Map<String, Object> map_1 = new HashMap<>();
        //map_1.put("direccion", searchView_2);
        //docref.update(map_1)
        if(fAuth.getCurrentUser() == null) {
            startActivity(new Intent(getApplicationContext(), Pe_inicio.class));
            finish();
        }

        String Usuario_2 = null;
        Usuario_2 = Usuario.toString();
        fStore.collection("pedidos")
                .whereEqualTo("Estado del pedido", 0)
                .whereEqualTo("Usuario", Usuario_2)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                final DocumentReference docref = FirebaseFirestore.getInstance()
                                .collection("pedidos")
                                .document(document.getId());
                                String searchView_2 = searchView.toString();
                                Map<String, Object> map_1 = new HashMap<>();
                                map_1.put("direccion", searchView_2);
                                docref.update(map_1)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                startActivity(new Intent(getApplicationContext(), Pe_usuario_pedidos.class));

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });

                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

}