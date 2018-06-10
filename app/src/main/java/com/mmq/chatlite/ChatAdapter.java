package com.mmq.chatlite;

import android.app.Notification;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * Created by mmq on 05/08/2017.
 */

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final int ITEM_TYPE_LEFT = 0;
    private static final int ITEM_TYPE_RIGHT = 1;
    private static final int ITEM_TYPE_IMAGE_LEFT = 2;
    private static final int ITEM_TYPE_IMAGE_RIGHT = 3;

    private List<Messages> messagesList;

    ClipData myClip;
    ClipboardManager clipboard;

    public ChatAdapter(List<Messages> messagesList) {
        this.messagesList = messagesList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == ITEM_TYPE_LEFT) {
            View leftView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_left, parent, false);
            return new ChatLeftViewHolder(leftView); // view holder for left items
        } else if (viewType == ITEM_TYPE_RIGHT) {
            View rightRow = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_right, parent, false);
            return new ChatRightViewHolder(rightRow); // view holder for right items
        } else if (viewType == ITEM_TYPE_IMAGE_LEFT) {
            View leftView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_image_left, parent, false);
            return new ChatImageLeftViewHolder(leftView); // view holder for left image items
        } else if (viewType == ITEM_TYPE_IMAGE_RIGHT) {
            View rightRow = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_image_right, parent, false);
            return new ChatImageRightViewHolder(rightRow); // view holder for right image items
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder.getItemViewType() == ITEM_TYPE_IMAGE_LEFT) {
            // Set view holder type
            final ChatImageLeftViewHolder myholder = ((ChatImageLeftViewHolder)holder);

            // Get current msg item
            final Messages msg = messagesList.get(position);

            myholder.sentTime.setText(msg.getSentTime());

            if (URLUtil.isValidUrl(msg.getAvatar())) {
                Picasso.with(myholder.imageAvatar.getContext())
                        .load(msg.getAvatar())
                        .into(myholder.imageAvatar);
            }

            // Load image message
            if (URLUtil.isValidUrl(msg.getMessage())) {
                Picasso.with(myholder.itemMessage.getContext())
                        .load(msg.getMessage())
                        .into(myholder.itemMessage);
            }

            myholder.imageAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Context context = myholder.imageAvatar.getContext();
                    API.showProfileDialog(context, msg.getUid());
                }
            });
        }

        if (holder.getItemViewType() == ITEM_TYPE_IMAGE_RIGHT) {

            // Set view holder type
            final ChatImageRightViewHolder myholder = ((ChatImageRightViewHolder)holder);

            // Get current msg item
            Messages msg = messagesList.get(position);

            myholder.sentTime.setText(msg.getSentTime());

            if (URLUtil.isValidUrl(msg.getAvatar())) {
                Picasso.with(myholder.imageAvatar.getContext())
                        .load(msg.getAvatar())
                        .into(myholder.imageAvatar);
            }
            // Load image message
            if (URLUtil.isValidUrl(msg.getMessage())) {
                Picasso.with(myholder.itemMessage.getContext())
                        .load(msg.getMessage())
                        .into(myholder.itemMessage);
            }

            myholder.itemMessage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View view) {
                    final PopupMenu popup = new PopupMenu(myholder.itemMessage.getContext(), myholder.itemMessage);
                    popup.getMenuInflater().inflate(R.menu.context_menu, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.m_copy:
                                    clipboard = (ClipboardManager)view.getContext().getSystemService(CLIPBOARD_SERVICE);
                                    myClip = ClipData.newPlainText("text", messagesList.get(position).getMessage());
                                    clipboard.setPrimaryClip(myClip);
                                    break;
                            }
                            return false;
                        }
                    });
                    popup.show();
                    return false;
                }
            });
        }

        if (holder.getItemViewType() == ITEM_TYPE_LEFT) {

            // Set view holder type
            final ChatLeftViewHolder myholder = ((ChatLeftViewHolder)holder);

            // Get current msg item
            final Messages msg = messagesList.get(position);

            myholder.itemMessage.setText(msg.getMessage());
            myholder.sentTime.setText(msg.getSentTime());

            if (URLUtil.isValidUrl(msg.getAvatar())) {
                Picasso.with(myholder.imageAvatar.getContext())
                        .load(msg.getAvatar())
                        .into(myholder.imageAvatar);
            }

            // Click vao avatar thi hien dialog thong tin cua nguoi do
            myholder.imageAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Context context = myholder.imageAvatar.getContext();
                    API.showProfileDialog(context, msg.getUid());
                }
            });

            myholder.itemMessage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View view) {
                    final PopupMenu popup = new PopupMenu(myholder.itemMessage.getContext(), myholder.itemMessage);
                    popup.getMenuInflater().inflate(R.menu.context_menu, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.m_copy:
                                    clipboard = (ClipboardManager)view.getContext().getSystemService(CLIPBOARD_SERVICE);
                                    myClip = ClipData.newPlainText("text", messagesList.get(position).getMessage());
                                    clipboard.setPrimaryClip(myClip);
                                    break;
                            }
                            return false;
                        }
                    });
                    popup.show();
                    return false;
                }
            });
        }
        if (holder.getItemViewType() == ITEM_TYPE_RIGHT) {

            // Set view holder type
            final ChatRightViewHolder myholder = ((ChatRightViewHolder)holder);

            // Get current msg item
            Messages msg = messagesList.get(position);

            myholder.itemMessage.setText(msg.getMessage());
            myholder.sentTime.setText(msg.getSentTime());

            if (URLUtil.isValidUrl(msg.getAvatar())) {
                Picasso.with(myholder.imageAvatar.getContext())
                        .load(msg.getAvatar())
                        .into(myholder.imageAvatar);
            }

            myholder.itemMessage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View view) {
                    final PopupMenu popup = new PopupMenu(myholder.itemMessage.getContext(), myholder.itemMessage);
                    popup.getMenuInflater().inflate(R.menu.context_menu, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.m_copy:
                                    clipboard = (ClipboardManager)view.getContext().getSystemService(CLIPBOARD_SERVICE);
                                    myClip = ClipData.newPlainText("text", messagesList.get(position).getMessage());
                                    clipboard.setPrimaryClip(myClip);
                                    break;
                            }
                            return false;
                        }
                    });
                    popup.show();
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        // Get the current position message
        Messages message = messagesList.get(position);

        if (message.getUid().equals(API.currentUID)) {
            if (message.isImage())
                    return ITEM_TYPE_IMAGE_RIGHT;
            else
                    return ITEM_TYPE_RIGHT;
        } else {
            if (message.isImage())
                return ITEM_TYPE_IMAGE_LEFT;
            else
                return ITEM_TYPE_LEFT;
        }
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    public static class ChatImageLeftViewHolder extends RecyclerView.ViewHolder {

        public ImageView itemMessage;
        public ImageView imageAvatar;
        public TextView sentTime;

        public ChatImageLeftViewHolder(final View itemView) {
            super(itemView);
            itemMessage = itemView.findViewById(R.id.imageViewMessage);
            imageAvatar = itemView.findViewById(R.id.imageViewAvatar);
            sentTime = itemView.findViewById(R.id.sentTime);
        }
    }

    public static class ChatImageRightViewHolder extends RecyclerView.ViewHolder {

        public ImageView itemMessage;
        public ImageView imageAvatar;
        public TextView sentTime;

        public ChatImageRightViewHolder(final View itemView) {
            super(itemView);
            itemMessage = itemView.findViewById(R.id.imageViewMessage);
            imageAvatar = itemView.findViewById(R.id.imageViewAvatar);
            sentTime = itemView.findViewById(R.id.sentTime);
        }
    }

    public static class ChatLeftViewHolder extends RecyclerView.ViewHolder {

        public TextView itemMessage;
        public ImageView imageAvatar;
        public TextView sentTime;

        public ChatLeftViewHolder(final View itemView) {
            super(itemView);
            itemMessage = itemView.findViewById(R.id.itemMessage);
            imageAvatar = itemView.findViewById(R.id.imageViewAvatar);
            sentTime = itemView.findViewById(R.id.sentTime);
        }
    }

    public static class ChatRightViewHolder extends RecyclerView.ViewHolder {

        public TextView itemMessage;
        public ImageView imageAvatar;
        public TextView sentTime;

        public ChatRightViewHolder(final View itemView) {
            super(itemView);
            itemMessage = itemView.findViewById(R.id.itemMessage);
            imageAvatar = itemView.findViewById(R.id.imageViewAvatar);
            sentTime = itemView.findViewById(R.id.sentTime);
        }
    }
}



