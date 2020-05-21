package com.asiantech.intern2019winterfinal.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.asiantech.intern2019winterfinal.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class LocationFragment : Fragment(), OnMapReadyCallback {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_location, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment: MapFragment = (activity)?.fragmentManager?.findFragmentById(R.id.fragmentLocation) as MapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        val location = LatLng(16.0808905, 108.2384487)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 0F))
        googleMap?.addMarker(
            MarkerOptions()
                .title("Asian Tech")
                .snippet("Best Restaurant in Da Nang City")
                .position(location)
        )
    }
}
