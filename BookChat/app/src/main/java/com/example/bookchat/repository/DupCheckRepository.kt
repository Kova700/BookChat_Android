package com.example.bookchat.repository

import android.util.Log
import android.widget.Toast
import com.example.bookchat.App
import com.example.bookchat.utils.Constants.TAG
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DupCheckRepository {

    fun duplicateCheck(callback : (Boolean) -> Unit){
        if(!isNetworkConnected()) {
            Toast.makeText(App.instance.applicationContext,"네트워크가 연결되어 있지 않습니다.\n네트워크를 연결해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        val call = App.instance.apiInterface.nicknameDuplicateCheck()
        call.enqueue(CallbackAction(callback))
    }
    private fun isNetworkConnected() :Boolean{
        return App.instance.networkManager.checkNetworkState()
    }

    inner class CallbackAction(val callback : (Boolean) -> Unit) : Callback<Any> {
        override fun onResponse(call: Call<Any>, response: Response<Any>) {
            if(response.isSuccessful){
                Log.d(TAG, "CallbackAction: onResponse() - Success(통신 성공) response.body() : ${response.body()}")
                Log.d(TAG, "CallbackAction: onResponse() - response.code() : ${response.code()}")
                callback(true)
                return
            }
            Log.d(TAG, "CallbackAction: onResponse() - Success-fail(통신 실패) response.body() : ${response.body()}")
            Log.d(TAG, "CallbackAction: onResponse() - response.code() : ${response.code()}")
            callback(false)

        }

        override fun onFailure(call: Call<Any>, t: Throwable) {
            Log.d(TAG, "CallbackAction: onResponse() - Fail(통신 실패) response.body() : ${t.message}")
            callback(false)
        }

    }
}