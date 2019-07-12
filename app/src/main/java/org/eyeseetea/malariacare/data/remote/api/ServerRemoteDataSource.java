package org.eyeseetea.malariacare.data.remote.api;

import static org.eyeseetea.malariacare.data.remote.api.OkHttpClientDataSource.executeCall;
import static org.eyeseetea.malariacare.data.remote.api.OkHttpClientDataSource.parseResponse;

import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;

import org.eyeseetea.malariacare.data.IServerDataSource;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.entity.Server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.Response;

public class ServerRemoteDataSource implements IServerDataSource {

    private static final String SERVER_VERSION_CALL = "api/systemSettings/";
    private static final String KEY_FLAG_FIELD = "keyFlag";
    private static final String APPLICATION_TITLE_FIELD = "applicationTitle";

    private static final String LOGO_URL_ENDPOINT = "dhis-web-commons/flags/%s.png";

    private static final String TAG = ".ServerRemoteDataSource";
    private Credentials credentials;

    public ServerRemoteDataSource() {
    }

    @Override
    public Server get() throws Exception {
        Server server;
        try {
            credentials = PreferencesState.getInstance().getCreedentials();
            Response response = executeCall(new BasicAuthenticator(credentials),
                    credentials.getServerURL(), SERVER_VERSION_CALL);
            JsonNode jsonNode = parseResponse(response.body().string());
            String keyFlag = jsonNode.get(KEY_FLAG_FIELD).asText();
            String applicationTitle = jsonNode.get(APPLICATION_TITLE_FIELD).asText();

            byte[] logo = getLogo(keyFlag);

            server = new Server(credentials.getServerURL(), applicationTitle, logo);

        } catch (Exception ex) {
            Log.e(TAG, "Cannot read server name and logo");
            ex.printStackTrace();
            throw ex;
        }
        return server;
    }

    private byte[] getLogo(String keyFlag) {
        String logoEndpoint = String.format(LOGO_URL_ENDPOINT, keyFlag);

        URL baseUrl;
        try {
            baseUrl = new URL(credentials.getServerURL());

            URL logoUrl = new URL(baseUrl.getProtocol(), baseUrl.getHost(), baseUrl.getPort(),
                    logoEndpoint);

            return getLogoFromURL(logoUrl);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public byte[] getLogoFromURL(URL url) {
        try {
            HttpURLConnection urlcon = (HttpURLConnection) url.openConnection();
            urlcon.setDoInput(true);
            urlcon.connect();
            InputStream in = urlcon.getInputStream();
            byte[] targetArray = readBytes(in);
            return targetArray;
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] readBytes( InputStream stream ) throws IOException {
        if (stream == null) return new byte[] {};
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        boolean error = false;
        try {
            int numRead = 0;
            while ((numRead = stream.read(buffer)) > -1) {
                output.write(buffer, 0, numRead);
            }
        } catch (IOException e) {
            error = true; // this error should be thrown, even if there is an error closing stream
            throw e;
        } catch (RuntimeException e) {
            error = true; // this error should be thrown, even if there is an error closing stream
            throw e;
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                if (!error) throw e;
            }
        }
        output.flush();
        return output.toByteArray();
    }


    @Override
    public void save(Server server) {
        //Not implemented
    }
}
