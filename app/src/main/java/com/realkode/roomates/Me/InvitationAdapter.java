package com.realkode.roomates.Me;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.realkode.roomates.ParseSubclassses.Invitation;
import com.realkode.roomates.ParseSubclassses.User;
import com.realkode.roomates.R;

/**
 * List adapter for the invitations
 */
public class InvitationAdapter extends ParseQueryAdapter<Invitation> {
    public InvitationAdapter(Context context) {

        super(context, new ParseQueryAdapter.QueryFactory<Invitation>() {
            public ParseQuery create() {
                ParseQuery query = new ParseQuery("Invitation");
                query.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
                query.whereEqualTo("invitee", User.getCurrentUser());
                query.include("inviter");
                query.include("household");
                query.orderByDescending("createdAt");

                return query;
            }
        });
    }

    // Customize the layout by overriding getItemView
    @Override
    public View getItemView(Invitation invitation, View view, ViewGroup parent) {
        if (view == null) {
            view = View.inflate(getContext(), R.layout.list_invitations_layout, null);
        }

        super.getItemView(invitation, view, parent);


        // Add the title view
        TextView invitedByTextView = (TextView) view.findViewById(R.id.invitedByTextView);
        invitedByTextView.setText(invitation.getInviter().getDisplayName());

        TextView householdTextView = (TextView) view.findViewById(R.id.householdTextView);
        householdTextView.setText(invitation.getHousehold().getHouseholdName());


        return view;
    }


}