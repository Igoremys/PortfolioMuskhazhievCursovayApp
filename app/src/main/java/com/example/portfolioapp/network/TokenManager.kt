package com.example.portfolioapp.network

import android.content.Context

object TokenManager {

    private var cachedToken: String? = null
    private var cachedUserId: Long = -1
    private var appContext: Context? = null  

    fun init(context: Context) {
        appContext = context.applicationContext
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        cachedToken = prefs.getString("access_token", null)
        cachedUserId = prefs.getLong("user_id", -1)
    }

    fun saveToken(context: Context, token: String, userId: Long) {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("access_token", token)
            .putLong("user_id", userId)
            .apply()

        cachedToken = token
        cachedUserId = userId
    }

    fun getToken(context: Context): String? {
        return cachedToken ?: run {
            val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            prefs.getString("access_token", null)?.also { cachedToken = it }
        }
    }

    fun getToken(): String? {
        return cachedToken ?: run {
            appContext?.let { ctx ->
                val prefs = ctx.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                prefs.getString("access_token", null)?.also { cachedToken = it }
            }
        }
    }

    fun getUserId(context: Context): Long {
        return if (cachedUserId != -1L) {
            cachedUserId
        } else {
            val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            prefs.getLong("user_id", -1).also { cachedUserId = it }
        }
    }

    fun getUserId(): Long? {
        return if (cachedUserId != -1L) {
            cachedUserId
        } else {
            appContext?.let { ctx ->
                val prefs = ctx.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                prefs.getLong("user_id", -1).takeIf { it != -1L }?.also { cachedUserId = it }
            }
        }
    }

    fun clearToken(context: Context) {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        cachedToken = null
        cachedUserId = -1
    }
}
