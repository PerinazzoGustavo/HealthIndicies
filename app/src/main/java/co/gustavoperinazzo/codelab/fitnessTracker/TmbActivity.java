package co.gustavoperinazzo.codelab.fitnessTracker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class TmbActivity extends AppCompatActivity {
    private EditText tmb_weight, tmb_height, tmb_years;
    private Spinner spinner;


    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tmb);


        Button btnTmb = findViewById(R.id.tmb_button);
        tmb_weight = findViewById(R.id.weight_tmb);
        tmb_height = findViewById(R.id.height_tmb);
        tmb_years = findViewById(R.id.years_old);
        spinner = findViewById(R.id.spinner_lifestyle);


        btnTmb.setOnClickListener(view -> {

            if (!validate()) {
                Toast.makeText(TmbActivity.this, R.string.invalide_fields, Toast.LENGTH_LONG).show();
                return;
            }


            String stringWeight = tmb_weight.getText().toString();
            String stringHeight = tmb_height.getText().toString();
            String stringYears = tmb_years.getText().toString();


            int Weight = Integer.parseInt(stringWeight);
            int Height = Integer.parseInt(stringHeight);
            int Years = Integer.parseInt(stringYears);

            double resultTmbMale = calculateTmbMale(Weight, Height, Years);
            double resultTmbFemale = calculateTmbFemale(Weight, Height, Years);

            double tmbResponseMaleId = tmbResponse(resultTmbMale);
            double tmbResponseFemaleId = tmbResponse(resultTmbFemale);

            RadioGroup genderRg = (RadioGroup) findViewById(R.id.tmb_rg);
            int selectId = genderRg.getCheckedRadioButtonId();
            Log.d("TesteTmb", "" + tmbResponseMaleId);
            switch (selectId) {
                case R.id.man_button:
                    AlertDialog.Builder builder = new AlertDialog.Builder(TmbActivity.this);
                    builder.setMessage(getString(R.string.tmb_response, tmbResponseMaleId));
                    builder.setNegativeButton(android.R.string.ok, (dialog, which) -> {
                        dialog.dismiss();
                    });
                    builder.setPositiveButton(R.string.save, ((dialog1, which) -> new Thread(() -> {
                        int updateId = 0;

                        /* Verifica se*/
                        if (getIntent().getExtras() != null)
                            updateId = getIntent().getExtras().getInt("tmb", 0);
                        long calcID;
                        if (updateId > 0) {
                            calcID = SqlHelper.getInstance(TmbActivity.this).updateItem("tmb", tmbResponseMaleId, updateId);
                        } else {
                            calcID = SqlHelper.getInstance(TmbActivity.this).addItem("tmb", tmbResponseMaleId);
                        }
                        runOnUiThread(() -> {
                            if (calcID > 0) {
                                Toast.makeText(TmbActivity.this, R.string.saved, Toast.LENGTH_SHORT).show();
                                openSaveListCalcActivity();
                            }
                        });
                    }).start()));
                    builder.create();
                    builder.show();
                case R.id.woman_button:
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(TmbActivity.this);
                    builder1.setMessage(getString(R.string.tmb_response, tmbResponseFemaleId));
                    builder1.setNegativeButton(android.R.string.ok, (dialog, which) -> {
                        dialog.dismiss();
                    });
                    builder1.setPositiveButton(R.string.save, ((dialog1, which) -> new Thread(() -> {
                        int updateId = 0;

                        if (getIntent().getExtras() != null)
                            updateId = getIntent().getExtras().getInt("tmb", 0);
                        long calcID;
                        if (updateId > 0) {
                            calcID = SqlHelper.getInstance(TmbActivity.this).updateItem("tmb", tmbResponseFemaleId, updateId);
                        } else {
                            calcID = SqlHelper.getInstance(TmbActivity.this).addItem("tmb", tmbResponseFemaleId);
                        }

                        runOnUiThread(() -> {
                            if (calcID > 0) {
                                Toast.makeText(TmbActivity.this, R.string.saved, Toast.LENGTH_SHORT).show();
                                openSaveListCalcActivity();
                            }
                        });
                    }).start()));
                    builder1.create();
                    builder1.show();
            }











            /* Ajustando a tela no Input de dados */
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(tmb_weight.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(tmb_height.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(tmb_years.getWindowToken(), 0);
        });


    }


    private double tmbResponse(double tmb) {
        int spinnerID = spinner.getSelectedItemPosition();
        switch (spinnerID) {
            case 0:
                return tmb * 1.2;
            case 1:
                return tmb * 1.375;
            case 2:
                return tmb * 1.55;
            case 3:
                return tmb * 1.725;
            case 4:
                return tmb * 1.9;
            default:
                return 0;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    // Evento de click de menus no android


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_save_list) {
            openSaveListCalcActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openSaveListCalcActivity() {
        Intent intent = new Intent(TmbActivity.this, SaveActivity.class);
        // -> passando dados para a nova tela passando um Bundle
        intent.putExtra("type", "tmb");
        startActivity(intent);

    }


    private double calculateTmbMale(int weight, int height, int years) {
        return 66.47 + (13.75 * weight) + (5 * height) - (6.8 * years);
    }

    private double calculateTmbFemale(int weight, int height, int years) {
        return 655.1 + (9.56 * weight) + (1.8 * height) - (4.7 * years);

    }

    public boolean validate() {
        return !tmb_weight.getText().toString().startsWith("0")
                && (!tmb_height.getText().toString().startsWith("0"))
                && (!tmb_years.getText().toString().startsWith("0"))
                && !tmb_weight.getText().toString().isEmpty()
                && !tmb_height.getText().toString().isEmpty()
                && !tmb_years.getText().toString().isEmpty();
    }
}