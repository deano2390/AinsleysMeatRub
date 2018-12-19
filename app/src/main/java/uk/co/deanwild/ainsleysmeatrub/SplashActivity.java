package uk.co.deanwild.ainsleysmeatrub;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class SplashActivity extends AppCompatActivity {

    private ImageView image;
    private MediaPlayer mediaPlayer;
    private boolean useAlternateResources = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        image = (ImageView) findViewById(R.id.iv_splash);
        image.setImageResource(R.drawable.splash_image);
        image.setVisibility(View.INVISIBLE);

        Prefs.shouldUseAlternateResources(this, new Prefs.PrefsCallback() {
            @Override
            public void callback(boolean shouldUseAlternateResources) {
                useAlternateResources = shouldUseAlternateResources;

                if(useAlternateResources){
                    MainActivity.start(SplashActivity.this, useAlternateResources);
                    finish();
                }else{
                    doIntro();
                }
            }
        });
    }

    void doIntro(){
        final Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein);
        fadeIn.setFillAfter(true);
        image.postDelayed(new Runnable() {
            @Override
            public void run() {
                image.startAnimation(fadeIn);
            }
        }, 100);

        mediaPlayer = MediaPlayer.create(this, R.raw.splash);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {

                if (!isFinishing()) {
                    MainActivity.start(SplashActivity.this, useAlternateResources);
                    finish();
                }
            }
        });

        mediaPlayer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mediaPlayer != null)
            mediaPlayer.stop();

        if (!isFinishing())
            finish();
    }
}
