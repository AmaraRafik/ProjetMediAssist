package com.example.projetmediassist.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projetmediassist.R
import com.example.projetmediassist.adapters.HomeVisitAdapter
import com.example.projetmediassist.database.AppDatabase
import com.example.projetmediassist.database.AppointmentDao
import com.example.projetmediassist.database.PatientDao
import com.example.projetmediassist.models.HomeVisitItem
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class HomeVisitActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HomeVisitAdapter

    private lateinit var appointmentDao: AppointmentDao
    private lateinit var patientDao: PatientDao

    private var doctorEmail: String? = null

    // Clé API Google
    private val GOOGLE_MAPS_API_KEY = "AIzaSyAS0wiGoU7bzeZ8oFpYjPjnCqLYfJjZgY8"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_visit)

        recyclerView = findViewById(R.id.home_visit_recycler)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val db = AppDatabase.getDatabase(this)
        appointmentDao = db.appointmentDao()
        patientDao = db.patientDao()

        val prefs = getSharedPreferences("session", 0)
        doctorEmail = prefs.getString("doctorEmail", null)

        if (doctorEmail == null) {
            Toast.makeText(this, "Erreur : médecin non connecté.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        loadHomeVisits()
    }

    private fun loadHomeVisits() {
        val email = doctorEmail ?: return

        lifecycleScope.launch {
            val patients = patientDao.getPatientsForDoctor(email)
            val appointments = appointmentDao.getAppointmentsForDoctor(email)
            val now = System.currentTimeMillis()

            val visitesDomicile = appointments
                .filter {
                    it.description?.contains("domicile", ignoreCase = true) == true &&
                            it.timeInMillis >= now
                }
                .sortedBy { it.timeInMillis }

            val homeVisitItems = visitesDomicile.mapNotNull { rdv ->
                val patient = patients.find { it.fullName == rdv.patient }
                if (patient?.address != null) {
                    HomeVisitItem(
                        patientName = patient.fullName,
                        hour = rdv.hour,
                        address = patient.address,
                        appointmentId = rdv.id
                    )
                } else null
            }

            withContext(Dispatchers.Main) {
                adapter = HomeVisitAdapter(homeVisitItems) { item ->
                    val intent = Intent(this@HomeVisitActivity, DetailAppointmentActivity::class.java)
                    intent.putExtra("APPOINTMENT_ID", item.appointmentId)
                    startActivity(intent)
                }
                recyclerView.adapter = adapter

                afficherMarqueursWeb(homeVisitItems)
            }
        }
    }

    private fun afficherMarqueursWeb(homeVisitItems: List<HomeVisitItem>) {
        if (homeVisitItems.isEmpty()) return

        lifecycleScope.launch(Dispatchers.IO) {
            var firstLatLng: LatLng? = null

            for ((index, item) in homeVisitItems.withIndex()) {
                val latLng = geoCodeAddressWeb(item.address)
                withContext(Dispatchers.Main) {
                    if (latLng != null) {
                        mMap.addMarker(
                            MarkerOptions()
                                .position(latLng)
                                .title("${index + 1}. ${item.patientName}")
                        )
                        if (firstLatLng == null) {
                            firstLatLng = latLng
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
                        }
                    }
                }
            }
        }
    }

    private suspend fun geoCodeAddressWeb(address: String): LatLng? = withContext(Dispatchers.IO) {
        try {
            val url = "https://maps.googleapis.com/maps/api/geocode/json?address=${address.replace(" ", "+")}&key=$GOOGLE_MAPS_API_KEY"
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()

            val body = response.body?.string() ?: return@withContext null
            val jsonObject = JSONObject(body)
            val results = jsonObject.getJSONArray("results")
            if (results.length() == 0) return@withContext null

            val location = results.getJSONObject(0)
                .getJSONObject("geometry")
                .getJSONObject("location")
            val lat = location.getDouble("lat")
            val lng = location.getDouble("lng")
            LatLng(lat, lng)
        } catch (e: Exception) {
            null
        }
    }
}
