package uk.co.deanwild.ainsleysmeatrub;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * Created by deanwild on 25/09/15.
 */
public class SalamiView extends FrameLayout {

    GameStateListener gameStateListener;
    private static final int ORGASM_THREASHOD = 700;
    int currentExcitmentLevel = 1;
    private Bitmap salamiBitmap;
    private float lastMoveX;
    private boolean stopped = true;
    private float angle = 30;
    private int direction;
    private float lastTouchX;
    private float lastTouchY;

    public SalamiView(Context context) {
        super(context);
        init();
    }

    public SalamiView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SalamiView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SalamiView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {

        setWillNotDraw(false);
        salamiBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.salami);
        updateFrame(); // fire off the game loop
    }

    private void updateFrame() {

        this.postDelayed(new Runnable() {
            @Override
            public void run() {
                gameLoop();
            }
        }, 33);
    }

    private synchronized void gameLoop() {

        invalidate();


        currentExcitmentLevel -= currentExcitmentLevel / 20;

        if (currentExcitmentLevel < 1)
            currentExcitmentLevel = 1;

        if (currentExcitmentLevel > ORGASM_THREASHOD) {
            if (gameStateListener != null)
                gameStateListener.onAinsleyGettingExcited(true);
        } else if (currentExcitmentLevel > 200) {
            if (gameStateListener != null)
                gameStateListener.onAinsleyGettingExcited(false);
        }

        updateFrame();
    }

    private void win() {


    }


    boolean idleFlag = false;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int bmheight = salamiBitmap.getHeight();
        float yOffset = getMeasuredHeight() - (bmheight * 3);

        Matrix matrix = new Matrix();
        matrix.preTranslate(-80f, yOffset);

        float scale = 1 + ((float) currentExcitmentLevel / (float) 1000);


        if (currentExcitmentLevel > 50) {
            angle = (30 - (float) currentExcitmentLevel / (float) 17);
            idleFlag = false;
        } else {

            if (!idleFlag) {
                idleFlag = true;
                direction = -1;
            }

            float change = 1.5f * direction;

            angle += change;

            if (angle >= 30) {
                direction = -1;
            } else if (angle <= 25) {
                direction = 1;
            }

        }


       /* float realHeight = salamiBitmap.getHeight() * scale;
        float realWidth = salamiBitmap.getWidth() * scale;
        float left = -80f;
        float top = yOffset;
        float right = left + realWidth;
        float bottom = top + realHeight;


        float absAngleInDegress = Math.abs(angle);
        float angleOfTri1 = degToRad(absAngleInDegress / 2);

        // trig 1
        float hyp = realWidth;
        float opp = (float) (hyp * Math.sin(angleOfTri1));
        float angle2 = (180 - absAngleInDegress) / 2;


        // trig 2
        float theta = degToRad(angle2);
        hyp = opp;
        opp = (float) (Math.sin(theta) * hyp);     // y offset
        float adj = (float) (Math.cos(theta) * hyp); //x offset


        if (angle > 0) {
            float realRight = right - adj;
            float realBottom = bottom + opp;
            canvas.drawLine(left, top, realRight, realBottom, new Paint());
        } else {
            float realRight = right - adj;
            float realBottom = bottom - opp;
            canvas.drawLine(left, top, realRight, realBottom, new Paint());
        }*/

        matrix.preScale(scale, scale);
        matrix.preRotate(angle, 0, salamiBitmap.getHeight() / 2);

        canvas.drawBitmap(salamiBitmap, matrix, new Paint());

        //canvas.drawRect(left, top, right, bottom, new Paint());
        //canvas.drawCircle(lastTouchX, lastTouchY, 5, new Paint());

    }


    static float degToRad(float degrees) {
        return (float) (degrees * (Math.PI / 180));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (stopped) return false;

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                break;

            case MotionEvent.ACTION_MOVE:

                if (lastMoveX > 0) {
                    float thisX = event.getX();
                    float diff = Math.abs(thisX - lastMoveX);
                    float points = diff / 3;
                    currentExcitmentLevel += points;

                    if (gameStateListener != null) {
                        gameStateListener.onMeatRubbed((int) points);
                    }
                }
                lastMoveX = event.getX();

                lastTouchX = event.getX();
                lastTouchY = event.getY();

                break;

            case MotionEvent.ACTION_UP:
                lastMoveX = 0;
                break;
        }

        return true;
    }

    public void setGameStateListener(GameStateListener stateListener) {
        this.gameStateListener = stateListener;
    }

    public void stop() {
        stopped = true;
    }

    public void start() {
        stopped = false;
    }

}
