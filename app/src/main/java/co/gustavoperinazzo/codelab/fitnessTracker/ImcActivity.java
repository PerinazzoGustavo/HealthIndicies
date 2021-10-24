package co.gustavoperinazzo.codelab.fitnessTracker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ImcActivity extends AppCompatActivity {

    private EditText editWeight;
    private EditText editHeight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imc);


        editWeight = findViewById(R.id.edit_imc_weight);
        editHeight = findViewById(R.id.edit_imc_height);

        Button btnSend = findViewById(R.id.btn_imc_send);


        btnSend.setOnClickListener(view -> {
            // validate
            if (!validate()) {
                Toast.makeText(ImcActivity.this, R.string.invalide_fields, Toast.LENGTH_LONG).show();
                return;
            }
            /* Pegando os valores do Layout */
            String sWeight = editWeight.getText().toString();
            String sHeight = editHeight.getText().toString();

            int weight = Integer.parseInt(sWeight);
            int height = Integer.parseInt(sHeight);

            double resultIMC = calculoImc(weight, height);
            // teste para checar o valor do IMC no log
            Log.d("teste", "resultado" + resultIMC);

            int imcResponseID = imcResponse(resultIMC);

            // PopUp
            AlertDialog dialog = new AlertDialog.Builder(ImcActivity.this)
                    .setTitle(getString(R.string.imc_response, resultIMC))
                    .setMessage(imcResponseID)
                    // lado direito
                    .setNegativeButton(android.R.string.ok, (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    })
                    .setPositiveButton(R.string.save, ((dialog1, which) -> new Thread(() -> {
                        int updateId = 0;
                        /* Verifica se tem algum ID vindo da tela anterior quando chamado UPDATE */
                        if (getIntent().getExtras() != null)
                            updateId = getIntent().getExtras().getInt("updateId", 0);

                        long calcID;

                        if (updateId > 0) {
                            calcID = SqlHelper.getInstance(ImcActivity.this).updateItem("imc", resultIMC, updateId);
                        } else {
                            calcID = SqlHelper.getInstance(ImcActivity.this).addItem("imc", resultIMC);
                        }


                        runOnUiThread(() -> {
                            /* Caso o id seja != de 0 irá mostrar um toast para o usuário, criando
                             * uma nova intent para ir para uma nova activity que irá mostrar a lista
                             * de saves que ele fez */
                            if (calcID > 0) {
                                Toast.makeText(ImcActivity.this, R.string.saved, Toast.LENGTH_SHORT).show();
                                openSaveListCalcActivity();
                            }
                        });
                    }).start())).create(); // Chamada da nova Thread e criação da activity
            dialog.show(); // Chamando o PopUp

            /*
             * Interagindo com o keyboard e a tela do usuário
             * */
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editWeight.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(editHeight.getWindowToken(), 0);

        });


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
        Intent intent = new Intent(ImcActivity.this, SaveActivity.class);
        // -> passando dados para a nova tela passando um Bundle
        intent.putExtra("type", "imc");
        startActivity(intent);

    }


    @StringRes
    private int imcResponse(double imc) {
        /* Tabela de IMC */
        if (imc < 15)
            return R.string.imc_severely_low_weight;
        else if (imc < 16)
            return R.string.imc_very_low_weight;
        else if (imc < 18.5)
            return R.string.imc_low_weight;
        else if (imc < 25)
            return R.string.normal;
        else if (imc < 30)
            return R.string.imc_high_weight;
        else if (imc < 35)
            return R.string.imc_so_high_weight;
        else if (imc < 40)
            return R.string.imc_severely_high_weight;
        else
            return R.string.imc_extreme_weight;
    }


    private double calculoImc(int weight, int height) {
        // peso / (altura * altura) com casting
        return weight / (((double) height / 100) * ((double) height / 100));

    }

    /* Validação dos dados  */
    private boolean validate() {
        return !editWeight.getText().toString().startsWith("0")
                && (!editHeight.getText().toString().startsWith("0"))
                && !editHeight.getText().toString().isEmpty()
                && !editWeight.getText().toString().isEmpty();
    }
}