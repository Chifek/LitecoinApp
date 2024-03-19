package com.example.litecoinapp;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    private TextView dataTextView;
    private Button refreshButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataTextView = findViewById(R.id.dataTextView);
        refreshButton = findViewById(R.id.refreshButton);

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FetchData().execute();
            }
        });

        new FetchData().execute();
    }

    private class FetchData extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            String result = "";
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL("https://www.litecoinpool.org/api?api_key=c66100ae714050d3c86f95d4e35d7210");
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                result = stringBuilder.toString();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                JSONObject jsonObject = new JSONObject(result);

                // Извлекаем данные о workers
                JSONObject workersObject = jsonObject.getJSONObject("workers");
                int numWorkers = workersObject.length(); // Количество workers

                // Определяем количество подключенных workers и их хешрейт
                int connectedWorkers = 0;
                double totalHashRate = 0;
                Iterator<String> keys = workersObject.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    JSONObject worker = workersObject.getJSONObject(key);
                    boolean connected = worker.getBoolean("connected");
                    if (connected) {
                        connectedWorkers++;
                        totalHashRate += worker.getInt("hash_rate");
                    }
                }

                double hashRateMhs = (double) totalHashRate / 1_000.0; // Переводим в MH/s с использованием дробного деления

                String formattedHashRateMhs = String.format("%.0f MH/s", hashRateMhs); // Форматируем значение

                System.out.println("hashRateMhs " + hashRateMhs); // Выводим результат
                System.out.println("formattedHashRateMhs " + formattedHashRateMhs); // Выводим результат

                // Извлекаем данные о рынке
                JSONObject userObject = jsonObject.getJSONObject("user");
                double pastRewards24Doge = userObject.getDouble("past_24h_rewards_doge");
                double pastRewards24Ltc = userObject.getDouble("past_24h_rewards");
                double unpaidRewardsDoge = userObject.getDouble("unpaid_rewards_doge");
                double unpaidRewardsLtc = userObject.getDouble("unpaid_rewards");
                String formattedUnpaidRewards = String.format("%.4f", unpaidRewardsLtc);

                String pastRewards24LtcString = String.valueOf(pastRewards24Ltc);
                if (pastRewards24LtcString.length() > 10) {
                    pastRewards24LtcString = pastRewards24LtcString.substring(0, 8);
                }
                String pastRewards24DogeString = String.valueOf(pastRewards24Doge);
                if (pastRewards24DogeString.length() > 4) {
                    pastRewards24DogeString = pastRewards24DogeString.substring(0, 4);
                }
                String unpaidRewardsDogeString = String.valueOf('0');
                if (unpaidRewardsDoge != 0) {
                    unpaidRewardsDogeString = String.valueOf(unpaidRewardsDoge);
                    if (unpaidRewardsDogeString.length() > 4) {
                        unpaidRewardsDogeString = unpaidRewardsDogeString.substring(0, 4);
                    }
                }

                JSONObject marketObject = jsonObject.getJSONObject("market");
                double ltcUsd = marketObject.getDouble("ltc_usd");
                double dogeUsd = marketObject.getDouble("doge_usd");

                double totalUsdDoge = dogeUsd * pastRewards24Doge;
                double totalUsdLtc = ltcUsd * pastRewards24Ltc;

                double totalUnpaidDoge = 0.0;
                if (unpaidRewardsDoge != 0) {
                    totalUnpaidDoge = dogeUsd * unpaidRewardsDoge;
                }
                double totalUnpaidLtc = ltcUsd * unpaidRewardsLtc;

                String totalUnpaidDogeString = String.valueOf(totalUnpaidDoge);
                if (totalUnpaidDogeString.length() > 4) {
                    totalUnpaidDogeString = totalUnpaidDogeString.substring(0, 4);
                }
                String totalUsdLtcString = String.valueOf(totalUsdLtc);
                if (totalUsdLtcString.length() > 4) {
                    totalUsdLtcString = totalUsdLtcString.substring(0, 4);
                }
                String totalUsdDogeString = String.valueOf(totalUsdDoge);
                if (totalUsdDogeString.length() > 4) {
                    totalUsdDogeString = totalUsdDogeString.substring(0, 4);
                }
                double totalAmount24 = totalUsdDoge + totalUsdLtc;
                String totalAmount24String = String.valueOf(totalAmount24);
                if (totalAmount24String.length() > 4) {
                    totalAmount24String = totalAmount24String.substring(0, 4);
                }
                String totalUnpaidLtcString = String.valueOf(totalUnpaidLtc);
                if (totalUnpaidLtcString.length() > 4) {
                    totalUnpaidLtcString = totalUnpaidLtcString.substring(0, 4);
                }

                double totalNow = totalUnpaidDoge + totalUnpaidLtc;
                String totalNowString = String.valueOf(totalNow);
                if (totalNowString.length() > 4) {
                    totalNowString = totalNowString.substring(0, 4);
                }

                // Формируем строку для отображения в TextView
                String displayText =
                        "\nКоличество workers: " + numWorkers +
                        "\nПодключенных workers: " + connectedWorkers +
                        "\nСуммарный хешрейт workers: " + formattedHashRateMhs +
                        "\n_____________________________" +
                        "\nЗаработано за 24 часа:" +
                        "\n" + pastRewards24DogeString + " DOGE ~" + totalUsdDogeString + "$" +
                        "\n" + pastRewards24LtcString + " LTC ~" + totalUsdLtcString + "$" +
                        "\n_____________________________" +
                        "\nЗаработано за 24 часа: " + totalAmount24String + "$" +
                        "\n_____________________________" +
                        "\nТекущий баланс:" +
                        "\n" + unpaidRewardsDogeString + " DOGE ~" + totalUnpaidDogeString + "$" +
                        "\n" + formattedUnpaidRewards + " LTC ~" + totalUnpaidLtcString + "$" +
                        "\nИтого: " + totalNowString + "$" +
                        "\n_____________________________" +
                        "\nКурс DOGE к USD: " + dogeUsd +
                        "\nКурс LTC к USD: " + ltcUsd;

                // Устанавливаем сформированную строку в TextView
                dataTextView.setText(displayText);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
}
