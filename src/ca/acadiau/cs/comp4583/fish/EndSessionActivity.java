package ca.acadiau.cs.comp4583.fish;

import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import ca.acadiau.cs.comp4583.fish.data.FishException;
import ca.acadiau.cs.comp4583.fish.data.FishingSession;
import ca.acadiau.cs.comp4583.fish.data.persistence.SessionStorageService;
import ca.acadiau.cs.comp4583.fish.data.persistence.SessionStorageService.SessionStorageBinder;

public class EndSessionActivity extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.end_session);
        Intent thisIntent = getIntent();
        final FishingSession session = (FishingSession) thisIntent.getSerializableExtra("Session");
        final Button complete_session_button = (Button) findViewById(R.id.complete_session_button);
        final Button back_session_button = (Button) findViewById(R.id.submit_new_session_data_button);
        final Spinner location_spinner = (Spinner) findViewById(R.id.location_text_spinner);
        final TimePicker endTimePicker = (TimePicker) findViewById(R.id.end_time_picker);

        List<String> locations = Arrays.asList(getResources().getStringArray(R.array.locations));

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, locations);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        location_spinner.setAdapter(dataAdapter);

        int position = dataAdapter.getPosition(session.getLocationName());
        location_spinner.setSelection(position);
        // --------------

        final EditText anglersText = (EditText) findViewById(R.id.num_anglers_text_edit);
        anglersText.setText(Integer.toString(session.getAnglers()));

        final CheckBox anglersEstimatedChbx = (CheckBox) findViewById(R.id.checkBox1);
        anglersEstimatedChbx.setChecked(!session.isExactAnglers());

        int number_of_fish = session.getFish().size();
        final EditText num_catches_Text = (EditText) findViewById(R.id.num_catches_text_edit);
        final EditText num_fish_Text = (EditText) findViewById(R.id.num_fishes_text_edit);
        final CheckBox estimated_catches_chbx = (CheckBox) findViewById(R.id.CheckBox02);
        estimated_catches_chbx.setChecked(!session.isExactCatches());

        if (session.getCatches() == 0)
        {
            num_catches_Text.setText(Integer.toString(number_of_fish));
            estimated_catches_chbx.setChecked(false);
        }
        else
            num_catches_Text.setText(Integer.toString(session.getCatches()));
        num_fish_Text.setText(Integer.toString(number_of_fish));

        final EditText linesText = (EditText) findViewById(R.id.num_rods_text_edit);
        linesText.setText(Integer.toString(session.getLines()));

        final CheckBox fish_num_edited = (CheckBox) findViewById(R.id.CheckBox01);

        // -----------------
        complete_session_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                session.setLocationName(location_spinner.getSelectedItem().toString());
                session.setAnglers(Integer.valueOf(anglersText.getText().toString()));
                session.setExactAnglers(!anglersEstimatedChbx.isChecked());
                session.setLines(Integer.valueOf(linesText.getText().toString()));
                session.setCatches(Integer.valueOf(num_catches_Text.getText().toString()));
                session.setExactCatches(!estimated_catches_chbx.isChecked());

                GregorianCalendar endCalendar = new GregorianCalendar();
                endCalendar.setTimeInMillis(session.getStartDate() * 1000);
                endCalendar.set(GregorianCalendar.HOUR, endTimePicker.getCurrentHour());
                endCalendar.set(GregorianCalendar.MINUTE, endTimePicker.getCurrentMinute());
                session.setEndDate(endCalendar.getTimeInMillis() / 1000);

                /* Having populated the session data, we need to start the
                 * submission service and pass it the session. */
                Intent serviceIntent = new Intent(EndSessionActivity.this, SessionStorageService.class);
                ServiceConnection serviceConnection = new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName name, IBinder service)
                    {
                        try
                        {
                            ((SessionStorageBinder) service).submitSession(session);
                        }
                        catch (FishException e)
                        {
                            /* TODO: Deal with potential validation errors. */
                        }

                        unbindService(this);
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName name)
                    {
                    }
                };

                /* Because bound services which are auto-started by being bound
                 * are stopped when all bindings have been unbound, we manually
                 * start the service. It may already have been started by
                 * FishApplication, but repeatedly calling startService(Intent)
                 * shouldn't hurt anything. */
                startService(serviceIntent);
                bindService(serviceIntent, serviceConnection, 0);
            }
        });
        back_session_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                Intent i = new Intent(getApplicationContext(), SubmitFishActivity.class);
                i.putExtra("Session", session);
                startActivity(i);

            }
        });
        num_fish_Text.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s)
            {

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                    int arg2, int arg3)
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1,
                    int arg2, int arg3)
            {
                fish_num_edited.setChecked(true);

            }
        });

    }

}
