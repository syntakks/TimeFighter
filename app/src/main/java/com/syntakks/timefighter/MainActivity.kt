package com.syntakks.timefighter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    // Data
    private var score = 0
    private var timeRemaining : Long = 0
    // Timer
    private var gameStarted = false
    private lateinit var countDownTimer: CountDownTimer
    internal val defaultCountdown: Long = 5000
    internal val countDownInterval: Long = 1000
    // UI
    private lateinit var rootView: ConstraintLayout
    private lateinit var scoreLabel: TextView
    private lateinit var timeLabel: TextView
    private lateinit var tapButton: Button
    // Animation
    private lateinit var bounceAnimation: Animation
    private lateinit var blinkAnimation: Animation

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val GAME_STARTED = "GAME_STARTED"
        private const val CURRENT_SCORE = "CURRENT_SCORE"
        private const val TIME_REMAINING = "TIME_REMAINING"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bounceAnimation = AnimationUtils.loadAnimation(this, R.anim.bounce)
        blinkAnimation = AnimationUtils.loadAnimation(this, R.anim.blink)
        Log.d(TAG, "onCreate called. Score is: $score")
        rootView = findViewById(R.id.root)
        scoreLabel = findViewById(R.id.scoreLabel)
        timeLabel = findViewById(R.id.timeLabel)
        tapButton = findViewById(R.id.tapButton)

        tapButton.setOnClickListener { view ->
            startGame()
            if (incrementScore()) {
                playScoreAnimations(view)
            }
        }

        if (savedInstanceState != null && savedInstanceState.getBoolean(GAME_STARTED, false)) {
            score = savedInstanceState.getInt(CURRENT_SCORE, 0)
            timeRemaining = savedInstanceState.getLong(TIME_REMAINING, defaultCountdown)
            restoreGame()
        } else {
            resetGame()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        if (item.itemId == R.id.actionAbout) {
            showInfo()
        }
        return true
    }

    private fun showInfo() {
        val dialogTitle = getString(R.string.about_title)
        val dialogMessage = getString(R.string.about_message)
        val builder = AlertDialog.Builder(this)
        builder
            .setTitle(dialogTitle)
            .setMessage(dialogMessage)
            .create().show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(GAME_STARTED, gameStarted)
        outState.putInt(CURRENT_SCORE, score)
        outState.putLong(TIME_REMAINING, timeRemaining)
        countDownTimer.cancel()

        Log.d(TAG, "onSaveInstanceState: Saving Current Score: $score & Time Remaining $timeRemaining")
    }

    private fun startGame() {
        if (!gameStarted) {
            countDownTimer.start()
            gameStarted = true
        }
    }

    private fun gameOver() {
        tapButton.isEnabled = false
        showSnackbar()
    }

    private fun restoreGame() {
        // Score
        scoreLabel.text = getString(R.string.current_score, score)
        // Time
        val restoredTime = timeRemaining / 1000
        timeLabel.text = getString(R.string.time_remaining, restoredTime)
        if (timeRemaining > 0) {
            countDownTimer = getCountdownTimer(timeRemaining).start()
        }
    }

    private fun resetGame() {
        // Restore Button
        tapButton.isEnabled = true
        // Reset Score / Label
        score = 0
        scoreLabel.text = getString(R.string.current_score, score)
        // Reset Time / Label
        var initialTimeLeft = defaultCountdown / 1000
        timeLabel.text = getString(R.string.time_remaining, initialTimeLeft)
        // Rest Timer
        countDownTimer = getCountdownTimer(defaultCountdown)

        gameStarted = false
    }

    private fun getCountdownTimer(totalTime: Long) : CountDownTimer {
        return object : CountDownTimer(totalTime, countDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemaining = millisUntilFinished
                timeLabel.text = getString(R.string.time_remaining, timeRemaining / 1000)
            }

            override fun onFinish() {
                gameOver()
            }
        }
    }

    private fun incrementScore() : Boolean {
        if (gameStarted) {
            score += 1
            val newScore = getString(R.string.current_score, score) // %1$d in the placeholder text is the score, neat.
            scoreLabel.text = newScore
            return true
        }
        return false
    }

    private fun showSnackbar() {
        Snackbar.make(findViewById(R.id.root), getString(R.string.game_over_text, score), Snackbar.LENGTH_INDEFINITE)
            .setAction("Dismiss") {
                resetGame()
            }.show()
    }

    private fun playScoreAnimations(view: View) {
        view.startAnimation(bounceAnimation)
        scoreLabel.startAnimation(blinkAnimation)
    }

}
