package com.averi.worldscribe.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.averi.worldscribe.Category;
import com.averi.worldscribe.LinkedArticleList;
import com.averi.worldscribe.Membership;
import com.averi.worldscribe.R;
import com.averi.worldscribe.activities.EditMembershipActivity;
import com.averi.worldscribe.activities.GroupActivity;
import com.averi.worldscribe.utilities.ExternalDeleter;
import com.averi.worldscribe.utilities.IntentFields;
import com.averi.worldscribe.utilities.ExternalReader;

import java.util.ArrayList;

/**
 * Created by mark on 02/07/16.
 * An Adapter for RecyclerViews displaying a Person's Memberships.
 */
public class MembershipsAdapter extends RecyclerView.Adapter<MembershipsAdapter.MembershipHolder>
implements ArticleLinkAdapter {

    public class MembershipHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final Context context;
        private final CardView membershipCard;
        private final TextView groupNameText;
        private final TextView memberRoleText;
        private final ImageButton editButton;
        private final ImageButton deleteButton;
        private final String worldName;
        private Membership membership;

        /**
         * Instantiate a new MembershipHolder.
         * @param context The Context possessing this MembershipHolder.
         * @param worldName The name of the current World.
         * @param membershipCard The Membership Card layout this object will hold.
         */
        public MembershipHolder(Context context, String worldName, View membershipCard) {
            super(membershipCard);

            this.context = context;
            this.membershipCard = (CardView) membershipCard;
            groupNameText = (TextView) this.membershipCard.findViewById(R.id.linkedArticleName);
            memberRoleText = (TextView) this.membershipCard.findViewById(R.id.memberRole);
            editButton = (ImageButton) this.membershipCard.findViewById(R.id.edit);
            deleteButton = (ImageButton) this.membershipCard.findViewById(R.id.delete);
            this.worldName = worldName;

            this.membershipCard.setOnClickListener(this);
            editButton.setOnClickListener(this);
            deleteButton.setOnClickListener(this);
        }

        /**
         * Store Membership data and use it to set the card's text.
         * @param membership The Membership represented by the card.
         */
        public void bindMembership(Membership membership) {
            this.membership = membership;
            setMembershipText();
        }

        /**
         * Set the card's text using data from the attached Membership.
         */
        private void setMembershipText() {
            groupNameText.setText(membership.groupName);

            // An empty String means that the Member has no designated role/rank.
            if (membership.memberRole.isEmpty()) {
                memberRoleText.setVisibility(View.GONE);
            } else {
                memberRoleText.setText(context.getString(R.string.memberRoleText,
                        membership.memberRole));
            }
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == editButton.getId()) {
                editMembership();
            } else if (view.getId() == deleteButton.getId()) {
                confirmMembershipDeletion();
            } else {
                goToGroup();
            }
        }

        /**
         * Deletes the Membership represented by this ViewHolder upon user confirmation.
         */
        private void confirmMembershipDeletion() {
            new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.confirmMembershipDeletionTitle,
                            membership.groupName))
                    .setMessage(context.getString(R.string.confirmMembershipDeletion,
                            membership.groupName))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            deleteMembership();
                        }})
                    .setNegativeButton(android.R.string.no, null).show();
        }

        /**
         * Deletes the Membership represented by this ViewHolder.
         * If an error occurs during deletion, an error message is displayed.
         */
        public void deleteMembership() {
            boolean membershipWasDeleted = ExternalDeleter.deleteMembership(context, membership);
            if (membershipWasDeleted) {
                // Update MembershipsAdapter.
            } else {
                Toast.makeText(context, context.getString(R.string.deleteMembershipError,
                               membership.groupName),
                        Toast.LENGTH_SHORT).show();
            }
        }

        /**
         * Open the contained Membership in EditMembershipActivity for editing.
         */
        private void editMembership() {
            Intent editMembershipIntent = new Intent(context, EditMembershipActivity.class);
            editMembershipIntent.putExtra(IntentFields.MEMBERSHIP, membership);
            context.startActivity(editMembershipIntent);
        }

        /**
         * Open the connected Group in a new GroupActivity.
         */
        private void goToGroup() {
            Intent goToGroupIntent = new Intent(context, GroupActivity.class);

            goToGroupIntent.putExtra(IntentFields.WORLD_NAME, worldName);
            goToGroupIntent.putExtra(IntentFields.CATEGORY, Category.Group);
            goToGroupIntent.putExtra(IntentFields.ARTICLE_NAME, membership.groupName);

            context.startActivity(goToGroupIntent);
        }
    }

    private final ArrayList<Membership> memberships;
    private final Context context;
    private final String worldName;

    /**
     * Instantiate a new MembershipsAdapter.
     * @param context The Context possessing this MembershipsAdapter.
     * @param worldName The name of the current World.
     * @param personName The name of the Person whose Memberships are contained in this Adapter.
     */
    public MembershipsAdapter(Context context, String worldName, String personName) {
        this.context = context;
        this.worldName = worldName;

        memberships = ExternalReader.getMembershipsForPerson(context, worldName, personName);
    }

    @Override
    public MembershipHolder onCreateViewHolder(ViewGroup parent, int pos) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.membership_card, parent, false);
        return new MembershipHolder(context, worldName, view);
    }

    @Override
    public void onBindViewHolder(MembershipHolder holder, int pos) {
        holder.bindMembership(memberships.get(pos));
    }

    @Override
    public int getItemCount() {
        return memberships.size();
    }

    public LinkedArticleList getLinkedArticleList() {
        LinkedArticleList linkedArticleList = new LinkedArticleList();

        for (Membership membership : memberships) {
            linkedArticleList.addArticle(Category.Group, membership.groupName);
        }

        return  linkedArticleList;
    }
}
