/**
 * Copyright 2018 Wilfred Agyei Asomani
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.devwilfred.journally.presenter;

import com.devwilfred.journally.model.Thought;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


/**
 * this singleton contains logic for crud operations on the
 * database
 */
public class DataPresenter {

    private StorageReference mReference;
    private FirebaseFirestore mFirebaseFirestore;


    private static DataPresenter instance;


    private DataPresenter() {
        // initialise firebase storage and firestore
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        mReference = firebaseStorage.getReference();
        mFirebaseFirestore = FirebaseFirestore.getInstance();
    }

    static {
        instance = new DataPresenter();
    }

    /**
     * get an instance of data presenter
     * only one instance per application
     * @return a new instance or an already existing instance
     */
    public static DataPresenter getInstance() {
        return instance;
    }

    /**
     * delete an entry from the database
     * @param pUserUid the collection path unique to the user
     * @param pThought the entry to be deleted
     * @return a Firestore task to be observed
     */
    public Task<Void> deleteThought(String pUserUid, String pThought) {

        DocumentReference document = mFirebaseFirestore.collection(pUserUid)
                .document(pThought);

        return document.delete();
    }

    /**
     * update an entry
     * @param pUserUid collection path
     * @param pOldThought path of the entry to be updated
     * @param pNewThought the updated entry
     * @return a firestore task to be observed
     */
    public Task<Void> updateThought(String pUserUid, String pOldThought, Thought pNewThought) {

        deleteThought(pUserUid, pOldThought);

        return mFirebaseFirestore.collection(pUserUid).document(pNewThought.getTitle())
                .set(pNewThought);
    }

    /**
     * add an entry
     * @param pUserUid collection path
     * @param pThought entry to add
     * @return firestore task to be observed
     */
    public Task<Void> addThought(String pUserUid, Thought pThought) {

        return  mFirebaseFirestore.collection(pUserUid).document(pThought.getTitle())
                .set(pThought);
    }

    /**
     * upload an image to firebase storage
     * @param pData the image bytes
     * @param pUserUid collection path unique to the user
     * @param pFileName the title of the entry, to be used as the file name
     * @return upload task to be observed
     */
    public UploadTask upLoadImage(byte[] pData, String pUserUid, String pFileName) {

        return mReference.child(pUserUid + "/" + pFileName).putBytes(pData);

    }


}
