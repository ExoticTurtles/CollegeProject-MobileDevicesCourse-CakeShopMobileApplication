package com.example.dmpasteleria;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Pe_registro extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    EditText mUsername, mEmail, mPassword, mCPassword;
    Button mRegisterBtn;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;
    CheckBox mIsAdmin, mIsUser, mIsWorker;

    private TextView mDisplayDate;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private static final String TAG = "Register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pe_registro);

        //Instancias de los checkbox y edittexts
        mUsername = findViewById(R.id.user);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.passwords);
        mRegisterBtn = findViewById(R.id.verificar_inicio);
        mCPassword = findViewById(R.id.cpass);
        mIsAdmin = findViewById(R.id.checkboxAdmin);
        mIsUser = findViewById(R.id.checkboxUsuario);
        mIsWorker = findViewById(R.id.checkboxEmpleado);

        //Instancias de la base de datos de Firebase
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        //Revisa si el usuario ya ha iniciado sesión, o si hay un usuario actual
        if(fAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(),Pe_iniciodesesion.class));
            finish();
        }

        //Spinner para ingresar la fecha de nacimiento
        mDisplayDate = findViewById(R.id.cnacimiento);
        mDisplayDate.setOnClickListener(view -> {
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1;
            int day = cal.get(Calendar.DAY_OF_MONTH);

            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, day);

            cal.add(Calendar.YEAR, -18);

            DatePickerDialog dialog = new DatePickerDialog(
                    Pe_registro.this,
                    android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                    mDateSetListener,
                    year,month,day);
            dialog.getDatePicker().setMaxDate(cal.getTimeInMillis());
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();

        });

        mDateSetListener = (datePicker, month, day, year) -> {
            Log.d(TAG,"onDateSet: mm/dd/yyyy : "+ month + "/" + day + "/" + year);

            String date = month + "/" + day + "/" + year;
            mDisplayDate.setText(date);
        };

        Spinner spinners = findViewById(R.id.spinner2);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this, R.array.municipio, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinners.setAdapter(adapter1);
        spinners.setOnItemSelectedListener(this);

        //Set on click listener para el botón de registro de usuario.
        mRegisterBtn.setOnClickListener(v -> {
            String Username = mUsername.getText().toString().trim();
            String Email = mEmail.getText().toString().trim();
            String Password = mPassword.getText().toString().trim();
            String CPassword = mCPassword.getText().toString().trim();
            String Spinner_0 = spinners.getSelectedItem().toString().trim();
            String Nacimiento = mDisplayDate.getText().toString().trim();


            //Revisa si hay campos en blanco
            if(TextUtils.isEmpty(Email)){
                mEmail.setError("Ingresa tu Email.");
                return;
            }
            if(TextUtils.isEmpty(Password)) {
                mPassword.setError("Ingresa tu contraseña, por favor.");
                return;
            }

            if(!Password.equals(CPassword)){
                mCPassword.setError("Las contraseñas no coinciden, intentalo de nuevo");
                return;
            }

            if(!(mIsWorker.isChecked() || mIsUser.isChecked() || mIsAdmin.isChecked())){
                Toast.makeText(Pe_registro.this, "Seleciona un tipo de cuenta", Toast.LENGTH_SHORT).show();
                return;
            }

            //Crea el usuario y lo registra en la base de datos
            fAuth.createUserWithEmailAndPassword(Email, Password).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    FirebaseUser fUser = fAuth.getCurrentUser();
                    userID = fAuth.getCurrentUser().getUid();
                    DocumentReference documentReference = fStore.collection("users").document(userID);
                    Map<String, Object> user = new HashMap<>();
                    user.put("Username", Username);
                    user.put("Email", Email);
                    user.put("Password", Password);
                    user.put("Residence", Spinner_0);
                    user.put("Nacimiento", Nacimiento);
                    if(mIsUser.isChecked() && !(mIsAdmin.isChecked() && !(mIsWorker.isChecked()))){
                        user.put("Role", "1");
                        user.put("RoleAdmin", "0");
                        user.put("RoleWorker", "0");
                    }
                    if(mIsAdmin.isChecked() && !(mIsWorker.isChecked()) && !(mIsUser.isChecked())){
                        user.put("Role", "0");
                        user.put("RoleAdmin", "1");
                        user.put("RoleWorker", "0");
                    }
                    if(mIsWorker.isChecked() && !(mIsUser.isChecked()) && !(mIsAdmin.isChecked())){
                        user.put("Role", "0");
                        user.put("RoleAdmin", "0");
                        user.put("RoleWorker", "1");
                    }
                    documentReference.set(user).addOnSuccessListener(aVoid -> Log.d(TAG, "Usuario creado correctamente con el ID: " + userID));

                    fUser.sendEmailVerification().addOnSuccessListener(aVoid -> Toast.makeText(Pe_registro.this, "Favor de revisar tu correo", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Log.d(TAG, "Error" + e.getMessage()));

                    Toast.makeText(Pe_registro.this, "Usuario creado con exito.",Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(),Pe_iniciodesesion.class));
                }
                else{
                    Toast.makeText(Pe_registro.this, "Error! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        });

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        Toast.makeText(parent.getContext(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }


    //Función para regresar a la pantalla principal
    public void regresar(View view) {
        startActivity(new Intent(getApplicationContext(), Pe_inicio.class));
        finish();
    }
}