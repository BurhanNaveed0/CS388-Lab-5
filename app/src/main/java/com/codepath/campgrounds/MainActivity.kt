package com.codepath.campgrounds

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codepath.campgrounds.databinding.ActivityMainBinding
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.Headers
import org.json.JSONException

fun createJson() = Json {
    isLenient = true
    ignoreUnknownKeys = true
    useAlternativeNames = false
}

private const val TAG = "CampgroundsMain/"
private val PARKS_API_KEY = BuildConfig.API_KEY
private val CAMPGROUNDS_URL =
    "https://developer.nps.gov/api/v1/campgrounds?api_key=${PARKS_API_KEY}"

class MainActivity : AppCompatActivity() {
    private lateinit var campgroundRecyclerView: RecyclerView
    private lateinit var binding: ActivityMainBinding

    private val campgrounds = mutableListOf<Campground>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Updated ID reference
        campgroundRecyclerView = findViewById(R.id.campgroundRecycler)

        val campgroundAdapter = CampgroundAdapter(this, campgrounds)
        campgroundRecyclerView.adapter = campgroundAdapter

        lifecycleScope.launch {
            (application as CampgroundApplication).db.campgroundDao().getAll().collect { databaseList ->
                databaseList.map { entity ->
                    Campground(
                        entity.name,
                        entity.description,
                        entity.latLong,
                        listOf(CampgroundImage(entity.imageUrl, null))
                    )
                }.also { mappedList ->
                    campgrounds.clear()
                    campgrounds.addAll(mappedList)
                    campgroundAdapter.notifyDataSetChanged()
                }
            }
        }

        campgroundRecyclerView.layoutManager = LinearLayoutManager(this).also {
            val dividerItemDecoration = DividerItemDecoration(this, it.orientation)
            campgroundRecyclerView.addItemDecoration(dividerItemDecoration)
        }

        val client = AsyncHttpClient()
        client.get(CAMPGROUNDS_URL, object : JsonHttpResponseHandler() {
            override fun onFailure(
                statusCode: Int,
                headers: Headers?,
                response: String?,
                throwable: Throwable?
            ) {
                Log.e(TAG, "Failed to fetch campgrounds: $statusCode")
            }

            override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                Log.i(TAG, "Successfully fetched campgrounds: $json")
                try {
                    val parsedJson = createJson().decodeFromString(
                        CampgroundResponse.serializer(),
                        json.jsonObject.toString()
                    )
                    parsedJson.data?.let { list ->
                        lifecycleScope.launch(IO) {
                            (application as CampgroundApplication).db.campgroundDao().deleteAll()
                            (application as CampgroundApplication).db.campgroundDao().insertAll(list.map {
                                CampgroundEntity(
                                    name = it.name,
                                    description = it.description,
                                    latLong = it.latLong,
                                    imageUrl = it.imageUrl
                                )
                            })
                        }
                    }
                } catch (e: JSONException) {
                    Log.e(TAG, "Exception: $e")
                }
            }
        })
    }
}
