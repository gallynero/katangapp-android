package es.craftsmanship.toledo.katangapp.activities;

import es.craftsmanship.toledo.katangapp.interactors.StopsInteractor;
import es.craftsmanship.toledo.katangapp.models.QueryResult;
import es.craftsmanship.toledo.katangapp.utils.KatangaFont;

import at.markushi.ui.CircleButton;

import android.Manifest;

import android.app.Dialog;

import android.content.Intent;
import android.content.pm.PackageManager;

import android.graphics.Typeface;

import android.os.Bundle;

import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import android.util.Log;

import android.view.View;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

/**
 * @author Cristóbal Hermida
 * @author Javier Gamarra
 */
public class MainActivity extends BaseGeoLocatedActivity {

    private static final int DEFAULT_RADIO = 500;

    private static final String TAG = "KATANGAPP";

    private CircleButton searchButton;
    private ImageView infoIcon;
    private ProgressBar searchProgressBar;
    private SeekBar seekBar;
    private TextView radioLabel;

    @Subscribe
    public void busStopsReceived(QueryResult queryResult) {
        Intent intent = new Intent(MainActivity.this, ShowStopsActivity.class);

        intent.putExtra("queryResult", queryResult);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        getApplicationContext().startActivity(intent);

        toggleVisualComponents(true);
    }

    @Subscribe
    public void busStopsReceived(Error error) {
        toggleVisualComponents(true);

        Log.e(TAG, "Error calling server ", error);

        View content = findViewById(android.R.id.content);

        Snackbar.make(content, "Error finding the nearest stop", Snackbar.LENGTH_LONG).show();
    }

    private void checkRuntimePermissions(String[] permissions, int requestCode) {
        boolean explanation = false;

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    explanation = true;
                }
                else {
                    // No explanation needed, we can request the permission.

                    // requestCode is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }
        }

        if (!explanation) {
            ActivityCompat.requestPermissions(this, permissions, requestCode);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        initializeRuntimePermissions();

        initializeVariables();

        initializeSeekTrack();

        initializeClickableComponents();
    }

    private void initializeSeekTrack() {
        radioLabel.setText(String.valueOf(seekBar.getProgress()));
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                radioLabel.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                searchButton.setPressed(true);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                searchButton.setPressed(false);
            }

        });
    }

    private void initializeClickableComponents() {
        infoIcon.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showInfoOverLay();
            }

        });

        searchButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                CharSequence charSequence = radioLabel.getText();

                String radio = charSequence.toString();

                if (radio.isEmpty()) {
                    radio = String.valueOf(DEFAULT_RADIO);
                }

                toggleVisualComponents(false);

                StopsInteractor stopsInteractor = new StopsInteractor(
                    radio, getLatitude(), getLongitude());

                new Thread(stopsInteractor).start();
            }

        });

    }

    private void initializeRuntimePermissions() {
        int requestCode = PackageManager.PERMISSION_GRANTED;

        String[] permissions = {
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.INTERNET
        };

        checkRuntimePermissions(permissions, requestCode);
    }

    /**
     * A private method to help us initialize our variables
     */
    private void initializeVariables() {

        infoIcon = (ImageView) findViewById(R.id.infoIcon);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        radioLabel = (TextView) findViewById(R.id.radioLabel);
        searchButton = (CircleButton) findViewById(R.id.searchButton);
        searchProgressBar = (ProgressBar) findViewById(R.id.searchProgressBar);

        toggleVisualComponents(true);

        Typeface tf = KatangaFont.getFont(getAssets(), KatangaFont.QUICKSAND_REGULAR);

        TextView txtKatangaLabel = (TextView) findViewById(R.id.title_katanga);

        txtKatangaLabel.setTypeface(tf);
        radioLabel.setTypeface(tf);
    }

    /**
     * Shows the overlay dialog showing the application information.
     */
    private void showInfoOverLay() {
        final Dialog dialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);

        dialog.setContentView(R.layout.overlay_view);

        LinearLayout layout = (LinearLayout) dialog.findViewById(R.id.overlayLayout);

        layout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
            }

        });

        dialog.show();
    }

    private void toggleVisualComponents(boolean buttonEnabled) {
        searchButton.setEnabled(buttonEnabled);

        int visibility = View.VISIBLE;

        radioLabel.setVisibility(visibility);

        if (buttonEnabled) {
            visibility = View.INVISIBLE;
        }

        searchProgressBar.setVisibility(visibility);
    }

}