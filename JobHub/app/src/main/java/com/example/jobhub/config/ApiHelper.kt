package com.example.jobhub.config

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.jobhub.model.ApiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ApiHelper {
    fun <T> callApi(
        context: Context,
        call: Call<ApiResponse<T>>,
        onSuccess: (T?) -> Unit,
        onError: ((Throwable?) -> Unit)? = null,
        onStart: (() -> Unit)? = null,
        onComplete: (() -> Unit)? = null
    ) {
        onStart?.invoke()
        call.enqueue(object : Callback<ApiResponse<T>> {
            override fun onResponse(call: Call<ApiResponse<T>>, response: Response<ApiResponse<T>>) {
                onComplete?.invoke()
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        if (!apiResponse.message.isNullOrBlank()) {
                            showToast(context, apiResponse.message)
                        }
                        onSuccess(apiResponse.data)
                    } else {
                        apiResponse?.message?.let { showToast(context, it) }
                        Log.e("ApiHelper", "API responded with failure.")
                        onError?.invoke(null)
                    }
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown API error"
                    showToast(context, "API Error: $errorMessage")
                    Log.e("ApiHelper", "API Error: HTTP ${response.code()} - $errorMessage")
                    onError?.invoke(null)
                }
            }

            override fun onFailure(call: Call<ApiResponse<T>>, t: Throwable) {
                onComplete?.invoke()
                showToast(context, "Request failed: ${t.localizedMessage ?: "Unknown error"}")
                Log.e("ApiHelper", "API Request failed", t)
                onError?.invoke(t)
            }
        })
    }

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
