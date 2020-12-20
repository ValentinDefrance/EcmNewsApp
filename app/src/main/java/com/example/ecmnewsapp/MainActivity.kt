package com.example.ecmnewsapp

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.example.ecmnewsapp.Api.ApiClient
import com.example.ecmnewsapp.Api.ApiInterface
import com.example.ecmnewsapp.models.Article
import com.example.ecmnewsapp.models.News
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class MainActivity : AppCompatActivity(), OnRefreshListener {
    private var recyclerView: RecyclerView? = null
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var articles: MutableList<Article> = ArrayList()
    private var adapter: Adapter? = null
    private var topHeadline: TextView? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var errorLayout: RelativeLayout? = null
    private var errorImage: ImageView? = null
    private var errorTitle: TextView? = null
    private var errorMessage: TextView? = null
    private var btnRetry: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
        topHeadline = findViewById(R.id.topheadelines)
        recyclerView = findViewById(R.id.recyclerView)
        layoutManager = LinearLayoutManager(this@MainActivity)

    }

    fun LoadJson(keyword: String) {
        errorLayout!!.visibility = View.GONE
        swipeRefreshLayout!!.isRefreshing = true
        val apiInterface = ApiClient.getApiClient().create(
            ApiInterface::class.java
        )
        val country = Utils.getCountry()
        val language = Utils.getLanguage()
        val call: Call<News>
        call = if (keyword.length > 0) {
            apiInterface.getNewsSearch(keyword, language, "publishedAt", API_KEY)
        } else {
            apiInterface.getNews(country, API_KEY)
        }
        call.enqueue(object : Callback<News> {
            override fun onResponse(call: Call<News>, response: Response<News>) {
                if (response.isSuccessful && response.body()!!.article != null) {
                    if (!articles.isEmpty()) {
                        articles.clear()
                    }
                    articles = response.body()!!.article
                    adapter = Adapter(articles, this@MainActivity)
                    recyclerView!!.adapter = adapter
                    adapter!!.notifyDataSetChanged()
                    topHeadline!!.visibility = View.VISIBLE
                    swipeRefreshLayout!!.isRefreshing = false
                } else {
                    topHeadline!!.visibility = View.INVISIBLE
                    swipeRefreshLayout!!.isRefreshing = false
                    val errorCode: String
                    errorCode = when (response.code()) {
                        404 -> "404 not found"
                        500 -> "500 server broken"
                        else -> "unknown error"
                    }
                    showErrorMessage(
                        R.drawable.no_result,
                        "No Result",
                        """
                            Please Try Again!
                            $errorCode
                            """.trimIndent()
                    )
                }
            }

            override fun onFailure(call: Call<News>, t: Throwable) {
                topHeadline!!.visibility = View.INVISIBLE
                swipeRefreshLayout!!.isRefreshing = false
                showErrorMessage(
                    R.drawable.oops,
                    "Oops..",
                    """
                        Network failure, Please Try Again
                        $t
                        """.trimIndent()
                )
            }
        })
    }


    override fun onRefresh() {
        LoadJson("")
    }

    private fun onLoadingSwipeRefresh(keyword: String) {
        swipeRefreshLayout!!.post { LoadJson(keyword) }
    }

    private fun showErrorMessage(imageView: Int, title: String, message: String) {
        if (errorLayout!!.visibility == View.GONE) {
            errorLayout!!.visibility = View.VISIBLE
        }
        errorImage!!.setImageResource(imageView)
        errorTitle!!.text = title
        errorMessage!!.text = message
        btnRetry!!.setOnClickListener { onLoadingSwipeRefresh("") }
    }

    companion object {
        const val API_KEY = "4211f8a5a852445f9d864d916bfe22d6"
    }
}