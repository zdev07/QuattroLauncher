package net.kdt.pojavlaunch.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;

/** Full Rewrite: Quattro Authentication Portal */
public class SelectAuthFragment extends Fragment {
    public static final String TAG = "QUATTRO_AUTH_FRAGMENT";

    public SelectAuthFragment() {
        super(R.layout.fragment_select_auth_method);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Rebrand UI Text
        TextView title = view.findViewById(R.id.auth_title);
        if (title != null) title.setText("Welcome to Quattro");

        Button mMicrosoftButton = view.findViewById(R.id.button_microsoft_authentication);
        Button mLocalButton = view.findViewById(R.id.button_local_authentication);

        // Modernized Click Listeners
        mMicrosoftButton.setOnClickListener(v -> 
            Tools.swapFragment(requireActivity(), MicrosoftLoginFragment.class, "QUATTRO_MS_LOGIN", null));
            
        mLocalButton.setOnClickListener(v -> 
            Tools.swapFragment(requireActivity(), LocalLoginFragment.class, "QUATTRO_LOCAL_LOGIN", null));
    }
}
