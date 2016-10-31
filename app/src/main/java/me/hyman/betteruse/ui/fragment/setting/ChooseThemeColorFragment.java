package me.hyman.betteruse.ui.fragment.setting;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import me.hyman.betteruse.R;

public class ChooseThemeColorFragment extends DialogFragment {


    public static void launch(Activity context) {
        Fragment fragment = context.getFragmentManager().findFragmentByTag("DialogFragment");
        if (fragment != null) {
            context.getFragmentManager().beginTransaction().remove(fragment).commit();
        }

        ChooseThemeColorFragment dialogFragment = new ChooseThemeColorFragment();
        dialogFragment.show(context.getFragmentManager(), "DialogFragment");
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setCancelable(true);

        View view = View.inflate(getActivity(), R.layout.fragment_choose_theme_color, null);
       /* final ColorPicker mColorPicker = (ColorPicker) view.findViewById(R.id.colorPicker);
        int callback = getResources().getColor(R.color.material_deep_teal_500);
        mColorPicker.setColor(Utils.resolveColor(getActivity(), R.attr.colorPrimary, callback));*/

       /* return new AlertDialogWrapper.Builder(getActivity())
                .setView(view)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.setting,
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                               *//* int selected = mColorPicker.getColor();
                                String color = String.format("#%X", selected);*//*

                                // AppSettings.setThemeColor(color);

                                dialog.dismiss();

                                getActivity().getFragmentManager().beginTransaction().remove(ChooseThemeColorFragment.this)
                                        .commit();
                            }
                        }
                )
                .create();*/

        return super.onCreateDialog(savedInstanceState);
    }
}
