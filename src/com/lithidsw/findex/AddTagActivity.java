package com.lithidsw.findex;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.lithidsw.findex.R;
import com.lithidsw.findex.db.DBUtils;
import com.lithidsw.findex.utils.C;

public class AddTagActivity extends Activity  {

    private SharedPreferences mPrefs;
    private Context mContext;
    private EditText editText;
    private EditText editText1;
    private Spinner spinner;
    private Spinner spinner1;
    private EditText editText2;
    private Spinner spinner2;
    private LinearLayout linearLayout;
    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = getSharedPreferences(C.PREF, MODE_PRIVATE);
        setTheme(getResources().getIdentifier(
                mPrefs.getString(C.PREF_THEME, C.DEFAULT_THEME),
                "style",
                C.THIS)
        );
        setContentView(R.layout.add_tag_activity);
        mContext = this;
        setupActionBar();

        editText = (EditText) findViewById(R.id.new_tag_name);
        editText1 = (EditText) findViewById(R.id.new_tag_value);
        spinner = (Spinner) findViewById(R.id.time_spinner);
        spinner1 = (Spinner) findViewById(R.id.size_value_spinner);
        editText2 = (EditText) findViewById(R.id.size_edit);
        spinner2 = (Spinner) findViewById(R.id.size_type_spinner);
        linearLayout = (LinearLayout) findViewById(R.id.size_layout);
        radioGroup = (RadioGroup) findViewById(R.id.radio_group);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    radioGroup.setVisibility(View.VISIBLE);
                } else {
                    radioGroup.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio_none:
                        editText1.setVisibility(View.GONE);
                        spinner.setVisibility(View.GONE);
                        linearLayout.setVisibility(View.GONE);
                        break;
                    case R.id.radio_search:
                        editText1.setVisibility(View.VISIBLE);
                        spinner.setVisibility(View.GONE);
                        linearLayout.setVisibility(View.GONE);
                        break;
                    case R.id.radio_extension:
                        editText1.setVisibility(View.VISIBLE);
                        spinner.setVisibility(View.GONE);
                        linearLayout.setVisibility(View.GONE);
                        break;
                    case R.id.radio_time:
                        editText1.setVisibility(View.GONE);
                        spinner.setVisibility(View.VISIBLE);
                        linearLayout.setVisibility(View.GONE);
                        break;
                    case R.id.radio_size:
                        editText1.setVisibility(View.GONE);
                        spinner.setVisibility(View.GONE);
                        linearLayout.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });

        RadioButton radioButton = (RadioButton) findViewById(R.id.radio_none);
        radioButton.setText(Html.fromHtml("<b>None:</b><br>This will not search, must manually add this tag to files"));

        RadioButton radioButton1 = (RadioButton) findViewById(R.id.radio_search);
        radioButton1.setText(Html.fromHtml("<b>Search:</><br>This will tag items while matching the value within the file name</i>"));

        RadioButton radioButton2 = (RadioButton) findViewById(R.id.radio_extension);
        radioButton2.setText(Html.fromHtml("<b>Extension</b><br>This will tag files by extension"));

        RadioButton radioButton3 = (RadioButton) findViewById(R.id.radio_time);
        radioButton3.setText(Html.fromHtml("<b>Time</b><br>This tag will show you items based on time"));

        RadioButton radioButton4 = (RadioButton) findViewById(R.id.radio_size);
        radioButton4.setText(Html.fromHtml("<b>Size</b><br>This tag will show you items based on size"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_tag_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_save:
                Editable editable = editText.getText();
                Editable editable1 = editText1.getText();
                if (editable != null && !editable.toString().equals("")) {
                    int pos = radioGroup.getCheckedRadioButtonId();
                    String type = "";
                    String value = "";
                    boolean cont = false;
                    switch (pos) {
                        case R.id.radio_none:
                            type = "none";
                            value = editable.toString().replace(" ", "").toLowerCase();
                            cont = true;
                            break;
                        case R.id.radio_search:
                            if (editable1 != null && !editable1.toString().equals("")) {
                                type = "search";
                                value = editable1.toString();
                                cont = true;
                            }
                            break;
                        case R.id.radio_extension:
                            if (editable1 != null && !editable1.toString().equals("")) {
                                type = "extension";
                                value = editable1.toString();
                                cont = true;
                            }
                            break;
                        case R.id.radio_time:
                            type = "time";
                            value = String.valueOf(spinner.getSelectedItemPosition());
                            cont = true;
                            break;
                        case R.id.radio_size:
                            type = "size";

                            int math = 0;
                            Editable editable2 = editText2.getText();
                            if (editable2 != null) {
                                math = Integer.parseInt(editable2.toString());
                            }

                            switch (spinner2.getSelectedItemPosition()) {
                                case 0:
                                    math = math * 1024;
                                    break;
                                case 1:
                                    math = math * (1024 * 1024);
                                    break;
                                case 2:
                                    math = math * (1024 * (1024 * 1024));
                                    break;
                            }

                            value = spinner1.getSelectedItemPosition() + "::" + math;
                            cont = true;
                            break;
                    }

                    if (cont) {
                        String[] items = new String[3];
                        items[0] = editable.toString();
                        items[1] = type;
                        items[2] = value;
                        new DBUtils(mContext).addCustomTag(items);
                        finish();
                    }
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }
}
