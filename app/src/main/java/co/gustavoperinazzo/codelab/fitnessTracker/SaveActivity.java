package co.gustavoperinazzo.codelab.fitnessTracker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SaveActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save);

        RecyclerView rv_save = findViewById(R.id.rv_list);

        Bundle extras = getIntent().getExtras();
        // new thread
        if (extras != null) {
            String type = extras.getString("type");

            new Thread(() -> {
                List<Register> registers = SqlHelper.getInstance(this).getRegisterBy(type);

                /* Roda o primeiro código na thread de código*/
                runOnUiThread(() -> {
                    Log.d("teste de registros", registers.toString());

                    /* Set do adapter */
                    ListAdapter adapter = new ListAdapter(registers);
                    rv_save.setLayoutManager(new LinearLayoutManager(this));
                    rv_save.setAdapter(adapter);
                });
            }).start();
        }
    }

    private class ListAdapter extends RecyclerView.Adapter<ListViewHolder> implements OnAdapterItemClickListener {

        private final List<Register> registersList;

        private ListAdapter(List<Register> registersList) {
            this.registersList = registersList;
        }


        @NonNull
        @Override
        public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ListViewHolder(getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false));
        }


        @Override
        public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
            Register currentRegister = registersList.get(position);
            holder.bind(currentRegister, this);
        }

        @Override
        public int getItemCount() {
            return registersList.size();
        }

        @Override
        public void onLongClick(int position, String type, int id) {
            /* Evento para exclusão dos itens ao usuário */
            AlertDialog.Builder builder = new AlertDialog.Builder(SaveActivity.this);
            builder.setMessage(getString(R.string.delete_itens));
            builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
                dialog.dismiss();
            });
            builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                new Thread(() -> {
                    SqlHelper sqlHelper = SqlHelper.getInstance(SaveActivity.this);
                    long calcId = sqlHelper.deleteItem(type, id);

                    runOnUiThread(() -> {
                        if (calcId > 0) {
                            Toast.makeText(SaveActivity.this, R.string.successfully_deleted, Toast.LENGTH_SHORT).show();
                            registersList.remove(position);
                            notifyDataSetChanged();
                            Log.d("Exlusão", "" + registersList.remove(position));
                        }
                    });
                }).start();
            }); AlertDialog alertDialog = builder
                    .create();

            alertDialog.show();
        }


        @Override
        public void onClick(int id, String type) {
            /* Verificar qual tipo de dado deve ser editado na proxima tela */
            switch (type) {
                case "imc":
                    Intent intent = new Intent(SaveActivity.this, ImcActivity.class);
                    intent.putExtra("updateId", id);
                    startActivity(intent);
                    break;
                case "tmb":
                    Intent intent1 = new Intent(SaveActivity.this, TmbActivity.class);
                    intent1.putExtra("updateId", id);
                    startActivity(intent1);
                    break;
            }
        }
    }


    private class ListViewHolder extends RecyclerView.ViewHolder {

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void bind(Register register, final OnAdapterItemClickListener onItemClickListener) {
            String formatted = "";
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("pt", "br"));
                Date dateSaved = sdf.parse(register.created_date);
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", new Locale("pt", "br"));
                formatted = dateFormat.format(dateSaved);

            } catch (ParseException e) {
            }

            /* Casting do objeto que já é uma view */
            ((TextView) itemView).setText(
                    getString(R.string.save_response, register.response, formatted)
            );

            /* Evento para Click e abrir a edição */
            itemView.setOnClickListener(v -> {
                onItemClickListener.onClick(register.id, register.type);
            });

            /* Evento de Click-longo - remover listagem  */
            itemView.setOnLongClickListener(v -> {
                onItemClickListener.onLongClick(getAbsoluteAdapterPosition(), register.type, register.id);
                return false;
            });
        }
    }
}
