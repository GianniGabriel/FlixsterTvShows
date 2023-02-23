package com.example.flixstertvshows

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

import android.os.Parcelable
import androidx.appcompat.widget.Toolbar
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TvShow(
    val name: String,
    val poster_path: String,
    val overview: String,
    val first_air_date: String,
    val vote_average: Double,
    val vote_count: Int
) : Parcelable


data class TvShowsResponse(
    @SerializedName("results") val results: List<TvShow>
)

class TvShowsAdapter(
    private val tvShows: List<TvShow>,
    private val onClick: (TvShow) -> Unit
) : RecyclerView.Adapter<TvShowsAdapter.TvShowViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TvShowViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tv_show_row, parent, false)
        return TvShowViewHolder(view)
    }

    override fun onBindViewHolder(holder: TvShowViewHolder, position: Int) {
        val tvShow = tvShows[position]

        Glide.with(holder.itemView)
            .load("https://image.tmdb.org/t/p/w500${tvShow.poster_path}")
            .into(holder.thumbnail)

        holder.title.text = tvShow.name
        holder.rating.text = tvShow.vote_average.toString()

        holder.itemView.setOnClickListener {
            onClick(tvShow)
        }
    }

    override fun getItemCount() = tvShows.size

    inner class TvShowViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val thumbnail: ImageView = view.findViewById(R.id.thumbnail)
        val title: TextView = view.findViewById(R.id.title)
        val rating: TextView = view.findViewById(R.id.rating)
    }
}

class TvShowDetailsActivity : AppCompatActivity() {

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tv_show_details)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        fun onSupportNavigateUp(): Boolean {
            onBackPressed()
            return true
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        val tvShow = intent.getParcelableExtra<TvShow>(MainActivity.TV_SHOW_EXTRA_KEY)
        val thumbnail = findViewById<ImageView>(R.id.thumbnail)
        if(tvShow != null) {
            Glide.with(this)
                .load("https://image.tmdb.org/t/p/w500${tvShow.poster_path}")
                .into(thumbnail)

            findViewById<TextView>(R.id.title).text = tvShow.name
            findViewById<TextView>(R.id.rating).text = "Rating: ${tvShow.vote_average.toString()}"
            findViewById<TextView>(R.id.review_count).text = "Total reviews: ${tvShow.vote_count.toString()}"
            findViewById<TextView>(R.id.first_air_date).text = "Air Date: ${tvShow.first_air_date}"
            findViewById<TextView>(R.id.description).text = tvShow.overview
        }
    }
}

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    companion object {
        const val TV_SHOW_EXTRA_KEY = "TV_SHOW"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val client = OkHttpClient()

        val request = Request.Builder()
            .url("https://api.themoviedb.org/3/tv/popular?api_key=a07e22bc18f5cb106bfe4cc1f83ad8ed&language=en-US&page=1")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val gson = Gson()
                val tvShowsResponse = gson.fromJson(response.body?.string(), TvShowsResponse::class.java)

                runOnUiThread {
                    recyclerView.adapter = TvShowsAdapter(tvShowsResponse.results) { tvShow ->
                        val intent = Intent(this@MainActivity, TvShowDetailsActivity::class.java)
                        intent.putExtra(TV_SHOW_EXTRA_KEY, tvShow)
                        startActivity(intent)
                    }
                }
            }
        })
    }
}