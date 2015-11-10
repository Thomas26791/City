package com.aix.city.core;

import com.aix.city.core.data.Comment;
import com.aix.city.core.data.Event;
import com.aix.city.core.data.User;

/**
 * Created by Thomas on 14.10.2015.
 */
public class EditableCommentListing extends PostListing {

    private Event event;

    /**
     * INTERNAL USE ONLY: use instead event.getPostListing()
     *
     * @param listingSource
     */
    public EditableCommentListing(ListingSource listingSource) {
        super(listingSource);
        if (listingSource instanceof Event) {
            event = (Event) getListingSource();
        } else {
            //TODO: neuer Exceptiontyp und fehlermeldung erstellen. ExceptionHandler notwendig.
        }
    }

    public Event getEvent() {
        return event;
    }

    public Comment createComment(String message) {
        int ID = 1; //TODO: getId from server
        User user = AIxLoginModule.getInstance().getLoggedInUser();
        long now = System.currentTimeMillis();
        Comment comment = new Comment(ID, message, now, 0, user.getId(), false, event.getId());
        //TODO: event modification?
        this.addPost(comment);
        //TODO: Add Post to database
        return comment;
    }

    public void deleteComment(Comment comment) {
        if (comment.getEventId() == event.getId()) {
            this.removePost(comment);
            comment.rawDelete();
            //TODO: commit deletion to database
        }
    }
}
