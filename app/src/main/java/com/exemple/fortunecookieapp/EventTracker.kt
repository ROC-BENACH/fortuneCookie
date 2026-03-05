package com.exemple.fortunecookieapp

import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics

object EventTracker {

    private val analytics: FirebaseAnalytics = Firebase.analytics

    fun logLoginSuccess(userId: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, "anonymous")
            putString("user_id", userId)
            putLong("timestamp", System.currentTimeMillis())
        }
        analytics.logEvent("login_success", bundle)
    }

    fun logLoginError(error: String) {
        val bundle = Bundle().apply {
            putString("error", error)
            putLong("timestamp", System.currentTimeMillis())
        }
        analytics.logEvent("login_error", bundle)
    }

    fun logCookieGenerated(cookieId: String, category: String, userId: String) {
        val bundle = Bundle().apply {
            putString("cookie_id", cookieId)
            putString("category", category)
            putString("user_id", userId)
            putLong("timestamp", System.currentTimeMillis())
        }
        analytics.logEvent("cookie_generated", bundle)
    }

    fun logFirstCookieOpen() {
        val bundle = Bundle().apply {
            putLong("timestamp", System.currentTimeMillis())
        }
        analytics.logEvent("first_cookie_open", bundle)
    }

    fun logCookieReload(phraseId: String, previousPhraseId: String, userId: String) {
        val bundle = Bundle().apply {
            putString("phrase_id", phraseId)
            putString("previous_phrase_id", previousPhraseId)
            putString("user_id", userId)
            putLong("timestamp", System.currentTimeMillis())
        }
        analytics.logEvent("cookie_reload", bundle)
    }

    fun logCopyPhrase(phrase: String, phraseId: String, userId: String) {
        val bundle = Bundle().apply {
            putString("phrase", phrase.take(100))
            putString("phrase_id", phraseId)
            putString("user_id", userId)
            putLong("timestamp", System.currentTimeMillis())
        }
        analytics.logEvent("copy_phrase", bundle)
    }

    fun logSharePhrase(phrase: String, phraseId: String, shareMethod: String, userId: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, shareMethod)
            putString("phrase", phrase.take(100))
            putString("phrase_id", phraseId)
            putString("user_id", userId)
            putLong("timestamp", System.currentTimeMillis())
        }
        analytics.logEvent("share_phrase", bundle)
    }

    fun logPhraseViewed(phraseId: String, durationMs: Long, userId: String) {
        val bundle = Bundle().apply {
            putString("phrase_id", phraseId)
            putLong("view_duration_ms", durationMs)
            putString("user_id", userId)
            putLong("timestamp", System.currentTimeMillis())
        }
        analytics.logEvent("phrase_viewed", bundle)
    }

    fun logSoundPlayed(soundName: String, success: Boolean) {
        val bundle = Bundle().apply {
            putString("sound_name", soundName)
            putBoolean("success", success)
            putLong("timestamp", System.currentTimeMillis())
        }
        analytics.logEvent("sound_played", bundle)
    }

    fun logVibration(durationMs: Long, success: Boolean) {
        val bundle = Bundle().apply {
            putLong("duration_ms", durationMs)
            putBoolean("success", success)
            putLong("timestamp", System.currentTimeMillis())
        }
        analytics.logEvent("vibration", bundle)
    }

    fun logScreenView(screenName: String, screenClass: String, userId: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
            putString("user_id", userId)
            putLong("timestamp", System.currentTimeMillis())
        }
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    fun logAppStart(userId: String) {
        val bundle = Bundle().apply {
            putString("user_id", userId)
            putLong("timestamp", System.currentTimeMillis())
        }
        analytics.logEvent("app_start", bundle)
    }

    fun logAppClose(userId: String, sessionDuration: Long) {
        val bundle = Bundle().apply {
            putString("user_id", userId)
            putLong("session_duration", sessionDuration)
            putLong("timestamp", System.currentTimeMillis())
        }
        analytics.logEvent("app_close", bundle)
    }

    fun logSessionEnd(sessionDuration: Long, userId: String) {
        val bundle = Bundle().apply {
            putString("user_id", userId)
            putLong("session_duration", sessionDuration)
            putLong("timestamp", System.currentTimeMillis())
        }
        analytics.logEvent("session_end", bundle)
    }

    fun logError(errorType: String, errorMessage: String, userId: String) {
        val bundle = Bundle().apply {
            putString("error_type", errorType)
            putString("error_message", errorMessage.take(200))
            putString("user_id", userId)
            putLong("timestamp", System.currentTimeMillis())
        }
        analytics.logEvent("app_error", bundle)
    }
}