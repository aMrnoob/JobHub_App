package com.example.jobhub.config

import android.content.Context
import android.widget.Toast
import com.example.jobhub.model.ApiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ApiHelper {
    fun <T> callApi(
        context: Context,
        call: Call<ApiResponse<T>>,
        onSuccess: (T?) -> Unit
    ) {
        call.enqueue(object : Callback<ApiResponse<T>> {
            override fun onResponse(call: Call<ApiResponse<T>>, response: Response<ApiResponse<T>>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.isSuccess == true) {
                        if (!apiResponse.message.isNullOrBlank()) {
                            showToast(context, apiResponse.message)
                        }
                        onSuccess(apiResponse.data)
                    } else {
                        showToast(context, apiResponse?.message ?: "Unknown error")
                    }
                } else {
                    showToast(context, "API Error: ${response.errorBody()?.string() ?: "Unknown error"}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<T>>, t: Throwable) {
                showToast(context, "Request failed: ${t.message}")
            }
        })
    }

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}

