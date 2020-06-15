package es.upv.etsit.aatt.paco.prediccindeltiempo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

     String TAG ="nolose" ;
     String url="https://opendata.aemet.es/opendata/api/prediccion/especifica/municipio/diaria/23039/?api_key=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjaXNhbGppQHRlbGVjby51cHYuZXMiLCJqdGkiOiI3ZmY4MGIwZS01ZGM0LTRhNTItYmM1Ny05MTI2ZGYxMGFlNTciLCJpc3MiOiJBRU1FVCIsImlhdCI6MTU4OTcxMzcxOSwidXNlcklkIjoiN2ZmODBiMGUtNWRjNC00YTUyLWJjNTctOTEyNmRmMTBhZTU3Iiwicm9sZSI6IiJ9.pNafp_N3NM-6HCga_k2_hxK7HjPClirl2pxH5ZG4-dU\n";
    boolean primera_vez = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /****/
        // Creación de tarea asíncrona
        TareaAsincrona Tarea= new TareaAsincrona();
        // Ejecución de hilo de tarea asíncrona
        Tarea.execute(url);
    }



    class TareaAsincrona extends AsyncTask<String,String,String> {


        @Override
        protected String doInBackground(String[] uri) {
            // Llamada a petición API-REST con la URI o URL indicada en el método
            // .execute.
           String respuesta=API_REST(uri[0]);

            // Por último, retorno del string entregado por la llamada
            // a la API-REST

            /****/
            return respuesta;
        }

        @Override
        protected void onPostExecute(String respuesta) {

            if (respuesta!=null) {
                try {

                    if (primera_vez) {
                        primera_vez = false;

                        // Obtención de la propiedad "datos" del JSON
                        JSONObject respues = new JSONObject(respuesta);
                        String datos= respues.getString("datos");

                        // Creación de una nuevo objeto de TareaAsincrona
                        TareaAsincrona Tarea2= new TareaAsincrona();
                        // Ejecución del hilo correspondiente
                        Tarea2.execute(datos);
                        /****/

                    } else { // segunda vez: recogida de respuesta de la segunda llamada
                        JSONArray respuest2 = new JSONArray(respuesta);
                        // Obtencion de las propiedades oportunas del JSON recibido
                        //String propiedades= respuest2.getString("datos");

                        double temperatura =  respuest2.getJSONObject(0).getJSONObject("prediccion").getJSONArray("dia").getJSONObject(1).getJSONObject("temperatura").getJSONArray("dato").getJSONObject(2).getDouble("value");
                        double precipitacion = respuest2.getJSONObject(0).getJSONObject("prediccion").getJSONArray("dia").getJSONObject(1).getJSONArray("probPrecipitacion").getJSONObject(5).getInt("value");
                        String  dirviento = respuest2.getJSONObject(0).getJSONObject("prediccion").getJSONArray("dia").getJSONObject(1).getJSONArray("viento").getJSONObject(5).getString("direccion");
                        double  velviento = respuest2.getJSONObject(0).getJSONObject("prediccion").getJSONArray("dia").getJSONObject(1).getJSONArray("viento").getJSONObject(5).getDouble("velocidad");
                        String  estadocielo = respuest2.getJSONObject(0).getJSONObject("prediccion").getJSONArray("dia").getJSONObject(1).getJSONArray("estadoCielo").getJSONObject(5).getString("value");
                        // Aquí ya se puede acceder a la UI, ya que estamos en el hilo
                        // convencional de ejecución, y por tanto ya se puede modificar
                        // el contenido de los TextView que contienen los valores de los datos.

                        TextView textmunicipio = (TextView) findViewById(R.id.municipio);
                        textmunicipio.setText("Guarroman");


                        TextView texttemperatura = (TextView) findViewById(R.id.temperatura);
                        texttemperatura.setText("" + temperatura +" ºC");

                        TextView textprecipitacion = (TextView) findViewById(R.id.probPrecipit);
                        textprecipitacion.setText("" + precipitacion + " %");

                        TextView textdirviento = (TextView) findViewById(R.id.dirViento);
                        textdirviento.setText("" + dirviento);

                        TextView textvelviento = (TextView) findViewById(R.id.velViento);
                        textvelviento.setText("" + velviento + " Km/h");

                        TextView textestadocielo = (TextView) findViewById(R.id.estadoCielo);
                        textestadocielo.setText("" + estadocielo);


                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d(TAG, "Problemas decodificando JSON");
                }
            }

        } // onPostExecute


    } // TareaAsincrona




    /** La peticion del argumento es recogida y devuelta por el método API_REST.
     Si hay algun problema se retorna null */
    public String API_REST(String uri){

        StringBuffer response = null;

        try {
            URL url = new URL(uri);
            Log.d(TAG, "URL: " + uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // Detalles de HTTP
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            Log.d(TAG, "Codigo de respuesta: " + responseCode);
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                String output;
                response = new StringBuffer();

                while ((output = in.readLine()) != null) {
                    response.append(output);
                }
                in.close();
            } else {
                Log.d(TAG, "responseCode: " + responseCode);
                return null; // retorna null anticipadamente si hay algun problema
            }
        } catch(Exception e) { // Posibles excepciones: MalformedURLException, IOException y ProtocolException
            e.printStackTrace();
            Log.d(TAG, "Error conexión HTTP:" + e.toString());
            return null; // retorna null anticipadamente si hay algun problema
        }

        return new String(response); // de StringBuffer -response- pasamos a String

    } // API_REST


}
