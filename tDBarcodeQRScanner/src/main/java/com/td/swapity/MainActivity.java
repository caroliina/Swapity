package com.td.swapity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.android.encode.QRCodeEncoder;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.ui.ParseLoginBuilder;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

	public int SCANNER_REQUEST_CODE = 123;

    AppSectionsPagerAdapter mAppSectionsPagerAdapter;
    Bundle extras;

    static String fullname;
    static String currentUsersId;

    /**
     * The {@link ViewPager} that will display the three primary sections of the app, one at a
     * time.
     */
    ViewPager mViewPager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        extras = getIntent().getExtras();
        if(extras != null) {
            fullname = extras.getString("Fullname");
        }

        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();

        // Specify that the Home/Up button should not be enabled, since there is no hierarchical
        // parent.
        actionBar.setDisplayShowHomeEnabled(false);  // hides action bar icon
        actionBar.setDisplayShowTitleEnabled(false); // hides action bar title
        actionBar.setHomeButtonEnabled(false);

        // Specify that we will be displaying tabs in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Set up the ViewPager, attaching the adapter and setting up a listener for when the
        // user swipes between sections.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When swiping between different app sections, select the corresponding tab.
                // We can also use ActionBar.Tab#select() to do this if we have a reference to the
                // Tab.
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by the adapter.
            // Also specify this Activity object, which implements the TabListener interface, as the
            // listener for when this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mAppSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {

		if (requestCode == SCANNER_REQUEST_CODE) {
			// Handle scan intent
			if (resultCode == Activity.RESULT_OK) {
				// Handle successful scan
				String contents = intent.getStringExtra("SCAN_RESULT");
				String formatName = intent.getStringExtra("SCAN_RESULT_FORMAT");
				byte[] rawBytes = intent.getByteArrayExtra("SCAN_RESULT_BYTES");
				int intentOrientation = intent.getIntExtra("SCAN_RESULT_ORIENTATION", Integer.MIN_VALUE);
				Integer orientation = (intentOrientation == Integer.MIN_VALUE) ? null : intentOrientation;
				String errorCorrectionLevel = intent.getStringExtra("SCAN_RESULT_ERROR_CORRECTION_LEVEL");

                ParseObject scanResults = new ParseObject("Scan");
                scanResults.put("resultId", contents);

                extras = getIntent().getExtras();
                if(extras != null) {
                    currentUsersId = extras.getString("UserID");
                }

                scanResults.put("scannerId", currentUsersId);
                scanResults.saveInBackground();

			} else if (resultCode == Activity.RESULT_CANCELED) {
				// Handle cancel
			}
		} else {
			// Handle other intents
		}

	}

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
        if (tab.getPosition() == 1) {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "SCAN_MODE");
            startActivityForResult(intent, 123);
        }
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
     * sections of the app.
     */
    public class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return new ProfileSectionFragment();
                case 1:
                    return new LaunchpadSectionFragment();
                case 2:
                    return new ListSectionFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "PROFILE";
                case 1:
                    return "SCANNER";
                case 2:
                    return "CONTACTS";
            }
            return null;
        }
    }

    /**
     * A fragment that launches other parts of the demo application.
     */
    public static class LaunchpadSectionFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_section_launchpad, container, false);

            return rootView;
        }
    }


    @SuppressLint("ValidFragment")
    public class ProfileSectionFragment extends Fragment {

        protected Activity mActivity;
        private TextView intro;
        private Button loginOrLogoutButton;
        private Switch facebook_switch;
        private ParseUser currentUser;

        @Override
        public void onAttach(Activity act)
        {
            super.onAttach(act);
            mActivity = act;
        }

        @Override
        public void onCreate(Bundle saveInstanceState)
        {
            super.onCreate(saveInstanceState);
        }

        @Override
        public void onActivityCreated(Bundle saveInstanceState)
        {
            super.onActivityCreated(saveInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 final Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_section_profile, container, false);

            intro = (TextView)view.findViewById(R.id.user_fullname_profile);
            intro.setText(fullname);

            extras = getIntent().getExtras();
            if(extras != null) {
                currentUsersId = extras.getString("UserID");
            }

            ParseQuery<ParseObject> query = ParseQuery.getQuery("QRCode");
            query.whereEqualTo("userId", currentUsersId);
            ParseObject object = null;

            try {
                object = query.getFirst();

            } catch (ParseException e) {

                    Intent intent = new Intent();
                    intent.setAction("com.google.zxing.client.android.ENCODE");
                    intent.putExtra("ENCODE_FORMAT", BarcodeFormat.QR_CODE.toString());
                    intent.putExtra("ENCODE_TYPE", "TEXT_TYPE");
                    intent.putExtra("ENCODE_DATA", currentUsersId);
                    QRCodeEncoder qrcode = null;
                    try {
                        qrcode = new QRCodeEncoder(getActivity(),intent,250,false);
                        Bitmap bitmap = qrcode.encodeAsBitmap();
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        byte[] image = stream.toByteArray();
                        ParseFile file = new ParseFile("qrcode.png", image);
                        file.saveInBackground();

                        ParseObject qrcodes = new ParseObject("QRCode");
                        qrcodes.put("ImageFile", file);
                        qrcodes.put("userId", currentUsersId);

                        try {
                            qrcodes.save();
                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }

                        ParseQuery<ParseObject> query2 = ParseQuery.getQuery("QRCode");
                        query2.whereEqualTo("userId", currentUsersId);
                        object = null;

                        try {
                            object = query.getFirst();
                        } catch (ParseException e2) {
                            e2.printStackTrace();
                        }

                    } catch (WriterException ex) {
                        ex.printStackTrace();
                    }

                e.printStackTrace();
            }

            generateQrCode(object);

            /*
            Toggle buttons
             */
            facebook_switch = (Switch)view.findViewById(R.id.switch_fb);
            currentUser = ParseUser.getCurrentUser();
            if(ParseFacebookUtils.isLinked(currentUser)) {
                facebook_switch.setChecked(true);
            }

            loginOrLogoutButton = (Button)view.findViewById(R.id.login_or_logout_button_profile);

            loginOrLogoutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LoginActivity la = new LoginActivity();
                    ParseUser.logOut();
                    currentUser = null;
                    Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                    startActivity(intent);
                }
            });

            return view;
        }
    }

    public void generateQrCode(ParseObject object){
        ParseFile fileObject = (ParseFile) object.get("ImageFile");
        fileObject.getDataInBackground(new GetDataCallback() {
            public void done(byte[] data, ParseException e) {
                if (e == null) {
                    Log.d("test", "We've got data in data.");
                    Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);

                    ImageView qrimage = (ImageView) findViewById(R.id.usersQrCode);
                    qrimage.setImageBitmap(bmp);

                } else {
                    Log.d("test", "There was a problem downloading the data.");
                }
            }
        });
    }


    @SuppressLint("ValidFragment")
    public class ListSectionFragment extends Fragment {

        protected Activity mActivity;
        private TextView intro;

        @Override
        public void onAttach(Activity act)
        {
            super.onAttach(act);
            mActivity = act;
        }

        @Override
        public void onCreate(Bundle saveInstanceState)
        {
            super.onCreate(saveInstanceState);
        }

        @Override
        public void onActivityCreated(Bundle saveInstanceState)
        {
            super.onActivityCreated(saveInstanceState);
        }

        public void findUserInfo(View view, List<ParseObject> scanRows){
            final ArrayList<String> scannedUsersList = new ArrayList<String>();

            for (ParseObject row : scanRows) {
                ParseQuery<ParseUser> usersQuery = ParseUser.getQuery();
                usersQuery.whereEqualTo("objectId", row.get("resultId"));
                ParseUser scannedUser = null;

                try {
                    scannedUser = usersQuery.getFirst();
                    Log.d("object", "" + scannedUser.get("name") + " E-mail: " + scannedUser.get("email"));
                    scannedUsersList.add(scannedUser.get("name") + " E-mail: " + scannedUser.get("email"));

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            Log.d("score", "Array " + scannedUsersList.size() + " size");

            ListView list = (ListView) view.findViewById(R.id.listview);
            list.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, scannedUsersList));

            list.setTextFilterEnabled(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View view = inflater.inflate(R.layout.fragment_section_dummy, container, false);

            extras = getIntent().getExtras();
            if(extras != null) {
                currentUsersId = extras.getString("UserID");
            }

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Scan");
            final List<ParseObject> scanRows = new ArrayList<ParseObject>();

            query.whereEqualTo("scannerId", currentUsersId);
            query.orderByAscending("createdAt");

            List<ParseObject> resultsList = null;

            try {
                resultsList = query.find();

                for (ParseObject result : resultsList) {
                    scanRows.add(result);
                }

                findUserInfo(view, scanRows);

            } catch (ParseException e) {
                e.printStackTrace();
            }

            return view;
        }
    }


}
