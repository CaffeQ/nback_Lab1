package mobappdev.example.nback_cimpl.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * This repository provides a way to interact with the DataStore api,
 * with this API you can save key:value pairs
 *
 * Currently this file contains only one thing: getting the highscore as a flow
 * and writing to the highscore preference.
 * (a flow is like a waterpipe; if you put something different in the start,
 * the end automatically updates as long as the pipe is open)
 *
 * Date: 25-08-2023
 * Version: Skeleton code version 1.0
 * Author: Yeetivity
 *
 */

class UserPreferencesRepository (
    private val dataStore: DataStore<Preferences>
){
    private companion object {
        val HIGHSCORE = intPreferencesKey("highscore")
        val SIDELENGTH = intPreferencesKey("sideLength")
        val N = intPreferencesKey("n")
        val TURNS = intPreferencesKey("turns")
        val PERCENT = intPreferencesKey("percent")
        val TIME = longPreferencesKey("time")
        const val TAG = "UserPreferencesRepo"
    }

    val highscore: Flow<Int> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[HIGHSCORE] ?: 0
        }
    val sideLength: Flow<Int> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[SIDELENGTH] ?: 3
        }
    val n: Flow<Int> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[N] ?: 2
        }
    val turns: Flow<Int> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[TURNS] ?: 10
        }

    val percent: Flow<Int> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[PERCENT] ?: 30
        }
    val time: Flow<Long> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[TIME] ?: 2000L
        }

    suspend fun saveHighScore(score: Int) {
        dataStore.edit { preferences ->
            preferences[HIGHSCORE] = score
        }
    }

    suspend fun saveSideLength(sideLength: Int) {
        try {
            dataStore.edit { preferences ->
                preferences[SIDELENGTH] = sideLength
            Log.e(TAG, "Logging side length " + sideLength)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving side length", e)
        }
    }
    suspend fun saveN(n: Int) {
        dataStore.edit { preferences ->
            preferences[N] = n
        }
    }
    suspend fun saveTurns(turns: Int) {
        dataStore.edit { preferences ->
            preferences[TURNS] = turns
        }
    }
    suspend fun savePercent(percent: Int) {
        dataStore.edit { preferences ->
            preferences[PERCENT] = percent
        }
    }
    suspend fun saveTime(time: Long) {
        dataStore.edit { preferences ->
            preferences[TIME] = time
        }
    }

}