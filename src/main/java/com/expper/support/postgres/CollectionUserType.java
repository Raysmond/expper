package com.expper.support.postgres;
/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

/**
 * A abstract class for {@link UserType UserTypes} that <u>may</u> return
 * mutable collections
 * (but may also return any other object types).
 * <p>
 * {@link #deepCopy(Object) Deep copies} collections to ensure any changes to
 * the
 * collection are tracked, so the collection is correctly updated. Recursively
 * clones collections
 * that contain collections, but <strong>not safe for collections that contain
 * themselves</strong>.
 */
public abstract class CollectionUserType extends MutableUserType {

    @SuppressWarnings("unchecked")
    @Override
    public Object deepCopy(Object value) throws HibernateException {
    /*
     * For non-collections, delegate; for collections, return a new collection
     * containing
     * the members of the initial collection.
     */
        if (!(value instanceof Collection)) {
            return deepCopyValue(value);
        }

    /*
     * Have to use a non-generic form because otherwise there is no way to get
     * the two ? to match
     * in the add call below.
     */
        Collection<?> collection = (Collection) value;
        Collection collectionClone = CollectionFactory.newInstance(collection.
            getClass());

    /*
     * Recurse on the members of the collection (which may themselves be
     * collections!)
     * XXX: will cause an endless loop for collections which contain themselves!
     */
        for (Object member : collection) {
            collectionClone.add(deepCopy(member));
        }

        return collectionClone;
    }

    /**
     * Creates a deep copy of the given value.
     * <p>
     * See {@link UserType#deepCopy(Object)}.
     *
     * @param value the object to be copied
     * @return a deep copy of the given object
     */
    protected abstract Object deepCopyValue(Object value);

    static final class CollectionFactory {

        /**
         * Creates a new collection instance. The runtime collection subtype of the
         * returned
         * instance matches that of the input collection, but the actual class may
         * differ.
         * <p>
         * So for a {@link LinkedList} argument, the result is guaranteed to be a
         * {@link List}. It may, however, be an {@link ArrayList} rather than a
         * {@code LinkedList}.
         *
         * @param <E>             the type of the collection elements
         * @param <T>             the type of the collection
         * @param collectionClass the runtime class of the collection
         * @return an instance of a collection that is of the same collection
         *         subtype as the
         *         given class
         * @throws IllegalArgumentException if the collection type is not supported
         */
        @SuppressWarnings("unchecked")
        static <E, T extends Collection<E>> T newInstance(Class<T> collectionClass) {
            if (List.class.isAssignableFrom(collectionClass)) {
                return (T) new ArrayList<E>();
            } else if (Set.class.isAssignableFrom(collectionClass)) {
                return (T) new HashSet<E>();
            } else {
                throw new IllegalArgumentException("Unsupported collection type: "
                    + collectionClass);
            }

        }
    }
}
