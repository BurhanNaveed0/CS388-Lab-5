package com.codepath.campgrounds

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

private const val TAG = "CampgroundDetailActivity"
const val CAMPGROUND_EXTRA = "CAMPGROUND_EXTRA"

class DetailActivity : AppCompatActivity() {
    private lateinit var titleTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var locationTextView: TextView
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // Updated view IDs
        titleTextView = findViewById(R.id.detailTitle)
        descriptionTextView = findViewById(R.id.detailDescription)
        locationTextView = findViewById(R.id.detailLocation)
        imageView = findViewById(R.id.detailImageView)

        val campground: Campground? =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                intent.getSerializableExtra(CAMPGROUND_EXTRA, Campground::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getSerializableExtra(CAMPGROUND_EXTRA) as? Campground
            }

        campground?.let {
            titleTextView.text = it.name
            descriptionTextView.text = it.description
            locationTextView.text = it.latLong

            Glide.with(this)
                .load(it.imageUrl)
                .into(imageView)
        }
    }
}
