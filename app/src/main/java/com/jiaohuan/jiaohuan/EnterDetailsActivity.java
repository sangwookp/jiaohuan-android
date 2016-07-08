package com.jiaohuan.jiaohuan;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jiaohuan.jiaohuan.jsonData.User;
import com.jiaohuan.jiaohuan.jsonData.UserAPI;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EnterDetailsActivity extends Activity {

    private TextView mNext;

    private EditText mName;
    private EditText mPassword;
    private EditText mPhone;
    private TextView mBack;
    private Spinner mSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_details);

        mNext = (TextView) findViewById(R.id.next);

        mName = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);
        mPhone = (EditText) findViewById(R.id.phone);
        mBack = (TextView) findViewById(R.id.back);
        mSpinner = (Spinner) findViewById(R.id.spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.nums, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        mSpinner.setAdapter(adapter);

        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mName.getText().toString();
                String password = mPassword.getText().toString();
                String phone = mSpinner.getSelectedItem().toString() + mPhone.getText().toString();

                if (email.matches("") || password.matches("") || phone.matches("")){
                    Toast.makeText(getApplicationContext(), "You did not enter a username", Toast.LENGTH_SHORT).show();
                    mName.setHintTextColor(Color.RED);
                    mPassword.setHintTextColor(Color.RED);
                    mPhone.setHintTextColor(Color.RED);
                    return;}
//                else if(phone.length() != 12){
//                    Toast.makeText(getApplicationContext(), "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
//                    mPhone.setTextColor(Color.RED);
//                }


                else{
                    /*

                    Send email, password, and phone to server*/

                    UserAPI.Factory.getInstance().createUser(email, password, "Donald").enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {

                        try{
                            Log.wtf("WORKS",""+ response.body().getUsername());
                        }catch (NullPointerException t){
                            Log.wtf("NO", "Didn't work, most likely incorrect username+password");
                            t.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Log.wtf("FAIL",""+t.getMessage());
                    }
                });

                    /* */
                }
            }
        });
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }



}
