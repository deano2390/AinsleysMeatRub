package uk.co.deanwild.ainsleysmeatrub;

import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by deanwild on 25/09/15.
 */
public class SoundManager implements SoundPool.OnLoadCompleteListener {


    private SoundPool soundPool;
    private boolean winSoundPlaying = false;
    Handler handler = new Handler();
    ArrayList<Integer> soundIDs = new ArrayList<>();

    int oldRub;
    int heheBwoi;
    int bitSpecial;
    int bounce;
    int delightful;
    int finger;
    int ohyes;
    int spicyMeat;
    int grind;
    int laugh;
    int beep;
    int buzzer;
    int readysteady;
    int endBell;

    public SoundManager(final MainActivity context) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
                soundPool.setOnLoadCompleteListener(SoundManager.this);

                readysteady = soundPool.load(context, R.raw.readysteady, 1);
                beep = soundPool.load(context, R.raw.beep, 1);
                buzzer = soundPool.load(context, R.raw.buzzer, 1);
                endBell = soundPool.load(context, R.raw.endbell, 1);

                oldRub = soundPool.load(context, R.raw.goodoldrubloud, 1);
                heheBwoi = soundPool.load(context, R.raw.hehebwoiloud, 1);

                bitSpecial = soundPool.load(context, R.raw.bitspecialloud, 1);
                bounce = soundPool.load(context, R.raw.bounceupanddownloud, 1);
                delightful = soundPool.load(context, R.raw.delightfulloud, 1);
                finger = soundPool.load(context, R.raw.fingerrubadubloud, 1);

                grind = soundPool.load(context, R.raw.goodoldgrindloud, 1);
                laugh = soundPool.load(context, R.raw.laughingloud, 1);
                ohyes = soundPool.load(context, R.raw.ohyesloud, 1);
                spicyMeat = soundPool.load(context, R.raw.spicymeatloud, 1);

                soundIDs.add(heheBwoi);
                soundIDs.add(bitSpecial);
                soundIDs.add(bounce);
                soundIDs.add(delightful);
                soundIDs.add(finger);
                soundIDs.add(grind);
                soundIDs.add(laugh);
                soundIDs.add(ohyes);
                soundIDs.add(spicyMeat);

            }
        }).start();


    }

    public void playSound(int soundID){
        soundPool.play(soundID, 1.5f, 1.5f, 1, 0, 1);
    }

    public void playWinSound(boolean excited) {

        if(MainActivity.useAlternateResources) return;

        if (!winSoundPlaying) {
            winSoundPlaying = true;

            if(excited){
                soundPool.play(heheBwoi, 1.5f, 1.5f, 1, 0, 1);
                soundPool.play(laugh, 1.5f, 1.5f, 1, 0, 1);
            }else{
                soundPool.play(laugh, 1.5f, 1.5f, 1, 0, 1);
            }

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    winSoundPlaying = false;
                }
            }, 1500);
        }
    }

    public void close() {
        soundPool.release();
    }

    @Override
    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {

        if(MainActivity.useAlternateResources) return;

        if (status == 0) {
            if (sampleId == readysteady) {
                soundPool.play(readysteady, 1.5f, 1.5f, 1, -1, 1);
            }
        }
    }


    public void playRandomSound() {
        if(MainActivity.useAlternateResources) return;
        if (winSoundPlaying) return;
        int soundID = randInt(0, soundIDs.size() - 1);
        soundID = soundIDs.get(soundID);
        soundPool.play(soundID, 1.5f, 1.5f, 1, 0, 1);
    }

    static Random rand = new Random();

    public static int randInt(int min, int max) {

        // NOTE: This will (intentionally) not run as written so that folks
        // copy-pasting have to think about how to initialize their
        // Random instance.  Initialization of the Random instance is outside
        // the main scope of the question, but some decent options are to have
        // a field that is initialized once and then re-used as needed or to
        // use ThreadLocalRandom (if using at least Java 1.7).


        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }
}
