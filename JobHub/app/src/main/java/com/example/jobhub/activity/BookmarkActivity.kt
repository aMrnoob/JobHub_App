package com.example.jobhub.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jobhub.adapter.BookmarkAdapter
import com.example.jobhub.config.ApiHelper
import com.example.jobhub.config.RetrofitClient
import com.example.jobhub.config.SharedPrefsManager
import com.example.jobhub.databinding.ActivityBookmarkBinding
import com.example.jobhub.dto.BookmarkRequest
import com.example.jobhub.dto.ItemJobDTO
import com.example.jobhub.entity.enumm.JobAction
import com.example.jobhub.service.BookmarkService

class BookmarkActivity : BaseActivity() {

    private lateinit var binding: ActivityBookmarkBinding
    private lateinit var sharedPrefs: SharedPrefsManager
    private lateinit var bookmarkAdapter: BookmarkAdapter

    private val listJobs: MutableList<ItemJobDTO> = mutableListOf()

    private val bookmarkService: BookmarkService by lazy { RetrofitClient.createRetrofit().create(BookmarkService::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookmarkBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPrefs = SharedPrefsManager(this)

        setupRecyclerView()
        loadBookmarkJobs()
    }

    private fun setupRecyclerView() {
        bookmarkAdapter = BookmarkAdapter(
            listJobs,
            onActionClick = { selectedJob, action ->
                when (action) {
                    JobAction.CLICK -> {
                        sharedPrefs.saveCurrentJob(selectedJob)
                        startActivity(Intent(this, InforJobActivity::class.java))
                    }

                    JobAction.BOOKMARK -> {
                        val userId = sharedPrefs.userId
                        val jobId = selectedJob.jobId
                        removeBookmark(BookmarkRequest(userId, jobId))
                    }

                    JobAction.APPLY -> {
                        val intent = Intent(this, ApplyJobActivity::class.java)
                        sharedPrefs.saveCurrentJob(selectedJob)
                        startActivity(intent)
                    }

                    else -> {}
                }
            }
        )

        binding.rvApplications.apply {
            adapter = bookmarkAdapter
            layoutManager = LinearLayoutManager(this@BookmarkActivity)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadBookmarkJobs() {
        val token = sharedPrefs.authToken ?: return

        ApiHelper().callApi(
            context = this,
            call = bookmarkService.getBookmarkedJobsByUser("Bearer $token"),
            onSuccess = { response ->
                listJobs.clear()
                response?.let { listJobs.addAll(it) }
                bookmarkAdapter.notifyDataSetChanged()
            },
        )
    }

    private fun removeBookmark(bookmarkRequest: BookmarkRequest) {
        ApiHelper().callApi(
            context = this,
            call = bookmarkService.deleteBookMark(bookmarkRequest),
            onSuccess = { loadBookmarkJobs() }
        )
    }
}