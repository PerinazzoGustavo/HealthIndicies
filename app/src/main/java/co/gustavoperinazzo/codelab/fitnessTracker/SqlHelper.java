package co.gustavoperinazzo.codelab.fitnessTracker;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// Classe mãe com métodos OnCreate e OnUpdate
public class SqlHelper extends SQLiteOpenHelper {
    // NOME DO BANCO
    private static final String DB_NAME = "fitness_Tracker_db";
    // VERSÃO DO BANCO
    private static final int DB_VERSION = 1;

    // padrão singletle -> para poder instanciar apenas
    private static SqlHelper INSTANCE;

    // Caso exista uma isntância crie, retorne ela, caso contrario retorne uma nova isntancia(primeira e unica)
    static SqlHelper getInstance(Context context) {
        if (INSTANCE == null)
            INSTANCE = new SqlHelper(context);
        return INSTANCE; // unico objeto
    }

    // Constructor do banco de dados
    private SqlHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // Criação da tabela dentro do sql
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE calc (id INTEGER primary key, type_calc TEXT, response DECIMAL, created_date DATATIME)"

        );

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        Log.d("testeDB", "on Upgrade disparado");
    }

    @SuppressLint("Range")
    List<Register> getRegisterBy(String type) {
        // Espera como retorno uma tabela dentro do banco de dados

        /* Não há necessidade de abrir uma nova callTransaction pois não estamos escrevendo dentro
        * banco de dados, estamos somente buscando dados. */
        List<Register> registers = new ArrayList<>();

        /* Lendo os valores do banco de dados */
        SQLiteDatabase db = getReadableDatabase();

        // instrução de chamada de query, e retorna um cursor de banco de dados
        Cursor cursor = db.rawQuery("SELECT * FROM calc WHERE type_calc = ? ", new String[]{ type });

        try {
            // move para a primeira linha do sql
            if (cursor.moveToFirst()) {
                do {
                    Register register = new Register();
                    /* Busca os valores dentro do SQL pelo cursor e retorna uma lista de registros
                    *  com os parametros type, response e created_date dentro da tabela calc */
                    register.type = cursor.getString(cursor.getColumnIndex("type_calc"));
                    register.response = cursor.getDouble(cursor.getColumnIndex("response"));
                    register.created_date = cursor.getString(cursor.getColumnIndex("created_date"));
                    registers.add(register);
                } while (cursor.moveToNext()); // Enquanto houver um proximo item na tabela
            }
        } catch(Exception ex) {
            Log.e("SQLite", ex.getMessage(), ex);
        } finally {
            /* Fechando a call */
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }

        }
        /* Retornando a lista de itens de dentro do banco de dados */
        return registers;
    }


    // adicionando itens os parametros da tabela "calc"
    long addItem(String type, double response) {
        /* Escrevendo dentro do sql */
        SQLiteDatabase db = getWritableDatabase();


        long calcID = 0; // valor default para salvar

        try {
            // Abertura do banco
            db.beginTransaction();
            ContentValues values = new ContentValues();


            /* Inserindo valores dentro das tabelas especificas */
            values.put("type_calc", type);
            values.put("response", response);

            /* Chamando a data e hora atual e adicionando na tabela de horários */
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("pt", "br"));
            String actual_hour = sdf.format(new Date());
            values.put("created_date", actual_hour);

            /* Fazendo o insert dentro do banco, e passsando os valores que foram adicionados */
            calcID = db.insertOrThrow("calc", null, values); // escrevendo o Insert into
            db.setTransactionSuccessful(); // Comando de escrita
        } catch (Exception ex) {
            // Disparando excessão
            Log.e("SQLite", ex.getMessage(), ex);
        } finally {
            // Fechamento do banco
            if (db.isOpen())
                db.endTransaction();
        }
        return calcID; // Retornando o id dos itens
    }


    long updateItem(String type, double response, int id) {
        SQLiteDatabase db = getWritableDatabase();
        long calcId = 0;
        db.beginTransaction();

        try {
            ContentValues values = new ContentValues();

            values.put("type_calc", type);
            values.put("response", response);

            String actual_hour = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                    new Locale("pt", "br")).format(Calendar.getInstance().getTime());

            values.put("created_time", actual_hour);
            // where statement dentro do banco
            calcId = db.update("calc", values, "id = ? and type_calc = ?", new String[]{String.valueOf(id), type});
            db.setTransactionSuccessful();

        } catch (Exception e) {
            Log.e("SqLite", e.getMessage(), e);
        } finally {
            if (db.isOpen())
                db.endTransaction();
        }
        return calcId;
    }

    long deleteItem(String type, int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        long calcId = 0;

        try {
            calcId = db.delete("calc", "id = ? and type_calc = ?", new String[]{String.valueOf(id), type} );
            db.setTransactionSuccessful();
        } catch (Exception e){
            Log.e("Erro ao remover", e.getMessage(), e);
        } finally {
            db.endTransaction();
        }
        return calcId;
    }

}
