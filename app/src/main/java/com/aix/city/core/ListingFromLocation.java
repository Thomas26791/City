package com.aix.city.core;

import java.sql.Timestamp;

/**
 * Created by Thomas on 14.10.2015.
 */
public class ListingFromLocation extends Listing {

    private Location location;

    /**
     * INTERNAL USE ONLY: use instead location.getListing()
     *
     * @param listingSource
     */
    public ListingFromLocation(ListingSource listingSource) {
        super(listingSource);
        if (listingSource instanceof Location) {
            location = (Location) getListingSource();
        } else {
            //TODO: neuer Exceptiontyp und fehlermeldung erstelln. Wir brauchen auch noch einen ExceptionHandler.
        }
    }

    public Location getLocation() {
        return location;
    }

    public Event createEvent(String message) {
        long ID = 0; //TODO: getID from server
        User user = AIXLoginModule.getInstance().getLoggedInUser();
        Timestamp now = new Timestamp(System.currentTimeMillis() / 1000);
        Event event = new Event(ID, message, now, 0, user, false, location, 0, false);
        getPosts().add(0, event);
        //TODO: Add Post to database
        return event;
    }
}
