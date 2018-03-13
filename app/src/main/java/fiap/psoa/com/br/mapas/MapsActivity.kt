package fiap.psoa.com.br.mapas

import android.Manifest
import android.location.Address
import android.location.Geocoder
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*
import android.content.DialogInterface
import android.Manifest.permission
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.location.Location
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.util.Log
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import java.text.DateFormat
import java.util.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private val INTERVAL = (1000 * 10).toLong()
    private val FASTEST_INTERVAL = (1000 * 5).toLong()
    var mCurrentLocation: Location? = null
    var mLastUpdateTime: String? = null
    lateinit var mLocationRequest: LocationRequest;
    override fun onLocationChanged(location: Location?) {
        Log.d("TIAGO", "Firing onLocationChanged..............................................")
        mCurrentLocation = location
        mLastUpdateTime = DateFormat.getTimeInstance().format(Date())
        updateUI()
    }

    private fun updateUI() {
        Log.d("TIAGO", "UI update initiated .............")
        if (null != mCurrentLocation) {
            val lat = mCurrentLocation?.getLatitude().toString()
            val lng = mCurrentLocation?.getLongitude().toString()
            Log.d("TIAGO", "At Time: " + mLastUpdateTime + "\n" +
                    "Latitude: " + lat + "\n" +
                    "Longitude: " + lng + "\n" +
                    "Accuracy: " + mCurrentLocation?.getAccuracy() + "\n" +
                    "Provider: " + mCurrentLocation?.getProvider())
        } else {
            Log.d("TIAGO", "location is null ...............")
        }
    }

    override fun onConnected(p0: Bundle?) {
        checkPermission()
        //val minhaLocalizacao = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)
        if (mCurrentLocation != null) {
            adicionarMarcador(mCurrentLocation!!.getLatitude(), mCurrentLocation!!.getLongitude(), "Nao sou Shajura mas estoy aqui")
        }
    }

    val REQUEST_GPS = 0

    private fun checkPermission() {
        val permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i("", "Permissão para gravar negada")

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                val builder = AlertDialog.Builder(this)

                builder.setMessage("Necessária a permissao para GPS")
                        .setTitle("Permissao Requerida")

                builder.setPositiveButton("OK") { dialog, id ->
                    //Log.i(FragmentActivity.TAG, "Clicked")
                    requestPermission()
                }

                val dialog = builder.create()
                dialog.show()

            } else {
                requestPermission()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            REQUEST_GPS -> {
                if (grantResults.size == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i("TIAGO", "Permissão negada pelo usuário")
                } else {
                    Log.i("TIAGO", "Permissao concedida pelo usuario")
                }
                return
            }
        }
    }

    protected fun requestPermission() {
        ActivityCompat.requestPermissions(this,
                arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_GPS)
    }

    override fun onConnectionSuspended(p0: Int) {
        Log.i("TIAGO", "onConnectionSuspended")
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.i("TIAGO", "onConnectionFailed")
    }

    private lateinit var mMap: GoogleMap
    private lateinit var mGoogleApiClient: GoogleApiClient

    @Synchronized
    fun callConnection() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API).build()
        mGoogleApiClient.connect()
    }

    fun createLocationRequest() {
        mLocationRequest = LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        btPesquisar.setOnClickListener {

            mMap.clear() //limpar marcadores antes de adicionar qualquer outro

            val geocoder = Geocoder(this)
            var address: List<Address>?

            address = geocoder.getFromLocationName(etEndereco.text.toString(), 1)
            if (address == null || address.isEmpty()) {
                var alert = AlertDialog.Builder(this).create()
                alert.setTitle("Deu ruim")
                alert.setMessage("Endereco nao encontrado")

                alert.setCancelable(false)

                alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK", { dialogInterface, inteiro ->
                    alert.dismiss()
                })
                alert.show()
            } else {
                val location = address[0]
                adicionarMarcador(location.latitude, location.longitude, etEndereco.text.toString())
            }
        }
        createLocationRequest();
    }

    fun adicionarMarcador(latitude: Double, longitude: Double, title: String) {
        // Add a marker in Sydney and move the camera
        val sydney = LatLng(latitude, longitude)
        mMap.addMarker(MarkerOptions().position(sydney).title(title)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 16f))

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
        callConnection()
    }
}
