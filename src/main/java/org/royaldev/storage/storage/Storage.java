package org.royaldev.storage.storage;

import java.util.Collection;

public interface Storage<OWNER, CONTAINED> {

    boolean add(final CONTAINED item);

    boolean addAll(final Collection<CONTAINED> item);

    Collection<CONTAINED> getAll();

    OWNER getOwner();

    void setOwner(final OWNER owner);

    boolean remove(final CONTAINED item);

    boolean removeAll(final Collection<CONTAINED> item);

}
