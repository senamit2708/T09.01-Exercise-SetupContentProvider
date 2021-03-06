/*
* Copyright (C) 2016 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.example.android.todolist.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Switch;

import com.example.android.todolist.data.TaskContract.*;

// TODO (1) Verify that TaskContentProvider extends from ContentProvider and implements required methods
public class TaskContentProvider extends ContentProvider {

    public static final String TAG= TaskContentProvider.class.getSimpleName();

    TaskDbHelper taskDBhelper;
    Context context;
    SQLiteDatabase db;

    public static final int TASKS =100;
    public static final int TASK_ITEM = 101;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher(){
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(TaskContract.AUTHORITY, TaskContract.PATH_TASK,TASKS);
        uriMatcher.addURI(TaskContract.AUTHORITY, TaskContract.PATH_TASK+ "/#",TASK_ITEM);
        return uriMatcher;
    }


    /* onCreate() is where you should initialize anything you’ll need to setup
    your underlying data source.
    In this case, you’re working with a SQLite database, so you’ll need to
    initialize a DbHelper to gain access to it.
     */
    @Override
    public boolean onCreate() {
        // TODO (2) Complete onCreate() and initialize a TaskDbhelper on startup
        // [Hint] Declare the DbHelper as a global variable

        context = getContext();
        taskDBhelper= new TaskDbHelper(context);


        return true;
    }


    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {

        db= taskDBhelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match){
            case TASKS:
                long id= db.insert(TaskEntry.TABLE_NAME,null, values );
                if (id>0){

                    returnUri = ContentUris.withAppendedId(TaskEntry.CONTENT_URI, id);
                }
                else {
                    throw new android.database.SQLException("failed to insert row into "+uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("unknown uri"+uri);
        }


        return returnUri;


    }


    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        db= taskDBhelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor cursor;

        switch (match){
            case TASKS:

                cursor=db.query(TaskEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case TASK_ITEM:

                String id = uri.getPathSegments().get(1);

                String mSelection = "_id=?";
                String[] mSelectionArgs= new String[]{id};
                cursor=db.query(TaskEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
            default:
                throw new android.database.SQLException("URI IS WRONG"+uri);


        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }


    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

        Log.i(TAG, "inside delete method");
        db= taskDBhelper.getWritableDatabase();
        String id = uri.getPathSegments().get(1);

        getContext().getContentResolver().notifyChange(uri, null);
        int match = sUriMatcher.match(uri);
        int rtnId=0;
        switch (match){
            case TASKS:

                Log.i(TAG, "iniside the switch case of deleted itesm "+id);
                return db.delete(TaskEntry.TABLE_NAME, selection, selectionArgs);



            case TASK_ITEM:

                String mSelection = "_id=?";
                String[] mSelectionArgs = new String[]{id};
                return db.delete(TaskEntry.TABLE_NAME,mSelection,mSelectionArgs );

            default:
                Log.i(TAG, "inside default of delete "+id);
                throw new android.database.SQLException("the uri is bad "+uri);
        }


//        return rtnId;


    }


    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public String getType(@NonNull Uri uri) {

     return null;
    }

}
