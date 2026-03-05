package com.exemple.fortunecookieapp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var tvFortune: TextView
    private lateinit var ivCookie: ImageView
    private lateinit var btnReload: ImageButton
    private lateinit var btnCopy: ImageButton
    private lateinit var btnShare: ImageButton
    private lateinit var fortunes: Array<String>
    private lateinit var btnThemeToggle: ImageButton

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var vibrator: Vibrator
    private var isCookieOpen = false

    private lateinit var firebaseAnalytics: FirebaseAnalytics


    private var rewardedAd: RewardedAd? = null
    private val TAG = "AdMob"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseAnalytics = Firebase.analytics

        val auth = FirebaseAuth.getInstance()
        auth.signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                } else {
                }
            }

        tvFortune = findViewById(R.id.tvFortune)
        ivCookie = findViewById(R.id.ivCookie)
        btnReload = findViewById(R.id.btnReload)
        btnCopy = findViewById(R.id.btnCopy)
        btnShare = findViewById(R.id.btnShare)
        btnThemeToggle = findViewById(R.id.btnThemeToggle)

        // Restaurar tema guardat
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val isDark = prefs.getBoolean("dark_mode", false)
        updateThemeIcon(isDark)

        btnThemeToggle.setOnClickListener {
            val currentlyDark = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
            val newIsDark = !currentlyDark
            prefs.edit().putBoolean("dark_mode", newIsDark).apply()
            applyTheme(newIsDark)
        }

        fortunes = resources.getStringArray(R.array.fortunes)

        mediaPlayer = MediaPlayer.create(this, R.raw.cookie_break)

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        //  INICIA ADMOB
        MobileAds.initialize(this) {}
        loadRewardedAd()

        ivCookie.setOnClickListener {
            if (!isCookieOpen) {
                playCookieSound()
                vibrate(200)
                ivCookie.setImageResource(R.drawable.cookie_opened)
                isCookieOpen = true
                EventTracker.logFirstCookieOpen()
            }
            showRandomFortune()
            EventTracker.logCookieGenerated(
                cookieId = tvFortune.text.toString().take(20),
                category = "general",
                userId = firebaseAnalytics.appInstanceId.toString()
            )
        }

        // RELOAD ADMOB
        btnReload.setOnClickListener {
            vibrate(50)
            val previous = tvFortune.text.toString()

            rewardedAd?.let { ad ->
                ad.show(this) {
                    Log.d(TAG, "User earned the reward.")
                    showRandomFortune()
                    EventTracker.logCookieReload(
                        phraseId = tvFortune.text.toString().take(20),
                        previousPhraseId = previous.take(20),
                        userId = firebaseAnalytics.appInstanceId.toString()
                    )
                }
            } ?: run {
                Log.d(TAG, "The ad wasn't ready yet.")
                Toast.makeText(this, "Carregant anunci, intenta-ho de nou...", Toast.LENGTH_SHORT).show()
                showRandomFortune()
                EventTracker.logCookieReload(
                    phraseId = tvFortune.text.toString().take(20),
                    previousPhraseId = previous.take(20),
                    userId = firebaseAnalytics.appInstanceId.toString()
                )
                loadRewardedAd()
            }
        }

        btnCopy.setOnClickListener {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Fortune", tvFortune.text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Frase copiada!", Toast.LENGTH_SHORT).show()
            vibrate(80)
            EventTracker.logCopyPhrase(
                phrase = tvFortune.text.toString(),
                phraseId = tvFortune.text.toString().take(20),
                userId = firebaseAnalytics.appInstanceId.toString()
            )
        }

        btnShare.setOnClickListener {
            vibrate(100)
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, tvFortune.text.toString())
            startActivity(Intent.createChooser(intent, "Comparteix la teva frase"))
            EventTracker.logSharePhrase(
                phrase = tvFortune.text.toString(),
                phraseId = tvFortune.text.toString().take(20),
                shareMethod = "intent",
                userId = firebaseAnalytics.appInstanceId.toString()
            )
        }
    }

    private fun showRandomFortune() {
        val fortune = fortunes.random()
        tvFortune.text = fortune
    }

    // ADMOB: CARGAR ANUNCIO
    private fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(this, "ca-app-pub-3940256099942544/5224354917", adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, adError.toString())
                    Toast.makeText(this@MainActivity, "Error carregant anunci: ${adError.message}", Toast.LENGTH_SHORT).show()
                    rewardedAd = null
                }
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Ad was loaded.")
                    Toast.makeText(this@MainActivity, "Anunci llest!", Toast.LENGTH_SHORT).show()
                    rewardedAd = ad
                    rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Log.d(TAG, "Ad dismissed fullscreen content.")
                            rewardedAd = null
                            loadRewardedAd()
                        }
                    }
                }
            }
        )
    }

    private fun playCookieSound() {
        try {
            if (mediaPlayer.isPlaying) mediaPlayer.seekTo(0)
            mediaPlayer.start()
            EventTracker.logSoundPlayed("cookie_break", true)
        } catch (e: Exception) {
            e.printStackTrace()
            EventTracker.logSoundPlayed("cookie_break", false)
        }
    }

    private fun vibrate(milliseconds: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(milliseconds)
            }
            EventTracker.logVibration(milliseconds, true)
        } catch (e: Exception) {
            e.printStackTrace()
            EventTracker.logVibration(milliseconds, false)
        }
    }

    override fun onResume() {
        super.onResume()
        EventTracker.logScreenView(
            screenName = "MainActivity",
            screenClass = "MainActivity",
            userId = firebaseAnalytics.appInstanceId.toString()
        )
    }

    private fun applyTheme(isDark: Boolean) {
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        updateThemeIcon(isDark)
    }

    private fun updateThemeIcon(isDark: Boolean) {
        if (isDark) {
            btnThemeToggle.setImageResource(R.drawable.ic_light_mode)
        } else {
            btnThemeToggle.setImageResource(R.drawable.ic_dark_mode)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
}