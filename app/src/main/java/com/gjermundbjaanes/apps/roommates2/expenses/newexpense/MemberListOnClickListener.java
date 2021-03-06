package com.gjermundbjaanes.apps.roommates2.expenses.newexpense;

import android.graphics.Color;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.gjermundbjaanes.apps.roommates2.parsesubclasses.User;
import com.parse.ParseUser;

import java.util.ArrayList;

class MemberListOnClickListener implements AdapterView.OnItemClickListener {
    private final ArrayList<User> notPaidList;
    private final ArrayList<User> paidList;
    private final ListView membersListView;

    public MemberListOnClickListener(ArrayList<User> notPaidList, ArrayList<User> paidList, ListView membersListView) {
        this.notPaidList = notPaidList;
        this.paidList = paidList;
        this.membersListView = membersListView;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        User clickedUser = (User) membersListView.getItemAtPosition(position);

        // Checking if clicked user should be put in or removed from paidlist or notpaidlist. If the user is currentuser, it
        // should be associated with paidlist, else notpaidlist
        if (clickedUser.getObjectId().equals(ParseUser.getCurrentUser().getObjectId()) &&
                paidList.contains(clickedUser)) {
            adapterView.getChildAt(position).setBackgroundColor(Color.WHITE);
            paidList.remove(clickedUser);
        } else if (clickedUser.getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
            adapterView.getChildAt(position).setBackgroundColor(Color.LTGRAY);
            paidList.add(clickedUser);
        } else if (notPaidList.contains(clickedUser)) {
            adapterView.getChildAt(position).setBackgroundColor(Color.WHITE);
            notPaidList.remove(clickedUser);
        } else {
            notPaidList.add(clickedUser);
            adapterView.getChildAt(position).setBackgroundColor(Color.LTGRAY);
        }
    }

}
