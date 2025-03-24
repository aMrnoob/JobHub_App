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
        onSuccess: (T?) -> Unit,
        onFailure: (Throwable) -> Unit = { t ->
            Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
        }
    ) {
        call.enqueue(object : Callback<ApiResponse<T>> {
            override fun onResponse(call: Call<ApiResponse<T>>, response: Response<ApiResponse<T>>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.isSuccess == true) {
                        onSuccess(apiResponse.data)
                    } else {
                        Toast.makeText(context, "Failed: ${apiResponse?.message ?: "Unknown error"}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "API Call Failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<T>>, t: Throwable) {
                onFailure(t)
            }
        })
    }
}