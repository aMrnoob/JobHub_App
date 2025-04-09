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
        onStart: (() -> Unit)? = null,
        onComplete: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null,
        onFailure: ((Throwable) -> Unit)? = null
    ) {
        onStart?.invoke()

        call.enqueue(object : Callback<ApiResponse<T>> {
            override fun onResponse(call: Call<ApiResponse<T>>, response: Response<ApiResponse<T>>) {
                onComplete?.invoke()
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.isSuccess == true) {
                        if (!apiResponse.message.isNullOrBlank()) {
                            showToast(context, apiResponse.message)
                        }
                        onSuccess(apiResponse.data)
                    } else {
                        val message = apiResponse?.message ?: "Unknown error from server"
                        onError?.invoke(message) ?: showToast(context, message)
                        Log.e("ApiHelper", "API responded with failure: $message")
                    }
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown API error"
                    onError?.invoke(errorMessage) ?: showToast(context, "API Error: $errorMessage")
                    Log.e("ApiHelper", "API Error: HTTP ${response.code()} - $errorMessage")
                }
            }

            override fun onFailure(call: Call<ApiResponse<T>>, t: Throwable) {
                onComplete?.invoke()
                onFailure?.invoke(t) ?: showToast(context, "Request failed: ${t.localizedMessage ?: "Unknown error"}")
                Log.e("ApiHelper", "API Request failed", t)
            }
        })
    }

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
