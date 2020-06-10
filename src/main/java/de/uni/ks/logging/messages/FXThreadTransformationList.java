/*
    Copyright (C) 2020 Philip Martin and Timo Sturm

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, see <http://www.gnu.org/licenses/>.
*/
package de.uni.ks.logging.messages;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.TransformationList;

/**
 * This class is required in order to display the UI log in the UI because the simulator logic and JavaFX logic run on different threads.
 */
public class FXThreadTransformationList<E> extends TransformationList<E, E> {

    public FXThreadTransformationList(ObservableList<E> source) {
        super(source);
    }

    @Override
    protected void sourceChanged(ListChangeListener.Change<? extends E> c) {
        beginChange();
        while (c.next()) {
            if (c.wasPermutated()) {

            } else if (c.wasUpdated()) {
                update(c);
            } else if (c.wasReplaced()) {

            } else {
                addedOrRemoved(c);
            }
        }
        // commit on fx-thread
        endChangeOnFXThread();
    }

    public void endChangeOnFXThread() {
        Platform.runLater(this::endChange);
    }

    private void addedOrRemoved(ListChangeListener.Change<? extends E> c) {
        if (c.wasRemoved()) {
            nextRemove(c.getFrom(), c.getRemoved());
        } else if (c.wasAdded()) {
            nextAdd(c.getFrom(), c.getTo());
        } else {
            throw new IllegalStateException("expected either removed or added, but was:" + c);
        }
    }

    private void update(ListChangeListener.Change<? extends E> c) {
        for (int pos = c.getFrom(); pos < c.getTo(); pos++) {
            nextUpdate(pos);
        }
    }

    @Override
    public int getViewIndex(int index) {
        return index;
    }

    @Override
    public int getSourceIndex(int index) {
        return index;
    }

    @Override
    public E get(int index) {
        return getSource().get(index);
    }

    @Override
    public int size() {
        return getSource().size();
    }
}