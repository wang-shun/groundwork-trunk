package com.groundworkopensource.portal.statusviewer.common;

import java.util.List;

import com.groundworkopensource.portal.statusviewer.bean.HostBean;

/**
 * A simple class that represents a "page" of data out of a longer set, ie a
 * list of objects together with info to indicate the starting row and the full
 * size of the data set.
 */
public class HostListDataPage {
    /**
     * datasetSize is the total number of matching rows available.
     */
    private final int datasetSize;
    /**
     * startRow is the index within the complete dataset of the first element in
     * the data list.
     */
    private final int startRow;
    /**
     * data is a list of consecutive objects from the dataset.
     */
    private final List<HostBean> data;

    /**
     * Create an object representing a sublist of a dataset.
     * 
     * @param datasetSize
     *            is the total number of matching rows available.
     * 
     * @param startRow
     *            is the index within the complete dataset of the first element
     *            in the data list.
     * 
     * @param data
     *            is a list of consecutive objects from the dataset.
     */
    public HostListDataPage(int datasetSize, int startRow, List<HostBean> data) {
        this.datasetSize = datasetSize;
        this.startRow = startRow;
        this.data = data;
    }

    /**
     * Return the number of items in the full dataset.
     * 
     * @return int
     */
    public int getDatasetSize() {
        return datasetSize;
    }

    /**
     * Return the offset within the full dataset of the first element in the
     * list held by this object.
     * 
     * @return int
     */
    public int getStartRow() {
        return startRow;
    }

    /**
     * Return the list of objects held by this object, which is a continuous
     * subset of the full dataset.
     * 
     * @return int
     */
    public List<HostBean> getData() {
        return data;
    }
}
