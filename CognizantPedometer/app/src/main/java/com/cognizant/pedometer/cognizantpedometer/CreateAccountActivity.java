package com.cognizant.pedometer.cognizantpedometer;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class CreateAccountActivity extends AppCompatActivity {
    private TextInputEditText usernameInput, emailInput;
    private TextInputLayout usernameInputLayout, emailInputLayout;
    private Button signUpButton;
    private Settings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        settings = new Settings(getApplicationContext());

        usernameInput = (TextInputEditText) findViewById(R.id.usernameText);
        emailInput = (TextInputEditText) findViewById(R.id.emailText);
        usernameInputLayout = (TextInputLayout) findViewById(R.id.usernameTextLayout);
        emailInputLayout = (TextInputLayout) findViewById(R.id.emailTextLayout);

        signUpButton = (Button) findViewById(R.id.signUpButton);

        usernameInput.addTextChangedListener(new MyTextWatcher(usernameInput));
        emailInput.addTextChangedListener(new MyTextWatcher(emailInput));

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitForm();
            }
        });
    }

    private void submitForm() {
        if (!validateUsername() || !validateEmail()) {
            return;
        }

        JSONObject encryptedJsonObject = getEncryptedJson();

        final CreateAccountActivity that = this;
        if (Utils.isConnected(getApplicationContext())) {
            HttpService httpService = new HttpService("POST", encryptedJsonObject) {
                @Override
                public void onResponseReceived(Response response) {
                    if (response.getResponseCode() == 200) {
                        settings.setUsername(usernameInput.getText().toString());
                        settings.setEmail(emailInput.getText().toString());
                        settings.setGuid(response.getContent().replace("\"", ""));
                        Intent mainActivity = new Intent(that, MainActivity.class);
                        startActivity(mainActivity);
                        finish();
                    } else {
                        try {
                            JSONObject responseJson = new JSONObject(response.getContent());
                            Toast.makeText(getApplicationContext(), responseJson.getString("message"), Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            httpService.execute();
        } else {
            Toast.makeText(getApplicationContext(), "No network connection available.", Toast.LENGTH_SHORT).show();
        }
    }

    private JSONObject getEncryptedJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", usernameInput.getText().toString());
            jsonObject.put("email", emailInput.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Utils.encryptJson(jsonObject);
    }

    private boolean validateUsername() {
        if (usernameInput.getText().toString().trim().isEmpty()) {
            usernameInputLayout.setError("Enter valid name");
            requestFocus(usernameInput);
            return false;
        } else {
            usernameInputLayout.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validateEmail() {
        String email = emailInput.getText().toString().trim();
        if (email.isEmpty() || !isValidEmail(email)) {
            emailInputLayout.setError("Enter valid email address");
            requestFocus(emailInput);
            return false;
        } else {
            emailInputLayout.setErrorEnabled(false);
        }

        return true;
    }

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.usernameText:
                    validateUsername();
                    break;
                case R.id.emailText:
                    validateEmail();
                    break;
            }
        }
    }

}
