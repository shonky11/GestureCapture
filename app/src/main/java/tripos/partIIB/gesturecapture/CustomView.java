package tripos.partIIB.gesturecapture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomView extends View {
    //drawing path
    private Path drawPath;
    //defines what to draw
    private Paint canvasPaint;
    //defines how to draw
    private Paint drawPaint;
    //initial color
    private int paintColor = 0xFF85B09A;
    //canvas - holding pen, holds your drawings
    //and transfers them to the view
    private Canvas drawCanvas;
    //canvas bitmap
    private Bitmap canvasBitmap;
    //brush size
    private float currentBrushSize, lastBrushSize;

    private Boolean training = true;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    float mX, mY, TOUCH_TOLERANCE;

    private ArrayList<Path> paths = new ArrayList<Path>();

    public CustomView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        //create canvas of certain device size.
        super.onSizeChanged(w, h, oldw, oldh);

        //create Bitmap of certain w,h
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        //apply bitmap to graphic to start drawing.
        drawCanvas = new Canvas(canvasBitmap);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                touch_start(touchX, touchY);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(touchX, touchY);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
            default:
                return false;
        }
        return true;
    }

    private void touch_start(float x, float y) {
        drawPath.reset();
        drawPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            drawPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;
        }
    }

    private void touch_up() {
        drawPath.lineTo(mX, mY);
        drawCanvas.drawPath(drawPath, drawPaint);
        paths.add(drawPath);
        drawPath = new Path();

    }

    protected void onDraw(Canvas canvas) {
        for (Path p : paths) {
            canvas.drawPath(p, drawPaint);
        }
        canvas.drawPath(drawPath, drawPaint);
    }

    private void init(){
        currentBrushSize = getResources().getInteger(R.integer.medium_size);
        lastBrushSize = currentBrushSize;

        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(currentBrushSize);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    public void eraseAll() {
        paths.clear();
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        invalidate();
    }

    public void onClickUndo () {
        if (paths.size()>0)
        {
            paths.remove(paths.size()-1);
            invalidate();
        }

    }

    public void onClickFlip (){
        if (training)
        {
            training = false;
            this.setBackgroundColor(Color.parseColor("#1f1f2b"));
        }
        else if (!training)
        {
            training = true;
            this.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void writeData(String label)
    {
        Map<String, Object> path = new HashMap<>();
        float error = (float) 0.5;

        if (!paths.isEmpty())
        {
            Path firstPath = paths.get(0);
            float[] first = firstPath.approximate(error);
            List<Number> firebaseFirst = new ArrayList<Number>();
            for (float point : first)
            {
                firebaseFirst.add((Number) point);
            }
            path.put("path", firebaseFirst);

            if (training) {
                path.put("label", label);

                String newDoc = db.collection("training").document().getId();
                db.collection("training").document(newDoc)
                        .set(path)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                            }
                        });
            }
            else
            {
                String newDoc = db.collection("testing").document().getId();
                db.collection("testing").document(newDoc)
                        .set(path)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                            }
                        });
            }

            paths.clear();
            drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
            invalidate();
        }
    }
}
