package com.tgf.user.personalaccountant;

import android.app.Activity;
import android.support.v7.widget.PopupMenu;

/**
 * Created by Akropon on 05.03.2017.
 */

public abstract class MenuClickListener implements PopupMenu.OnMenuItemClickListener{
    public Activity parentActivity;

    public MenuClickListener(Activity parentActivity) {
        this.parentActivity = parentActivity;
    }


}
