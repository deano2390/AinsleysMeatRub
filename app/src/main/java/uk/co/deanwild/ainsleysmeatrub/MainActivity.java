package uk.co.deanwild.ainsleysmeatrub;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.games.Games;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import uk.co.deanwild.ainsleysmeatrub.leaderboard.BaseGameActivity;

public class MainActivity extends BaseGameActivity implements GameStateListener {

    private static final int GAME_DURATION = 30;
    private static final long FACE_CHANGE_INTERVAL = 250;
    private static final long SOUND_INTERVAL = 5000;
    private static final long ONE_SECOND = 1000;
    int secondsLeft = GAME_DURATION;

    private SoundManager soundManager;
    Handler handler = new Handler();

    private TextView tvScore;
    private TextView tvTimer;
    private ImageView imgHead;
    private SalamiView salamiView;
    private View startOverlay;
    private TextView tvOverlay;
    private TextView overlayButton;

    private GameState gameState;
    private Animation throbAnim;
    private Animation throbInfinateAnim;
    private boolean allowPause = false;
    private int score = 0;
    public static boolean useAlternateResources;
    private View shareButton;

    public static void start(Context context, boolean useAlternateResources) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("useAlternateResources", useAlternateResources);
        context.startActivity(intent);
    }

    enum GameState {
        IDLE,
        STARTING,
        PLAYING,
        END
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getGameHelper().setMaxAutoSignInAttempts(0);
        useAlternateResources = getIntent().getBooleanExtra("useAlternateResources", true);
        doCreate();
    }

    void doCreate() {
        gameState = GameState.IDLE;

        // stop game service form auto login

        setContentView(R.layout.activity_main);


        soundManager = new SoundManager(this);

        salamiView = (SalamiView) findViewById(R.id.game_view);
        salamiView.setGameStateListener(this);

        imgHead = (ImageView) findViewById(R.id.head);
        tvScore = (TextView) findViewById(R.id.tv_score);
        tvTimer = (TextView) findViewById(R.id.tv_timer);
        startOverlay = findViewById(R.id.start_overlay);
        tvOverlay = (TextView) findViewById(R.id.tv_overlay);
        overlayButton = (TextView) findViewById(R.id.overlay_button);
        overlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRoutine();
            }
        });
        shareButton = findViewById(R.id.share_button);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                share();
            }
        });
        Typeface tf = Typeface.createFromAsset(getAssets(),
                "SpicyRice-Regular.ttf");

        tvScore.setTypeface(tf);
        tvTimer.setTypeface(tf);
        tvOverlay.setTypeface(tf);
        overlayButton.setTypeface(tf);

        score = 0;
        secondsLeft = GAME_DURATION;
        tvScore.setText("Score: 0");
        tvTimer.setText("Time: " + secondsLeft);

        throbAnim = AnimationUtils.loadAnimation(this, R.anim.throb);
        throbInfinateAnim = AnimationUtils.loadAnimation(this, R.anim.throb_infinate);
        overlayButton.startAnimation(throbInfinateAnim);

        handler.postDelayed(faceRunnable, 0);
        handler.postDelayed(soundRunnable, SOUND_INTERVAL);

        if (!useAlternateResources) {
            findViewById(R.id.root).setBackgroundResource(R.drawable.ainsleybackground);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (!allowPause) {
            // don't allow pausing if we're mid game
            if (!isFinishing()) {
                finish();
            }
        }
    }

    @Override
    public void finish() {
        if (soundManager != null)
            soundManager.close();
        soundManager = null;
        super.finish();
    }

    @Override
    public void onMeatRubbed(int points) {
        score += points;
        tvScore.setText("Score: " + score);
    }

    private void win() {
        gameState = GameState.END;

        if (soundManager != null)
            soundManager.playSound(soundManager.endBell);

        salamiView.stop();

        tvOverlay.setText("YEEAHHEAA BWOI! \r\n" + score + " points!");
        tvOverlay.startAnimation(throbInfinateAnim);
        startOverlay.setVisibility(View.VISIBLE);

        overlayButton.setText("Try again");
        overlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRoutine();
            }
        });

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                submitScore();
            }
        }, 3000);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                overlayButton.setVisibility(View.VISIBLE);
            }
        }, 8000);
    }

    private void submitScore() {

        allowPause = true;

        if (isSignedIn() && getApiClient().isConnected()) {

            Games.Leaderboards.submitScore(getApiClient(),
                    getString(R.string.leaderboard_id),
                    score);


            startActivityForResult(Games.Leaderboards.getLeaderboardIntent(
                            getApiClient(), getString(R.string.leaderboard_id)),
                    2);

        } else {
            beginUserInitiatedSignIn();
        }
    }

    @Override
    protected void onActivityResult(int request, int response, Intent data) {
        super.onActivityResult(request, response, data);
        allowPause = false;
    }

    Runnable countDownRunnable = new Runnable() {
        @Override
        public void run() {
            secondsLeft--;

            tvTimer.setText("Time: " + secondsLeft);
            tvTimer.startAnimation(throbAnim);

            if (soundManager != null)
                soundManager.playSound(soundManager.beep);

            if (secondsLeft <= 0) {
                win();
            } else {
                handler.postDelayed(countDownRunnable, ONE_SECOND);
            }
        }
    };

    Runnable soundRunnable = new Runnable() {
        @Override
        public void run() {
            if (soundManager != null)
                soundManager.playRandomSound();

            handler.postDelayed(soundRunnable, 5000);
        }
    };

    Runnable faceRunnable = new Runnable() {
        @Override
        public void run() {
            incrementFaceImage();
            handler.postDelayed(faceRunnable, FACE_CHANGE_INTERVAL);
        }

        int imageId = 0;

        private void incrementFaceImage() {
            imageId++;
            if (imageId > 1) imageId = 0;

            if (imageId == 0) {
                if (useAlternateResources) {
                    imgHead.setImageResource(R.drawable.cartoonface1);
                } else {
                    imgHead.setImageResource(R.drawable.ainsley_head);
                }

            } else if (imageId == 1) {
                if (useAlternateResources) {
                    imgHead.setImageResource(R.drawable.cartoonface2);
                } else {
                    imgHead.setImageResource(R.drawable.ainsley_head_2);
                }

            }
        }
    };


    @Override
    public void onAinsleyGettingExcited(boolean excited) {
        if (soundManager != null)
            soundManager.playWinSound(excited);
    }


    void startRoutine() {

        gameState = GameState.STARTING;
        score = 0;
        secondsLeft = GAME_DURATION;
        tvScore.setText("Score: 0");
        tvTimer.setText("Time: " + secondsLeft);


        tvOverlay.clearAnimation();
        overlayButton.setVisibility(View.GONE);
        tvOverlay.setText("READY!");
        tvOverlay.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);

        tvOverlay.startAnimation(throbAnim);

        if (soundManager != null)
            soundManager.playSound(soundManager.beep);

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                tvOverlay.post(new Runnable() {
                    @Override
                    public void run() {
                        tvOverlay.setText("STEADY!");
                        tvOverlay.startAnimation(throbAnim);

                        if (soundManager != null)
                            soundManager.playSound(soundManager.beep);
                    }
                });

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                tvOverlay.post(new Runnable() {
                    @Override
                    public void run() {
                        tvOverlay.setText("GO!");
                        tvOverlay.startAnimation(throbAnim);

                        if (soundManager != null)
                            soundManager.playSound(soundManager.beep);
                    }
                });

                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                startOverlay.post(new Runnable() {
                    @Override
                    public void run() {
                        startOverlay.setVisibility(View.GONE);

                        if (soundManager != null)
                            soundManager.playSound(soundManager.buzzer);

                        start();
                    }
                });
            }
        }).start();
    }


    void start() {
        gameState = GameState.PLAYING;
        salamiView.start();

        handler.postDelayed(countDownRunnable, ONE_SECOND);
    }

    @Override
    public void onSignInFailed() {
        allowPause = false;
    }

    @Override
    public void onSignInSucceeded() {
        if (gameState == GameState.END)
            submitScore();
    }

    private void share() {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

// Add data to the intent, the receiving app will decide what to do with it.
        intent.putExtra(Intent.EXTRA_SUBJECT, "Ainsley's Meat rub");

        if (score > 0) {
            intent.putExtra(Intent.EXTRA_TEXT, "I scored " + score + " points on Ainsley's Meat Rub! Download it from Google play and give your meat a good ol' rub! https://play.google.com/store/apps/details?id=uk.co.deanwild.ainsleysmeatrub\");");
        } else {
            intent.putExtra(Intent.EXTRA_TEXT, "Download Ainsley's Meat Rub from Google play and give your meat a good ol' rub! https://play.google.com/store/apps/details?id=uk.co.deanwild.ainsleysmeatrub");
        }


        startActivity(intent);
    }
}
