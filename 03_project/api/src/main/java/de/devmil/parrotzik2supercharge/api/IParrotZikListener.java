package de.devmil.parrotzik2supercharge.api;

/**
 * Created by michaellamers on 12/01/15.
 */
public interface IParrotZikListener {

    /**
     * gets called on changes in the API data
     * @param newData
     */
    public void onDataChanged(ApiData newData);
}
