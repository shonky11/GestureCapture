package tripos.partIIB.gesturecapture;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ContentMain extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private Toolbar mToolbar_bottom;
    private CustomView mCustomView;
    public String label;
    public Spinner spinner;
    public ArrayList<String> labelsList = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        mCustomView = (CustomView)findViewById(R.id.custom_view);
        mToolbar_bottom = (Toolbar)findViewById(R.id.toolbar_bottom);
        spinner = findViewById(R.id.labelText);

        DocumentReference labRef = db.collection("labels").document("labels");
        ListenerRegistration locReg;
        locReg = labRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    return;
                }
                if (value != null && value.exists()) {
                    labelsList = (ArrayList<String>) value.getData().get("labels");
                    populateSpinner(labelsList);
                } else {
                }
            }
        });
    }

//    db.collection("cities").document("DC")
//        .delete()
//        .addOnSuccessListener(new OnSuccessListener<Void>() {
//        @Override
//        public void onSuccess(Void aVoid) {
//            Log.d(TAG, "DocumentSnapshot successfully deleted!");
//        }
//    })
//            .addOnFailureListener(new OnFailureListener() {
//        @Override
//        public void onFailure(@NonNull Exception e) {
//            Log.w(TAG, "Error deleting document", e);
//        }
//    });

    public void populateSpinner(ArrayList<String> labelsList){
        spinner.setOnItemSelectedListener(this);
        String[] labList = labelsList.toArray(new String[labelsList.size()]);
        //Creating the ArrayAdapter instance having the country list
        ArrayAdapter aa = new ArrayAdapter(this,R.layout.spinner_list,labList);
        aa.setDropDownViewResource(R.layout.spinner_list);
        //Setting the ArrayAdapter data on the Spinner
        spinner.setAdapter(aa);
    }

    public void delete(View view){
        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(this);
        deleteDialog.setTitle("Clear Gesture");
        deleteDialog.setMessage("Are you sure you want to clear this gesture?");
        deleteDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                mCustomView.eraseAll();
                dialog.dismiss();
            }
        });
        deleteDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        deleteDialog.show();
    }


    public void undo(View view) {
        mCustomView.onClickUndo();
    }

    public void flip(View view) {
        mCustomView.onClickFlip();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void save(View view) {
        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(this);
        deleteDialog.setTitle("Save Gesture");
        deleteDialog.setMessage("Are you sure you want to save this gesture with the label: " + label + "?");
        deleteDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                mCustomView.writeData(label);
                dialog.dismiss();
            }
        });
        deleteDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        deleteDialog.show();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        label = labelsList.get(i);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        label = "Select Label";
    }
}