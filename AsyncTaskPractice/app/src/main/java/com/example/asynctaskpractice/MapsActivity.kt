package com.example.asynctaskpractice

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.asynctaskpractice.data.Library

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.asynctaskpractice.databinding.ActivityMapsBinding
import com.google.android.gms.maps.model.LatLngBounds
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {


    //서울 도서관 api key
    //42666e656b676f6c3933794d767568
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        loadLibraries()
        // Add a marker in Sydney and move the camera
//        val sydney = LatLng(-34.0, 151.0)
//        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }


    fun loadLibraries(){
        val retrofit = Retrofit.Builder()
            .baseUrl(SeoulOpenAPI.DOMAIN)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(SeoulOpenService::class.java)


        service.getLibraries(SeoulOpenAPI.API_KEY, 200)
            .enqueue(object : Callback<Library>{
                override fun onFailure(call: Call<Library>, t: Throwable) {
                    Toast.makeText(this@MapsActivity,"데이터를 가져올수없습니다",Toast.LENGTH_LONG).show()
                }
                override fun onResponse(call: Call<Library>, response: Response<Library>) {
                    val result = response.body()
                    showLibraries(result)
                }

            })
    }

    fun showLibraries(result:Library?){

        result?.let{
            val latlngBounds = LatLngBounds.Builder()
            for(library in it.SeoulPublicLibraryInfo.row){

                val position = LatLng(library.XCNTS.toDouble(), library.YDNTS.toDouble())
                val marker = MarkerOptions().position(position).title(library.LBRRY_NAME)
                mMap.addMarker(marker)

                latlngBounds.include(position)
            }

            val bounds = latlngBounds.build()
            val padding = 0


            val camera = CameraUpdateFactory.newLatLngBounds(bounds, padding)
            mMap.moveCamera(camera)
        }


    }
}